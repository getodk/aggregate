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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.FusionTableElementFormatter;
import org.opendatakit.aggregate.format.header.FusionTableHeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.gdata.util.ServiceException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FusionTable extends OAuthExternalService implements ExternalService {
  private static final Log logger = LogFactory.getLog(FusionTable.class.getName());

  /**
   * Datastore entity specific to this type of external service
   */
  private final FusionTableParameterTable objectEntity;

  /**
   * Datastore entity specific to this type of external service for the repeats
   */
  private final List<FusionTableRepeatParameterTable> repeatElementEntities
  								= new ArrayList<FusionTableRepeatParameterTable>();
  
    
  private FusionTable(FusionTableParameterTable entity, FormServiceCursor formServiceCursor, Form form, CallingContext cc) {
    super(form, formServiceCursor, new FusionTableElementFormatter(cc.getServerURL()), new FusionTableHeaderFormatter(), cc);
    objectEntity = entity;
  }
  
  private FusionTable(FusionTableParameterTable entity, Form form, ExternalServicePublicationOption externalServiceOption, CallingContext cc) throws ODKDatastoreException {
    this (entity, createFormServiceCursor(form, entity, externalServiceOption, ExternalServiceType.GOOGLE_FUSIONTABLES, cc), form, cc);
    persist(cc); 
  }

  public FusionTable(FormServiceCursor formServiceCursor, Form form, CallingContext cc) throws ODKDatastoreException {
    
    this(retrieveEntity(FusionTableParameterTable.assertRelation(cc), formServiceCursor, cc), formServiceCursor, form, cc);
        
    repeatElementEntities.addAll(FusionTableRepeatParameterTable.getRepeatGroupAssociations(
    								objectEntity.getUri(), cc));
  }

  public FusionTable(Form form, ExternalServicePublicationOption externalServiceOption, CallingContext cc)
      throws ODKDatastoreException {
    this(newEntity(FusionTableParameterTable.assertRelation(cc), cc), form, externalServiceOption, cc);
  }
    
  public void authenticateAndCreate(OAuthToken authToken, CallingContext cc) throws ODKExternalServiceException, ODKDatastoreException {

    objectEntity.setAuthToken(authToken.getToken());
    objectEntity.setAuthTokenSecret(authToken.getTokenSecret());
    
    String tableId = executeFusionTableCreation(form.getTopLevelGroupElement(), cc);
    objectEntity.setFusionTableId(tableId);
    
    List<TableId> repeatIds = new ArrayList<TableId>();
    for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
      String id = executeFusionTableCreation(repeatGroupElement, cc);
      repeatIds.add(new TableId(id, repeatGroupElement));
    }

    FusionTableRepeatParameterTable frpt = FusionTableRepeatParameterTable.assertRelation(cc);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    for (TableId a : repeatIds) {
      FusionTableRepeatParameterTable t = ds.createEntityUsingRelation(frpt, user);
      t.setUriFusionTable(objectEntity.getUri());
      t.setFormElementKey(a.getElement().constructFormElementKey(form));
      t.setFusionTableId(a.getId());
      repeatElementEntities.add(t);
    }

    fsc.setIsExternalServicePrepared(true);
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    persist(cc);
  }
  
  @Override
  protected void insertData(Submission submission, CallingContext cc) throws ODKExternalServiceException {
    // upload base submission values
    List<String> headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(),
        null);
    executeInsertData(objectEntity.getFusionTableId(), submission, headers, cc);

    // upload repeat value
    for (FusionTableRepeatParameterTable tableId : repeatElementEntities) {
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
      executeStmt(insertQuery, cc);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }
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
  private String executeStmt(String statement,  CallingContext cc)
      throws ServiceException, IOException, ODKExternalServiceException {

    OAuthToken authToken = getAuthToken();
    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(ServletConsts.OAUTH_CONSUMER_KEY,
        ServletConsts.OAUTH_CONSUMER_SECRET);
    consumer.setTokenWithSecret(authToken.getToken(), authToken.getTokenSecret());

    URI uri;
    try {
    	uri = new URI(FusionTableConsts.FUSION_SCOPE);
    } catch ( Exception e ) {
    	throw new ODKExternalServiceException(e);
    }
    
    System.out.println(uri.toString());
    HttpParams httpParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParams, FusionTableConsts.SERVICE_TIMEOUT_MILLISECONDS);
    HttpConnectionParams.setSoTimeout(httpParams, FusionTableConsts.SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS);
    
    
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
    } catch (Exception e) {
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
        String tmp = cur.replaceAll(FusionTableConsts.SINGLE_QUOTE,
            FusionTableConsts.HTML_ESCAPED_SINGLE_QUOTE);
        str.append(tmp);
      }
      str.append(BasicConsts.SINGLE_QUOTE);
      if (itr.hasNext()) {
        str.append(FormatConsts.CSV_DELIMITER);
      }
    }
    str.append(BasicConsts.RIGHT_PARENTHESIS + BasicConsts.SPACE);
    return str.toString();
  }

  private String executeFusionTableCreation(FormElementModel root, CallingContext cc)
      throws ODKExternalServiceException {

    String resultRequest;
    try {
      String createStmt = createFusionTableStatement(form, root);
      resultRequest = executeStmt(createStmt, cc);
    } catch (Exception e) {
      logger.error("Failed to create fusion table");
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

  private String createFusionTableStatement(Form form, FormElementModel rootNode) throws ODKExternalServiceException {

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
  
  protected OAuthToken getAuthToken() {
    return new OAuthToken(objectEntity.getAuthToken(), objectEntity.getAuthTokenSecret());
  }
  
  @Override
  public String getDescriptiveTargetString() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("dsrcid", objectEntity.getFusionTableId());
    return HtmlUtil.createHrefWithProperties("http://www.google.com/fusiontables/DataSource",
        properties, "View Fusion Table");
  }
  
  protected CommonFieldsBase retrieveObjectEntity() {
    return objectEntity;
  }

  @Override
  protected List<? extends CommonFieldsBase> retrieveRepateElementEntities() {
    return repeatElementEntities;
  }
}
