/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.externalservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.opendatakit.aggregate.client.services.admin.ExternServSummary;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceType;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.FusionTableElementFormatter;
import org.opendatakit.aggregate.format.header.FusionTableHeaderFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.web.CallingContext;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.util.ServiceException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FusionTable extends AbstractExternalService implements ExternalService {
  /**
   * Datastore entity specific to this type of external service
   */
  private FusionTableParameterTable objectEntity;

  private List<FusionTableRepeatParameterTable> repeatElementTableIds;
  private final GoogleService fusionTableService;

  private FusionTable(Form form, CallingContext cc) {
    super(form, new FusionTableElementFormatter(cc.getServerURL()),
        new FusionTableHeaderFormatter(), cc);
    fusionTableService = new GoogleService(FusionTableConsts.FUSTABLE_SERVICE_NAME,
        ServletConsts.APPLICATION_NAME);

  }

  public FusionTable(FormServiceCursor fsc, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException, ODKFormNotFoundException {
    this(Form.retrieveForm(fsc.getFormId(), cc), cc);
    this.fsc = fsc;
    FusionTableParameterTable fp = FusionTableParameterTable.assertRelation(cc);
    objectEntity = cc.getDatastore().getEntity(fp, fsc.getAuriService(), cc.getCurrentUser());
    repeatElementTableIds = FusionTableRepeatParameterTable.getRepeatGroupAssociations(
        new EntityKey(fp, objectEntity.getUri()), cc);
  }

  public FusionTable(Form form, ExternalServicePublicationOption externalServiceOption, CallingContext cc)
      throws ODKDatastoreException {
    this(form, cc);

    objectEntity = cc.getDatastore().createEntityUsingRelation(
        FusionTableParameterTable.assertRelation(cc), cc.getCurrentUser());

    fsc = FormServiceCursor.createFormServiceCursor(form, ExternalServiceType.GOOGLE_FUSIONTABLES,
        objectEntity, cc);
    fsc.setExternalServiceOption(externalServiceOption);
    fsc.setIsExternalServicePrepared(false);
    fsc.setOperationalStatus(OperationalStatus.ESTABLISHED);
    fsc.setEstablishmentDateTime(new Date());

    fsc.setUploadCompleted(false);
    persist(cc);
  }
  
  
  public void authenticateAndCreate(OAuthToken authToken, CallingContext cc) throws ODKExternalServiceException, ODKDatastoreException {

    try {
      GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
      oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
      oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
      oauthParameters.setOAuthToken(authToken.getToken());
      oauthParameters.setOAuthTokenSecret(authToken.getTokenSecret());
      fusionTableService.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
    } catch (OAuthException e) {
      // TODO: handle OAuth failure
      e.printStackTrace();
    }

    HeaderFormatter headerFormatter = new FusionTableHeaderFormatter();
    FormElementModel root = form.getTopLevelGroupElement();

    String tableId = executeFusionTableCreation(form, fusionTableService, headerFormatter, root,
        authToken, cc);

    List<TableId> repeatIds = new ArrayList<TableId>();

    for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
      String id = executeFusionTableCreation(form, fusionTableService, headerFormatter,
          repeatGroupElement, authToken, cc);
      TableId repeatId = new TableId(id, repeatGroupElement);
      repeatIds.add(repeatId);
    }

    // construct the list of table ids from the passed-in associations...
    repeatElementTableIds = new ArrayList<FusionTableRepeatParameterTable>();

    FusionTableRepeatParameterTable frpt = FusionTableRepeatParameterTable.assertRelation(cc);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    for (TableId a : repeatIds) {
      FusionTableRepeatParameterTable t = ds.createEntityUsingRelation(frpt, user);
      t.setUriFusionTable(objectEntity.getUri());
      t.setFormElementKey(a.getElement().constructFormElementKey(form));
      t.setFusionTableId(a.getId());
      repeatElementTableIds.add(t);
    }

    objectEntity.setAuthToken(authToken.getToken());
    objectEntity.setAuthTokenSecret(authToken.getTokenSecret());
    objectEntity.setFusionTableId(tableId);
    fsc.setIsExternalServicePrepared(true);
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    persist(cc);
  }
  
  public FusionTable(Form form, OAuthToken authToken, ExternalServicePublicationOption externalServiceOption,
      String tableId, List<TableId> repeatElementTableIdAssociations, CallingContext cc)
      throws ODKDatastoreException, ODKExternalServiceException {
    this(form, cc);

    objectEntity = cc.getDatastore().createEntityUsingRelation(
        FusionTableParameterTable.assertRelation(cc), cc.getCurrentUser());

    fsc = FormServiceCursor.createFormServiceCursor(form, ExternalServiceType.GOOGLE_FUSIONTABLES,
        objectEntity, cc);
    fsc.setExternalServiceOption(externalServiceOption);
    fsc.setIsExternalServicePrepared(true);
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    fsc.setEstablishmentDateTime(new Date());

    fsc.setUploadCompleted(false);

    objectEntity.setAuthToken(authToken.getToken());
    objectEntity.setAuthTokenSecret(authToken.getTokenSecret());
    objectEntity.setFusionTableId(tableId);

    // construct the list of table ids from the passed-in associations...
    repeatElementTableIds = new ArrayList<FusionTableRepeatParameterTable>();

    FusionTableRepeatParameterTable frpt = FusionTableRepeatParameterTable.assertRelation(cc);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    for (TableId a : repeatElementTableIdAssociations) {
      FusionTableRepeatParameterTable t = ds.createEntityUsingRelation(frpt, user);
      t.setUriFusionTable(objectEntity.getUri());
      t.setFormElementKey(a.getElement().constructFormElementKey(form));
      t.setFusionTableId(a.getId());
      repeatElementTableIds.add(t);
    }
    persist(cc);
  }

  @Override
  protected void insertData(Submission submission, CallingContext cc) throws ODKExternalServiceException {
    // upload base submission values
    List<String> headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(),
        null);
    executeInsertData(objectEntity.getFusionTableId(), submission, headers, cc);

    // upload repeat value
    for (FusionTableRepeatParameterTable tableId : repeatElementTableIds) {
      FormElementKey elementKey = tableId.getFormElementKey();
      FormElementModel element = FormElementModel.retrieveFormElementModel(form, elementKey);
      headers = headerFormatter.generateHeaders(form, element, null);

      List<SubmissionValue> values = submission.findElementValue(element);
      for (SubmissionValue value : values) {
        if (value instanceof RepeatSubmissionType) {
          RepeatSubmissionType repeat = (RepeatSubmissionType) value;
          if (repeat.getElement().equals(element)) {
            for (SubmissionSet set : repeat.getSubmissionSets()) {
              executeInsertData(tableId.getFusionTableId(), set, headers, cc);
            }
          }
        } else {
          System.err.println("ERROR: How did a non Repeat Submission Type get in the for loop?");
        }
      }
    }
  }

  private void executeInsertData(String tableId, SubmissionSet set, List<String> headers, CallingContext cc)
      throws ODKExternalServiceException {

    try {
      Row row = set.getFormattedValuesAsRow(null, formatter, true, cc);

      String insertQuery = FusionTableConsts.INSERT_STMT + tableId
          + createCsvString(headers.iterator()) + FusionTableConsts.VALUES_STMT
          + createCsvString(row.getFormattedValues().iterator());
      OAuthToken authToken = new OAuthToken(objectEntity.getAuthToken(), objectEntity
          .getAuthTokenSecret());
      executeStmt(insertQuery, authToken, cc);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }
  }

  @Override
  public void setUploadCompleted(CallingContext cc) throws ODKEntityPersistException {
    fsc.setUploadCompleted(true);
    if (fsc.getExternalServicePublicationOption() == ExternalServicePublicationOption.UPLOAD_ONLY) {
      fsc.setOperationalStatus(OperationalStatus.COMPLETED);
    }
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    ds.putEntity(fsc, user);
  }

  @Override
  public void abandon(CallingContext cc) throws ODKDatastoreException {
    if (fsc.getOperationalStatus() != OperationalStatus.COMPLETED) {
      fsc.setOperationalStatus(OperationalStatus.ABANDONED);
      persist(cc);
    }
  }

  @Override
  public ExternServSummary transform() {    
    return new ExternServSummary(fsc.getUri(),
        fsc.getCreatorUriUser(),
        fsc.getOperationalStatus(),
        fsc.getEstablishmentDateTime(),
        fsc.getExternalServicePublicationOption(),
        fsc.getUploadCompleted(),
        fsc.getLastUploadCursorDate(),
        fsc.getLastStreamingCursorDate(),
        fsc.getExternalServiceType().getServiceName(),
          getDescriptiveTargetString());
  }
  
  public void persist(CallingContext cc) throws ODKEntityPersistException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    if(repeatElementTableIds != null) {
      ds.putEntities(repeatElementTableIds, user);
    }
    ds.putEntity(objectEntity, user);
    ds.putEntity(fsc, user);
  }

  public void delete(CallingContext cc) throws ODKDatastoreException {
    // remove fusion table permission as no longer needed
    // TODO: test that the revoke REALLY works, can be easy to miss since we
    // ignore exception
    try {
      OAuthToken token = new OAuthToken(objectEntity.getAuthToken(), objectEntity
          .getAuthTokenSecret());
      ;
      GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
      oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
      oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
      oauthParameters.setOAuthToken(token.getToken());
      oauthParameters.setOAuthTokenSecret(token.getTokenSecret());
      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());

      oauthHelper.revokeToken(oauthParameters);
    } catch (OAuthException e) {
      // just moving on, as we still want to delete
      e.printStackTrace();
    }

    List<EntityKey> keys = new ArrayList<EntityKey>();
    for (FusionTableRepeatParameterTable repeat : repeatElementTableIds) {
      keys.add(new EntityKey(repeat, repeat.getUri()));
    }
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    ds.deleteEntities(keys, user);

    ds.deleteEntity(new EntityKey(objectEntity, objectEntity.getUri()), user);
    ds.deleteEntity(new EntityKey(fsc, fsc.getUri()), user);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FusionTable)) {
      return false;
    }
    FusionTable other = (FusionTable) obj;
    return (objectEntity == null ? (other.objectEntity == null)
        : (other.objectEntity != null && objectEntity.equals(other.objectEntity)))
        && (fsc == null ? (other.fsc == null) : (other.fsc != null && fsc.equals(other.fsc)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 19;
    if (objectEntity != null)
      hashCode += objectEntity.hashCode();
    if (fsc != null)
      hashCode += fsc.hashCode();
    return hashCode;
  }

  /**
   * Executes the given statement as a FusionTables API call, using the given
   * authToken for authorization.
   * 
   * @param statement
   * @param authToken
   * @return the HTTP response of the statement execution
   * @throws ServiceException
   *           if there was a failure signing the request with OAuth credentials
   * @throws IOException
   *           if there was a problem communicating over the network
   * @throws ODKExternalServiceException
   *           if FusionTables returns a response with an HTTP response code
   *           other than 200.
   */
  private static String executeStmt(String statement, OAuthToken authToken, CallingContext cc)
      throws ServiceException, IOException, ODKExternalServiceException {
    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(ServletConsts.OAUTH_CONSUMER_KEY,
        ServletConsts.OAUTH_CONSUMER_SECRET);
    consumer.setTokenWithSecret(authToken.getToken(), authToken.getTokenSecret());

    URI uri;
    try {
    	uri = new URI(FusionTableConsts.FUSION_SCOPE);
    } catch ( Exception e ) {
    	e.printStackTrace();
    	throw new ODKExternalServiceException(e);
    }
    
    System.out.println(uri.toString());
    HttpParams httpParams = new BasicHttpParams();
    
    HttpClientFactory factory = (HttpClientFactory) cc.getBean(BeanDefs.HTTP_CLIENT_FACTORY);
    HttpClient client = factory.createHttpClient(httpParams);
    HttpPost post = new HttpPost(uri);
    List<NameValuePair> formParams = new ArrayList<NameValuePair>();
    formParams.add( new BasicNameValuePair("sql", statement));
    UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, 
    				FusionTableConsts.FUSTABLE_ENCODE);
    post.setEntity(form);

    try {
      consumer.sign(post);
    } catch (OAuthMessageSignerException e) {
      e.printStackTrace();
      throw new ServiceException("Failed to sign request: " + e.getMessage());
    } catch (OAuthExpectationFailedException e) {
      e.printStackTrace();
      throw new ServiceException("Failed to sign request: " + e.getMessage());
    } catch (OAuthCommunicationException e) {
      e.printStackTrace();
      throw new IOException("Failed to sign request: " + e.getMessage());
    }

    HttpResponse resp = client.execute(post);
    // TODO: this section of code is possibly causing 'WARNING: Going to buffer
    // response body of large or unknown size. Using getResponseBodyAsStream
    // instead is recommended.'
    // The WARNING is most likely only happening when running appengine locally,
    // but we should investigate to make sure
    BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
    StringBuffer response = new StringBuffer();
    String responseLine;
    while ((responseLine = reader.readLine()) != null) {
      response.append(responseLine);
    }
    if (resp.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
      throw new ODKExternalServiceException(response.toString() + statement);
    }
    return response.toString();
  }

  private String createCsvString(Iterator<String> itr) {
    StringBuilder str = new StringBuilder();
    str.append(BasicConsts.SPACE + BasicConsts.LEFT_PARENTHESIS);
    while (itr.hasNext()) {
      String cur = itr.next();
      str.append(BasicConsts.SINGLE_QUOTE);
      if (cur != null) {
        str.append(escapeSingleQuotes(cur));
      }
      str.append(BasicConsts.SINGLE_QUOTE);
      if (itr.hasNext()) {
        str.append(FormatConsts.CSV_DELIMITER);
      }
    }
    str.append(BasicConsts.RIGHT_PARENTHESIS + BasicConsts.SPACE);
    return str.toString();
  }

  private String escapeSingleQuotes(String string) {
    return string.replaceAll(FusionTableConsts.SINGLE_QUOTE,
        FusionTableConsts.HTML_ESCAPED_SINGLE_QUOTE);
  }

  public static FusionTable createFusionTable(Form form, OAuthToken authToken,
      ExternalServicePublicationOption externalServiceOption, CallingContext cc) throws ODKDatastoreException,
      ODKExternalServiceException {

    GoogleService fusionTableService = new GoogleService(FusionTableConsts.FUSTABLE_SERVICE_NAME,
        ServletConsts.APPLICATION_NAME);
    try {
      GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
      oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
      oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
      oauthParameters.setOAuthToken(authToken.getToken());
      oauthParameters.setOAuthTokenSecret(authToken.getTokenSecret());
      fusionTableService.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
    } catch (OAuthException e) {
      // TODO: handle OAuth failure
      e.printStackTrace();
    }

    HeaderFormatter headerFormatter = new FusionTableHeaderFormatter();
    FormElementModel root = form.getTopLevelGroupElement();

    String tableid = executeFusionTableCreation(form, fusionTableService, headerFormatter, root,
        authToken, cc);

    List<TableId> repeatIds = new ArrayList<TableId>();

    for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
      String id = executeFusionTableCreation(form, fusionTableService, headerFormatter,
          repeatGroupElement, authToken, cc);
      TableId repeatId = new TableId(id, repeatGroupElement);
      repeatIds.add(repeatId);
    }

    return new FusionTable(form, authToken, externalServiceOption, tableid.trim(), repeatIds, cc);
  }

  private static String executeFusionTableCreation(Form form, GoogleService fusionTableService,
      HeaderFormatter headerFormatter, FormElementModel root, OAuthToken authToken, CallingContext cc)
      throws ODKExternalServiceException {

    String resultRequest;
    try {
      String createStmt = createFusionTableStatement(form, root, headerFormatter);
      resultRequest = executeStmt(createStmt, authToken, cc);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }

    int index = resultRequest.lastIndexOf(FusionTableConsts.CREATE_FUSION_RESP_HEADER);
    if (index >= 0) {
      return resultRequest.substring(index + FusionTableConsts.CREATE_FUSION_RESP_HEADER.length());
    } else {
      throw new ODKExternalServiceException(ErrorConsts.ERROR_OBTAINING_FUSION_TABLE_ID);
    }
  }

  private static String createFusionTableStatement(Form form, FormElementModel rootNode,
      HeaderFormatter headerFormatter) throws ODKExternalServiceException {

    List<String> headers = headerFormatter.generateHeaders(form, rootNode, null);

    // types are in the same order as the headers...
    List<ElementType> types = headerFormatter.getHeaderTypes();
    StringBuilder createStmt = new StringBuilder();
    createStmt.append(FusionTableConsts.CREATE_STMT);
    createStmt.append(BasicConsts.SINGLE_QUOTE);
    createStmt.append(rootNode.getElementName());
    createStmt.append(BasicConsts.SINGLE_QUOTE);
    createStmt.append(BasicConsts.LEFT_PARENTHESIS);

    boolean first = true;
    for (int i = 0; i < headers.size(); ++i) {
      String name = headers.get(i);
      ElementType type = types.get(i);
      if (!first) {
        createStmt.append(BasicConsts.COMMA);
      }
      first = false;
      createStmt.append(BasicConsts.SINGLE_QUOTE);
      createStmt.append(name);
      createStmt.append(BasicConsts.SINGLE_QUOTE);
      createStmt.append(BasicConsts.COLON);
      createStmt.append(FusionTableConsts.typeMap.get(type).getFusionTypeValue());
    }

    createStmt.append(BasicConsts.RIGHT_PARENTHESIS);
    return createStmt.toString();
  }

  @Override
  public String getDescriptiveTargetString() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("dsrcid", objectEntity.getFusionTableId());
    return HtmlUtil.createHrefWithProperties("http://www.google.com/fusiontables/DataSource",
        properties, "View Fusion Table");
  }
}
