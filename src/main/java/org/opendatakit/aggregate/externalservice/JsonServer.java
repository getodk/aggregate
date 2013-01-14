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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.JsonServerConsts;
import org.opendatakit.aggregate.constants.externalservice.JsonServerType;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.structure.JsonFormatterWithFilters;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.utils.WebUtils;
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
  private final JsonServerParameterTable objectEntity;

  private JsonServer(JsonServerParameterTable entity, FormServiceCursor formServiceCursor, IForm form, CallingContext cc) {
    super(form, formServiceCursor, new BasicElementFormatter(true, true, true, false), new BasicHeaderFormatter(true, true, true), cc);
    objectEntity = entity;
  }

  private JsonServer(JsonServerParameterTable entity, IForm form, ExternalServicePublicationOption externalServiceOption, CallingContext cc) throws ODKDatastoreException {
    this (entity, createFormServiceCursor(form, entity, externalServiceOption, ExternalServiceType.JSON_SERVER, cc), form, cc);
  }

  public JsonServer(FormServiceCursor formServiceCursor, IForm form, CallingContext cc) throws ODKDatastoreException {
    this(retrieveEntity(JsonServerParameterTable.assertRelation(cc), formServiceCursor, cc), formServiceCursor, form, cc);
  }

  public JsonServer(IForm form,  String serverURL, ExternalServicePublicationOption externalServiceOption, CallingContext cc)
      throws ODKDatastoreException {
    this(newEntity(JsonServerParameterTable.assertRelation(cc), cc), form, externalServiceOption, cc);

    // set stuff to ready for now
    fsc.setIsExternalServicePrepared(true);
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);

    // createForm();

    objectEntity.setServerUrl(serverURL);
    persist(cc);
  }

  @Override
  public void initiate(CallingContext cc) throws ODKExternalServiceException,
      ODKEntityPersistException, ODKOverQuotaException, ODKDatastoreException {
  }

  @Override
  public void sharePublishedFiles(String ownerEmail, CallingContext cc) {
  }

  public String getServerUrl() {
    return objectEntity.getServerUrl();
  }

  private void sendRequest(String uriString, byte[] postBody, CallingContext cc)
      throws ODKExternalServiceException {
    try {
      // TODO: change so not hard coded
      URI uri = new URI(uriString);

      System.out.println(uri.toString());
      HttpParams httpParams = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParams, JsonServerConsts.CONNECTION_TIMEOUT);
      HttpConnectionParams.setSoTimeout(httpParams, JsonServerConsts.CONNECTION_TIMEOUT);

      HttpClientFactory factory = (HttpClientFactory) cc.getBean(BeanDefs.HTTP_CLIENT_FACTORY);
      HttpClient client = factory.createHttpClient(httpParams);
      HttpPost post = new HttpPost(uri);
      post.setEntity(new ByteArrayEntity(postBody));

      HttpResponse resp = client.execute(post);
      WebUtils.readResponse(resp);

      int statusCode = resp.getStatusLine().getStatusCode();
      if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
        throw new ODKExternalServiceCredentialsException(resp.getStatusLine().getReasonPhrase() + " ("
            + statusCode + ")");
      } else if (statusCode != HttpServletResponse.SC_OK) {
        throw new ODKExternalServiceException(resp.getStatusLine().getReasonPhrase() + " ("
            + statusCode + ")");
      }
    } catch (ODKExternalServiceException e) {
      throw e; // don't wrap these...
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);// wrap...
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
    List<String> headers = headerFormatter.generateHeaders(form, form.getTopLevelGroupElement(),
        null);
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

    sendRequest("http://floresta.rhizalabs.com/cbi/upload/createDataset", postBody.getBytes(), cc);
  }

  @Override
  public void sendSubmission(Submission submission, CallingContext cc)
      throws ODKExternalServiceException {
    // TODO: think of more appropriate method
    List<Submission> list = new ArrayList<Submission>();
    list.add(submission);
    sendSubmissions(list, cc);
  }

  @Override
  public void sendSubmissions(List<Submission> submissions, CallingContext cc)
      throws ODKExternalServiceException {
    try {

      ByteArrayOutputStream baStream = new ByteArrayOutputStream();
      PrintWriter pWriter = new PrintWriter(baStream);

      System.out.println("Sending JSON Submissions");

      JsonFormatterWithFilters formatter = new JsonFormatterWithFilters(pWriter, form, null, true, null);
      formatter.processSubmissions(submissions, cc);

      pWriter.flush();

      // TODO: PROBLEM - NOT good for only one response code check at the end
      this.sendRequest(getServerUrl(), baStream.toByteArray(), cc);
    } catch (ODKExternalServiceCredentialsException e) {
      fsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
      try {
        persist(cc);
      } catch ( Exception e1) {
        e1.printStackTrace();
        throw new ODKExternalServiceException("unable to persist bad credentials status", e1);
      }
      throw e; // don't wrap
    } catch (ODKExternalServiceException e) {
      throw e; // don't wrap
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
    return (objectEntity == null ? (other.objectEntity == null)
        : (other.objectEntity != null && objectEntity.equals(other.objectEntity)))
        && (fsc == null ? (other.fsc == null) : (other.fsc != null && fsc.equals(other.fsc)));
  }

  @Override
  protected void insertData(Submission submission, CallingContext cc)
      throws ODKExternalServiceException {
    sendSubmission(submission, cc);
  }

  @Override
  public String getDescriptiveTargetString() {
    return getServerUrl();
  }

  protected CommonFieldsBase retrieveObjectEntity() {
    return objectEntity;
  }

  @Override
  protected List<? extends CommonFieldsBase> retrieveRepeatElementEntities() {
    return null;
  }

}
