/**
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.externalservice;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.common.datamodel.DeleteHelper;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;


/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public abstract class AbstractExternalService implements ExternalService {

  private static final String NO_BATCH_FUNCTIONALITY_ERROR = "ERROR! External Service does NOT implement a BATCH function to upload multiple submissions - AbstractExternalService";

  /**
   * Datastore entity holding registration of an external service for a specific
   * form and the cursor position within that form that was last processed by
   * this service.
   */
  protected final FormServiceCursor fsc;

  protected final IForm form;

  protected final ElementFormatter formatter;

  protected final HeaderFormatter headerFormatter;

  // these do not take entity bodies...
  protected static final String DELETE = "DELETE";
  protected static final String GET = "GET";

  // these do...
  protected static final String POST = "POST";
  protected static final String PUT = "PUT";
  protected static final String PATCH = "PATCH";

  // and also share all session cookies and credentials across all sessions...
  // these are thread-safe, so this is OK.
  protected static final CookieStore cookieStore = new BasicCookieStore();
  protected static final CredentialsProvider credsProvider = new BasicCredentialsProvider();

  protected static final int SERVICE_TIMEOUT_MILLISECONDS = 60000;

  protected static final int SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS = 60000;

  protected static final Charset UTF_CHARSET = Charset.forName(HtmlConsts.UTF8_ENCODE);

  protected AbstractExternalService(IForm form, FormServiceCursor formServiceCursor, ElementFormatter formatter, HeaderFormatter headerFormatter, CallingContext cc) {
    this.form = form;
    this.formatter = formatter;
    this.headerFormatter = headerFormatter;
    this.fsc = formServiceCursor;
  }

  protected abstract String getOwnership();

  protected abstract CommonFieldsBase retrieveObjectEntity();

  protected abstract List<? extends CommonFieldsBase> retrieveRepeatElementEntities();

  protected abstract void insertData(Submission submission, CallingContext cc) throws ODKExternalServiceException;

  @Override
  public boolean canBatchSubmissions() {
    return false;
  }

  @Override
  public void sendSubmissions(List<Submission> submissions, boolean streaming, CallingContext cc) throws ODKExternalServiceException {
    throw new ODKExternalServiceException(NO_BATCH_FUNCTIONALITY_ERROR);
  }

  @Override
  public void sendSubmission(Submission submission, CallingContext cc) throws ODKExternalServiceException {
    insertData(submission, cc);
  }

  @Override
  public void setUploadCompleted(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    fsc.setUploadCompleted(true);
    if (fsc.getExternalServicePublicationOption() == ExternalServicePublicationOption.UPLOAD_ONLY) {
      fsc.setOperationalStatus(OperationalStatus.COMPLETED);
    }
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    ds.putEntity(fsc, user);
  }


  protected HttpResponse sendHttpRequest(String method, String url, HttpEntity entity, List<NameValuePair> qparams, CallingContext cc) throws
      IOException {

    // setup client
    HttpClientFactory factory = (HttpClientFactory) cc.getBean(BeanDefs.HTTP_CLIENT_FACTORY);

    SocketConfig socketConfig = SocketConfig.copy(SocketConfig.DEFAULT)
        .setSoTimeout(SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS)
        .build();
    RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
        .setConnectTimeout(SERVICE_TIMEOUT_MILLISECONDS)
        .setRedirectsEnabled(true)
        .setAuthenticationEnabled(true)
        .setMaxRedirects(32)
        .setCircularRedirectsAllowed(true)
        .build();

    HttpClient client = factory.createHttpClient(socketConfig, null, requestConfig);

    // context holds authentication state machine, so it cannot be
    // shared across independent activities.
    HttpContext localContext = new BasicHttpContext();

    localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
    localContext.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider);

    HttpUriRequest request = null;
    if (entity == null && (POST.equals(method) || PATCH.equals(method) || PUT.equals(method))) {
      throw new IllegalStateException("No body supplied for POST, PATCH or PUT request");
    } else if (entity != null && !(POST.equals(method) || PATCH.equals(method) || PUT.equals(method))) {
      throw new IllegalStateException("Body was supplied for GET or DELETE request");
    }

    URI nakedUri;
    try {
      nakedUri = new URI(url);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    if (qparams == null) {
      qparams = new ArrayList<NameValuePair>();
    }
    URI uri;
    try {
      uri = new URI(nakedUri.getScheme(), nakedUri.getUserInfo(), nakedUri.getHost(),
          nakedUri.getPort(), nakedUri.getPath(), URLEncodedUtils.format(qparams, HtmlConsts.UTF8_ENCODE), null);
    } catch (URISyntaxException e1) {
      e1.printStackTrace();
      throw new IllegalStateException(e1);
    }
    System.out.println(uri.toString());

    if (GET.equals(method)) {
      HttpGet get = new HttpGet(uri);
      request = get;
    } else if (DELETE.equals(method)) {
      HttpDelete delete = new HttpDelete(uri);
      request = delete;
    } else if (PATCH.equals(method)) {
      HttpPatch patch = new HttpPatch(uri);
      patch.setEntity(entity);
      request = patch;
    } else if (POST.equals(method)) {
      HttpPost post = new HttpPost(uri);
      post.setEntity(entity);
      request = post;
    } else if (PUT.equals(method)) {
      HttpPut put = new HttpPut(uri);
      put.setEntity(entity);
      request = put;
    } else {
      throw new IllegalStateException("Unexpected request method");
    }

    HttpResponse resp = client.execute(request);
    return resp;
  }

  @Override
  public void abandon(CallingContext cc) throws ODKDatastoreException {
    if (fsc.getOperationalStatus() != OperationalStatus.COMPLETED) {
      fsc.setOperationalStatus(OperationalStatus.ABANDONED);
      persist(cc);
    }
  }

  @Override
  public void delete(CallingContext cc) throws ODKDatastoreException {
    CommonFieldsBase serviceEntity = retrieveObjectEntity();
    List<? extends CommonFieldsBase> repeats = retrieveRepeatElementEntities();

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    if (repeats != null) {
      List<EntityKey> keys = new ArrayList<EntityKey>();
      for (CommonFieldsBase repeat : repeats) {
        keys.add(repeat.getEntityKey());
      }
      DeleteHelper.deleteEntities(keys, cc);
      repeats.clear();
    }

    ds.deleteEntity(serviceEntity.getEntityKey(), user);
    ds.deleteEntity(fsc.getEntityKey(), user);
  }

  @Override
  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    CommonFieldsBase serviceEntity = retrieveObjectEntity();
    List<? extends CommonFieldsBase> repeats = retrieveRepeatElementEntities();

    if (repeats != null) {
      ds.putEntities(repeats, user);
    }
    ds.putEntity(serviceEntity, user);
    ds.putEntity(fsc, user);
  }

  @Override
  public FormServiceCursor getFormServiceCursor() {
    return fsc;
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
        fsc.getExternalServiceType(),
        getOwnership(),
        getDescriptiveTargetString());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if (retrieveObjectEntity() != null)
      hashCode += retrieveObjectEntity().hashCode();
    if (fsc != null)
      hashCode += fsc.hashCode();
    return hashCode;
  }

  protected void postUploadTask(CallingContext cc) throws ODKExternalServiceException {
    // upload data to external service
    if (!fsc.getExternalServicePublicationOption().equals(
        ExternalServicePublicationOption.STREAM_ONLY)) {

      UploadSubmissions uploadTask = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
      CallingContext ccDaemon = ContextFactory.duplicateContext(cc);
      ccDaemon.setAsDaemon(true);
      uploadTask.createFormUploadTask(fsc, true, ccDaemon);
    }
  }

  /**
   * Helper function for constructors.
   *
   */
  protected static FormServiceCursor createFormServiceCursor(IForm form, CommonFieldsBase entity, ExternalServicePublicationOption externalServiceOption, ExternalServiceType type, CallingContext cc) throws ODKDatastoreException {
    FormServiceCursor formServiceCursor = FormServiceCursor.createFormServiceCursor(form, type, entity, cc);
    formServiceCursor.setExternalServiceOption(externalServiceOption);
    formServiceCursor.setIsExternalServicePrepared(false);
    formServiceCursor.setOperationalStatus(OperationalStatus.ESTABLISHED);
    formServiceCursor.setEstablishmentDateTime(new Date());
    formServiceCursor.setUploadCompleted(false);
    return formServiceCursor;
  }

  /**
   * Helper function for constructors.
   *
   * @param parameterTableRelation
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  protected static final <T extends CommonFieldsBase> T newEntity(T parameterTableRelation, CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    return ds.createEntityUsingRelation(parameterTableRelation, user);
  }

  /**
   * Helper function for constructors.
   *
   * @param parameterTableRelation
   * @param fsc
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  protected static final <T extends CommonFieldsBase> T retrieveEntity(T parameterTableRelation, FormServiceCursor fsc, CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    return ds.getEntity(parameterTableRelation, fsc.getAuriService(), user);
  }

}
