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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceType;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor.OperationalStatus;
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
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

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

  
  private FusionTable(Form form, String webServerUrl, Datastore datastore, User user){
    super(form, new FusionTableElementFormatter(webServerUrl), new FusionTableHeaderFormatter(), datastore, user);
    fusionTableService = new GoogleService(FusionTableConsts.FUSTABLE_SERVICE_NAME,
        ServletConsts.APPLICATION_NAME);
 
  }
  
  public FusionTable(FormServiceCursor fsc, String webServerUrl, Datastore datastore, User user)
      throws ODKEntityNotFoundException, ODKDatastoreException, ODKFormNotFoundException {
    this(Form.retrieveForm(fsc.getFormId(), datastore, user), webServerUrl, datastore, user);
    this.fsc = fsc;
    FusionTableParameterTable fp = FusionTableParameterTable.createRelation(datastore, user);
    objectEntity = datastore.getEntity(fp, fsc.getServiceAuri(), user);
    repeatElementTableIds = FusionTableRepeatParameterTable.getRepeatGroupAssociations(new EntityKey(fp, objectEntity.getUri()), datastore, user);
  }

  public FusionTable(Form form, OAuthToken authToken, ExternalServiceOption externalServiceOption,
      String tableId, List<TableId> repeatElementTableIdAssociations, String webServerUrl, Datastore datastore, User user) throws ODKDatastoreException,
      ODKExternalServiceException {
    this(form, webServerUrl, datastore, user);    

    objectEntity = datastore.createEntityUsingRelation(FusionTableParameterTable.createRelation(
        datastore, user), user);
    
    fsc = FormServiceCursor.createFormServiceCursor(form, 
    		ExternalServiceType.GOOGLE_FUSIONTABLES, objectEntity, datastore, user);
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
    
    FusionTableRepeatParameterTable frpt = FusionTableRepeatParameterTable.createRelation(datastore, user);
    
    for ( TableId a : repeatElementTableIdAssociations ) {
    	FusionTableRepeatParameterTable t = datastore.createEntityUsingRelation(frpt, user);
    	t.setDomAuri(objectEntity.getUri());
    	t.setFormElementKey(a.getElement().constructFormElementKey(form));
    	t.setFusionTableId(a.getId());
    	repeatElementTableIds.add(t);
    }
    persist();
  }
  
  @Override
  protected void insertData(Submission submission) throws ODKExternalServiceException {
    // upload base submission values
    List<String> headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(), null);
    executeInsertData(objectEntity.getFusionTableId(), submission, headers);

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
              executeInsertData(tableId.getFusionTableId(), set, headers);
            }
          }
        } else {
          System.err.println("ERROR: How did a non Repeat Submission Type get in the for loop?");
        }
      }
    }
  }

  private void executeInsertData(String tableId, SubmissionSet set, List<String> headers) throws ODKExternalServiceException {
 
    try {
      Row row = set.getFormattedValuesAsRow(null, formatter, true);

      String insertQuery = FusionTableConsts.INSERT_STMT + tableId
          + createCsvString(headers.iterator()) + FusionTableConsts.VALUES_STMT
          + createCsvString(row.getFormattedValues().iterator());
      OAuthToken authToken = new OAuthToken(objectEntity.getAuthToken(), objectEntity.getAuthTokenSecret());
		executeInsert(fusionTableService, insertQuery, authToken);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }
  }
  
  @Override
  public void setUploadCompleted() throws ODKEntityPersistException {
    fsc.setUploadCompleted(true);
    if ( fsc.getExternalServiceOption() == ExternalServiceOption.UPLOAD_ONLY) {
    	fsc.setOperationalStatus(OperationalStatus.COMPLETED);
    }
    ds.putEntity(fsc, user);
  }

  @Override
  public void abandon() throws ODKDatastoreException {
	if ( fsc.getOperationalStatus() != OperationalStatus.COMPLETED ) {
	  fsc.setOperationalStatus(OperationalStatus.ABANDONED);
	  persist();  
	}
  }
  
  public void persist() throws ODKEntityPersistException {
  	ds.putEntities(repeatElementTableIds, user);
    ds.putEntity(objectEntity, user);
    ds.putEntity(fsc, user);
  }

  public void delete() throws ODKDatastoreException {
    // remove fusion table permission as no longer needed
    // TODO: test that the revoke REALLY works, can be easy to miss since we ignore exception
    try {
      OAuthToken token = new OAuthToken(objectEntity.getAuthToken(), objectEntity.getAuthTokenSecret());;
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
    return (objectEntity == null ? (other.objectEntity == null) : (objectEntity
        .equals(other.objectEntity)))
        && (fsc == null ? (other.fsc == null) : (fsc.equals(other.fsc)));
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

  private static String executeInsert(GoogleService service, String insertStmt, OAuthToken authToken)
      throws MalformedURLException, IOException, ServiceException, ODKExternalServiceException {
	  
      System.out.println(insertStmt);
    
	  	OAuthConsumer consumer = new DefaultOAuthConsumer(ServletConsts.OAUTH_CONSUMER_KEY, ServletConsts.OAUTH_CONSUMER_SECRET);
		consumer.setTokenWithSecret(authToken.getToken(), authToken.getTokenSecret());
	  
		URL url = new URL(FusionTableConsts.FUSION_SCOPE + HtmlConsts.BEGIN_PARAM
		            + FusionTableConsts.BEGIN_SQL
		            + URLEncoder.encode(insertStmt, FusionTableConsts.FUSTABLE_ENCODE));		  
		
       System.out.println(url.toString());

		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.setDoOutput(true);
		request.setDoInput(true);
		request.setRequestMethod(HtmlConsts.POST);
		request.setFixedLengthStreamingMode(0);
		try {
			consumer.sign(request);
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
		
		// TODO: this section of code is possibly causing 'WARNING: Going to buffer response body of large or unknown size. Using getResponseBodyAsStream instead is recommended.'
	    // The WARNING is most likely only happening when running appengine locally, but we should investigate to make sure
		InputStream is = request.getInputStream();
		request.connect();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuffer response = new StringBuffer();
		String responseLine;
		while ((responseLine = reader.readLine()) != null)
		{
			response.append(responseLine);
		}		
      if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new ODKExternalServiceException(response.toString() + insertStmt);
      }
      return response.toString();
  }

  private String executeQuery(GoogleService service, String queryStmt, OAuthToken authToken) throws IOException,
      ServiceException, ODKExternalServiceException {
	  	OAuthConsumer consumer = new DefaultOAuthConsumer(ServletConsts.OAUTH_CONSUMER_KEY, ServletConsts.OAUTH_CONSUMER_SECRET);
		consumer.setTokenWithSecret(authToken.getToken(), authToken.getTokenSecret());
	  
	    URL url = new URL(FusionTableConsts.FUSION_SCOPE + HtmlConsts.BEGIN_PARAM
	    		+ FusionTableConsts.BEGIN_SQL
	    		+ URLEncoder.encode(queryStmt, FusionTableConsts.FUSTABLE_ENCODE));		  
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
	   request.setDoOutput(true);
	   request.setDoInput(true);
      request.setRequestMethod(HtmlConsts.POST);
      request.setFixedLengthStreamingMode(0);
		try {
			consumer.sign(request);
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
		
      // TODO: this section of code is possibly causing 'WARNING: Going to buffer response body of large or unknown size. Using getResponseBodyAsStream instead is recommended.'
		// The WARNING is most likely only happening when running appengine locally, but we should investigate to make sure
      InputStream is = request.getInputStream();
      request.connect();
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      StringBuffer response = new StringBuffer();
      String responseLine;
      while ((responseLine = reader.readLine()) != null)
      {
         response.append(responseLine);
      }     
      if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new ODKExternalServiceException(response.toString() + queryStmt);
      }
      return response.toString();
  }

  private String createCsvString(Iterator<String> itr) {
    StringBuilder str = new StringBuilder();
    str.append(BasicConsts.SPACE + BasicConsts.LEFT_PARENTHESIS);
    while (itr.hasNext()) {
      String cur = itr.next();
      str.append(BasicConsts.SINGLE_QUOTE);
      if ( cur != null ) {
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
     return string.replaceAll(FusionTableConsts.SINGLE_QUOTE, FusionTableConsts.HTML_ESCAPED_SINGLE_QUOTE);
  }

  public static FusionTable createFusionTable(Form form, OAuthToken authToken,
      ExternalServiceOption externalServiceOption, String webServerUrl, Datastore datastore, User user)
      throws ODKDatastoreException, ODKExternalServiceException {

    GoogleService fusionTableService = new GoogleService(FusionTableConsts.FUSTABLE_SERVICE_NAME,
        ServletConsts.APPLICATION_NAME);
	  try
	  {
	    GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
		oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
		oauthParameters.setOAuthToken(authToken.getToken());
		oauthParameters.setOAuthTokenSecret(authToken.getTokenSecret());
	    fusionTableService.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
	  }
	  catch (OAuthException e)
	  {
		  // TODO
    	e.printStackTrace();
	  }

    HeaderFormatter headerFormatter = new FusionTableHeaderFormatter();
    FormElementModel root = form.getTopLevelGroupElement();

    String tableid = executeFusionTableCreation(form, fusionTableService, headerFormatter, root, authToken);

    List<TableId> repeatIds = new ArrayList<TableId>();
    
    for (FormElementModel m :  form.getRepeatGroupsInModel() ) {
      String id = executeFusionTableCreation(form, fusionTableService, headerFormatter, m, authToken);
      TableId repeatId = new TableId(id, m);
      repeatIds.add(repeatId);
    }
    
    return new FusionTable(form, authToken, externalServiceOption, tableid.trim(), repeatIds, webServerUrl, datastore, user);
  }

  private static String executeFusionTableCreation(Form form, GoogleService fusionTableService,
      HeaderFormatter headerFormatter, FormElementModel root, OAuthToken authToken) throws ODKExternalServiceException {

    String resultRequest;
    try {
      String createStmt = createFusionTableStatement(form, root, headerFormatter);
      resultRequest = executeInsert(fusionTableService, createStmt, authToken);
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
	Map<String,String> properties = new HashMap<String,String>();
	properties.put("dsrcid", objectEntity.getFusionTableId());
	return HtmlUtil.createHrefWithProperties("http://www.google.com/fusiontables/DataSource", properties, "View Fusion Table");
  }
}
