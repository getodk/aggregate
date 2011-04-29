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
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import org.opendatakit.aggregate.client.form.ExternServSummary;
import org.opendatakit.aggregate.constants.common.ExternalServiceOption;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceType;
import org.opendatakit.aggregate.constants.externalservice.JsonServerConsts;
import org.opendatakit.aggregate.constants.externalservice.JsonServerType;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.structure.JsonFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.web.CallingContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class JsonServer extends AbstractExternalService implements ExternalService {

  /**
   * Datastore entity specific to this type of external service
   */
  private JsonServerParameterTable objectEntity;

  private JsonServer(Form form, CallingContext cc) {
    super(form, new BasicElementFormatter(true, true, true), new BasicHeaderFormatter(true, true,
        true), cc);
  }

  public JsonServer(FormServiceCursor fsc, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException, ODKFormNotFoundException {
    this(Form.retrieveForm(fsc.getFormId(), cc), cc);

    objectEntity = cc.getDatastore().getEntity(JsonServerParameterTable.assertRelation(cc),
        fsc.getAuriService(), cc.getCurrentUser());

    // createForm();
  }

  public JsonServer(Form form, String serverURL, ExternalServiceOption externalServiceOption,
      CallingContext cc) throws ODKDatastoreException, ODKExternalServiceException {
    this(form, cc);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    objectEntity = ds.createEntityUsingRelation(JsonServerParameterTable.assertRelation(
        cc), user);
    fsc = FormServiceCursor.createFormServiceCursor(form, ExternalServiceType.JSON_SERVER, objectEntity,
        cc);
    fsc.setExternalServiceOption(externalServiceOption);
    fsc.setIsExternalServicePrepared(true);
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    fsc.setEstablishmentDateTime(new Date());
    fsc.setUploadCompleted(false);
    objectEntity.setServerUrl(serverURL);
    ds.putEntity(fsc, user);
    ds.putEntity(objectEntity, user);

    // createForm();
  }

  public void authenticateAndCreate(OAuthToken authToken, CallingContext cc) throws ODKExternalServiceException, ODKDatastoreException {
    // TODO: figure out if this could be useful in another context
  }

  
  @Override
  public void abandon(CallingContext cc) throws ODKDatastoreException {
	if ( fsc.getOperationalStatus() != OperationalStatus.COMPLETED ) {
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
        fsc.getExternalServiceOption().getDescriptionOfOption(),
        fsc.getExternalServiceType().getServiceName(),
          getDescriptiveTargetString());
  }
  
  public void persist(CallingContext cc) throws ODKEntityPersistException {
	Datastore ds = cc.getDatastore();
	User user = cc.getCurrentUser();
    ds.putEntity(objectEntity, user);
    ds.putEntity(fsc, user);
  }

  public void delete(CallingContext cc) throws ODKDatastoreException {
	Datastore ds = cc.getDatastore();
	User user = cc.getCurrentUser();
    ds.deleteEntity(new EntityKey(objectEntity, objectEntity.getUri()), user);
    ds.deleteEntity(new EntityKey(fsc, fsc.getUri()), user);
  }

  public String getServerUrl() {
    return objectEntity.getServerUrl();
  }

  private void sendRequest(String uriString, byte[] postBody, CallingContext cc) throws ODKExternalServiceException {
    try {
        // TODO: change so not hard coded
        URI uri = new URI(uriString);
        
        System.out.println(uri.toString());
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setIntParameter( HttpClientParams.SO_TIMEOUT, 
      		  					  JsonServerConsts.CONNECTION_TIMEOUT);
        
        HttpClientFactory factory = (HttpClientFactory) cc.getBean(BeanDefs.HTTP_CLIENT_FACTORY);
        HttpClient client = factory.createHttpClient(httpParams);
        HttpPost post = new HttpPost(uri);
        post.setEntity(new ByteArrayEntity(postBody));
        
        HttpResponse resp = client.execute(post);

        if (resp.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
          InputStreamReader is = new InputStreamReader(resp.getEntity().getContent());
          BufferedReader reader = new BufferedReader(is);

          String responseLine = reader.readLine();
          while (responseLine != null) {
            System.out.print(responseLine);
            responseLine = reader.readLine();
          }
          is.close();
        } else {
        	throw new ODKExternalServiceException(resp.getStatusLine().getReasonPhrase() + " (" + 
        			resp.getStatusLine().getStatusCode() + ")");
        }
      } catch (Exception e) {
        throw new ODKExternalServiceException(e);
      }
  }
  
  private void createForm(CallingContext cc) throws ODKExternalServiceException {
    System.out.println("BEGINNING INSERTION");

    JsonArray def = new JsonArray();

    JsonObject jsonFormBase = new JsonObject();
    jsonFormBase.addProperty("ODKID", form.getFormId());
    def.add(jsonFormBase);

    // TODO: Waylon -- I need to understand this to recommend an alternative
    // mechanism...
    List<String> headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(), null);
    List<ElementType> types = headerFormatter.getHeaderTypes();
    for (int i = 0; i < headers.size(); ++i) {
      String name = headers.get(i);
      ElementType type = types.get(i);
      JsonObject element = new JsonObject();
      JsonServerType jt = JsonServerConsts.typeMap.get(type);
      if (jt == JsonServerType.CONTENT_TYPE) {
        // TODO: need to handle the case where element is
        // a binary -- we don't know whether it is really
        // a picture or not, but should we claim it to be?
        element.addProperty("TYPE", "picture");
      } else {
        element.addProperty("TYPE", jt.getJsonServerTypeValue());
      }
      element.addProperty("LABEL", name);
      def.add(element);
    }

    String postBody = def.toString();
    System.out.println(postBody);

    sendRequest("http://floresta.rhizalabs.com/cbi/upload/createDataset", 
    			postBody.getBytes(), cc);
  }

  @Override
  public void sendSubmission(Submission submission, CallingContext cc) throws ODKExternalServiceException {
    // TODO: think of more appropriate method
    List<Submission> list = new ArrayList<Submission>();
    list.add(submission);
    sendSubmissions(list, cc);
  }

  @Override
  public void sendSubmissions(List<Submission> submissions, CallingContext cc) throws ODKExternalServiceException {
    try {
    
      ByteArrayOutputStream baStream = new ByteArrayOutputStream();
      PrintWriter pWriter = new PrintWriter(baStream);

      System.out.println("Sending JSON Submissions");

      JsonFormatter formatter = new JsonFormatter(pWriter, null, form, cc);
      formatter.processSubmissions(submissions, cc);

      pWriter.flush();

      // TODO: PROBLEM - NOT good for only one response code check at the end
      this.sendRequest( getServerUrl(), 
    		  			baStream.toByteArray(), cc);

    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }

  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JsonServer)) {
      return false;
    }
    JsonServer other = (JsonServer) obj;
    return (objectEntity == null ? (other.objectEntity == null) : 
    			(other.objectEntity != null && objectEntity.equals(other.objectEntity)))
        && (fsc == null ? (other.fsc == null) : 
        		(other.fsc != null && fsc.equals(other.fsc)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if (objectEntity != null)
      hashCode += objectEntity.hashCode();
    if (fsc != null)
      hashCode += fsc.hashCode();
    return hashCode;
  }

  @Override
  public void setUploadCompleted(CallingContext cc) throws ODKEntityPersistException {
    fsc.setUploadCompleted(true);
    if ( fsc.getExternalServiceOption() == ExternalServiceOption.UPLOAD_ONLY) {
    	fsc.setOperationalStatus(OperationalStatus.COMPLETED);
    }
	Datastore ds = cc.getDatastore();
	User user = cc.getCurrentUser();
    ds.putEntity(fsc, user);
  }

  @Override
  protected void insertData(Submission submission, CallingContext cc) throws ODKExternalServiceException {
    sendSubmission(submission, cc);
  }

  @Override
  public String getDescriptiveTargetString() {
	return getServerUrl();
  }
}
