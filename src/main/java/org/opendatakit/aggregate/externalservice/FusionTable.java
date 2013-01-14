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
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.FusionTableElementFormatter;
import org.opendatakit.aggregate.format.header.FusionTableHeaderFormatter;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.gdata.util.ServiceException;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class FusionTable extends OAuth2ExternalService implements ExternalService {
  private static final Log logger = LogFactory.getLog(FusionTable.class.getName());

  private static final String FUSION_TABLE_OAUTH2_SCOPE = "https://www.googleapis.com/auth/drive https://www.googleapis.com/auth/fusiontables";

  private static final String FUSION_TABLE_QUERY_API = "https://www.googleapis.com/fusiontables/v1/query";
  private static final String FUSION_TABLE_TABLE_API = "https://www.googleapis.com/fusiontables/v1/tables";

  // these do not take entity bodies...
  private static final String DELETE = "DELETE";
  private static final String GET = "GET";

  // these do...
  private static final String POST = "POST";
  private static final String PUT = "PUT";
  private static final String PATCH = "PATCH";

  // and also share all session cookies and credentials across all sessions...
  // these are thread-safe, so this is OK.
  private static final CookieStore cookieStore = new BasicCookieStore();
  private static final CredentialsProvider credsProvider = new BasicCredentialsProvider();

  // access token is not shared across all active fusionTables actions...
  // TODO: verify that this does not mess up when multiple publishers are run
  private String accessToken = null;

  /**
   * Datastore entity specific to this type of external service
   */
  private final FusionTable2ParameterTable objectEntity;

  /**
   * Datastore entity specific to this type of external service for the repeats
   */
  private final List<FusionTableRepeatParameterTable> repeatElementEntities = new ArrayList<FusionTableRepeatParameterTable>();

  /**
   * Common base initialization of a FusionTable (both new and existing).
   *
   * @param entity
   * @param formServiceCursor
   * @param form
   * @param cc
   */
  private FusionTable(FusionTable2ParameterTable entity, FormServiceCursor formServiceCursor,
      IForm form, CallingContext cc) {
    super(form, formServiceCursor, new FusionTableElementFormatter(cc.getServerURL()),
        new FusionTableHeaderFormatter(), cc);
    objectEntity = entity;
  }

  /**
   * Continuation of the creation of a brand new FusionTable.
   * Needed because entity must be passed into two objects in the constructor.
   *
   * @param entity
   * @param form
   * @param externalServiceOption
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   */
  private FusionTable(FusionTable2ParameterTable entity, IForm form,
      ExternalServicePublicationOption externalServiceOption, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException, ODKDatastoreException {
    this(entity, createFormServiceCursor(form, entity, externalServiceOption,
        ExternalServiceType.GOOGLE_FUSIONTABLES, cc), form, cc);

    // and create records for all the repeat elements (but without any actual table ids)...
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    FusionTableRepeatParameterTable frpt = FusionTableRepeatParameterTable.assertRelation(cc);

    for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
      FusionTableRepeatParameterTable t = ds.createEntityUsingRelation(frpt, user);
      t.setUriFusionTable(objectEntity.getUri());
      t.setFormElementKey(repeatGroupElement.constructFormElementKey(form));
      repeatElementEntities.add(t);
    }
    persist(cc);
  }

  /**
   * Reconstruct a FusionTable definition from its persisted representation in the datastore.
   *
   * @param formServiceCursor
   * @param form
   * @param cc
   * @throws ODKDatastoreException
   */
  public FusionTable(FormServiceCursor formServiceCursor, IForm form, CallingContext cc)
      throws ODKDatastoreException {

    this(retrieveEntity(FusionTable2ParameterTable.assertRelation(cc), formServiceCursor, cc),
        formServiceCursor, form, cc);

    repeatElementEntities.addAll(FusionTableRepeatParameterTable.getRepeatGroupAssociations(
        objectEntity.getUri(), cc));
  }

  /**
   * Create a brand new FusionTable
   *
   * @param form
   * @param externalServiceOption
   * @param ownerUserEmail -- user that should be granted ownership of the fusionTable artifact(s)
   * @param cc
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   */
  public FusionTable(IForm form, ExternalServicePublicationOption externalServiceOption,
      String ownerEmail, CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException,
      ODKDatastoreException {
    this(newFusionTableEntity(ownerEmail, cc), form, externalServiceOption,
        cc);
  }

  /**
   * Helper function to create a FusionTable parameter table (missing the not-yet-created tableId).
   *
   * @param ownerEmail
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  private static final FusionTable2ParameterTable newFusionTableEntity(String ownerEmail, CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    FusionTable2ParameterTable t = ds.createEntityUsingRelation(FusionTable2ParameterTable.assertRelation(cc), user);
    t.setOwnerEmail(ownerEmail);
    return t;
  }

  public void initiate(CallingContext cc) throws ODKExternalServiceException, ODKDatastoreException {
    //authenticate2AndCreate(FUSION_TABLE_OAUTH2_SCOPE, cc);

    // See if the access token we know actually works...

    accessToken = ServerPreferencesProperties.getServerPreferencesProperty(cc,
        ServerPreferencesProperties.GOOGLE_FUSION_TABLE_OAUTH2_ACCESS_TOKEN);

    if (accessToken == null) {
      try {
        accessToken = getOAuth2AccessToken(FUSION_TABLE_OAUTH2_SCOPE, cc);
        ServerPreferencesProperties.setServerPreferencesProperty(cc,
            ServerPreferencesProperties.GOOGLE_FUSION_TABLE_OAUTH2_ACCESS_TOKEN, accessToken);
      } catch (Exception e) {
        throw new ODKExternalServiceCredentialsException(
            "Unable to obtain OAuth2 access token: " + e.toString());
      }
    }

    // OK if we got here, we have a valid accessToken.

    if (fsc.isExternalServicePrepared() == null || !fsc.isExternalServicePrepared()) {

      if (objectEntity.getFusionTableId() == null) {
        String tableId = executeFusionTableCreation(form.getTopLevelGroupElement(), cc);
        objectEntity.setFusionTableId(tableId);
      }

      // See which of the repeat groups still need to have their tableId created
      // and define those...
      for (FormElementModel repeatGroupElement : form.getRepeatGroupsInModel()) {
        boolean found = false;
        for ( FusionTableRepeatParameterTable t : repeatElementEntities ) {
          if ( objectEntity.getUri().equals(t.getUriFusionTable()) &&
               repeatGroupElement.constructFormElementKey(form).equals(t.getFormElementKey()) ) {
            // Found the match
            if ( found ) {
              throw new ODKExternalServiceException("duplicate row in FusionTableRepeatParameterTable");
            }
            found = true;
            if ( t.getFusionTableId() != null ) {
              String id = executeFusionTableCreation(repeatGroupElement, cc);
              t.setFusionTableId(id);
            }
          }
        }
        if ( !found ) {
          throw new ODKExternalServiceException("missing row in FusionTableRepeatParameterTable");
        }
      }

      // TODO: define the view here...

      fsc.setIsExternalServicePrepared(true);
    }
    fsc.setOperationalStatus(OperationalStatus.ACTIVE);
    persist(cc);

    // upload data to external service
    if (!fsc.getExternalServicePublicationOption().equals(ExternalServicePublicationOption.STREAM_ONLY)) {

      UploadSubmissions uploadTask = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
      CallingContext ccDaemon = ContextFactory.duplicateContext(cc);
      ccDaemon.setAsDaemon(true);
      uploadTask.createFormUploadTask(fsc, ccDaemon);

    }

  }

  public void sharePublishedFiles(String ownerEmail, CallingContext cc) {

  }

  /**
   * Requests an access token for the given scopes.
   *
   * Precondition: a service account and private key have been configured for the server.
   *
   * @param scopes
   * @param cc
   * @return access token (must be a Bearer token).
   * @throws ODKEntityNotFoundException
   * @throws ODKOverQuotaException
   */
  protected String getOAuth2AccessToken(String scopes, CallingContext cc) throws ODKEntityNotFoundException, ODKOverQuotaException {

    String serviceEmailAddress = ServerPreferencesProperties.getServerPreferencesProperty(cc, ServerPreferencesProperties.GOOGLE_API_SERVICE_ACCOUNT_EMAIL);
    String privateKeyString = ServerPreferencesProperties.getServerPreferencesProperty(cc, ServerPreferencesProperties.PRIVATE_KEY_FILE_CONTENTS);

    if ( serviceEmailAddress == null || privateKeyString == null || serviceEmailAddress.length() == 0 || privateKeyString.length() == 0 ) {
      throw new IllegalArgumentException("No OAuth2 credentials. Have you supplied any OAuth2 credentials on the Site Admin / Preferences page?");
    }
    byte[] privateKeyBytes = Base64.decodeBase64(privateKeyString);

    Map<String, String> jwtHeader = new HashMap<String, String>();
    Map<String, Object> jwtBody = new HashMap<String, Object>();

    jwtHeader.put("alg", "RS256");
    jwtHeader.put("typ", "JWT");
    String headerValueString = null;
    try {
       byte[] headerValue = mapper.writeValueAsBytes(jwtHeader);
       headerValueString = Base64.encodeBase64URLSafeString(headerValue);
    } catch (JsonGenerationException e) {
       e.printStackTrace();
       throw new IllegalArgumentException("Unexpected JsonGenerationException " + e.toString());
    } catch (JsonMappingException e) {
       e.printStackTrace();
       throw new IllegalArgumentException("Unexpected JsonMappingException " + e.toString());
    } catch (IOException e) {
       e.printStackTrace();
       throw new IllegalArgumentException("Unexpected IOException " + e.toString());
    }

    jwtBody.put("iss", serviceEmailAddress);
    jwtBody.put("scope", scopes);
    jwtBody.put("aud", "https://accounts.google.com/o/oauth2/token");
    int now = ((int) (System.currentTimeMillis() / 1000L));
    jwtBody.put("exp", now + 59 * 60);
    jwtBody.put("iat", now);

    String claimSetValueString = null;
    try {
       byte[] claimSetValue = mapper.writeValueAsBytes(jwtBody);
       claimSetValueString = Base64
             .encodeBase64URLSafeString(claimSetValue);
    } catch (JsonGenerationException e) {
       e.printStackTrace();
       throw new IllegalArgumentException("Unexpected JsonGenerationException " + e.toString());
    } catch (JsonMappingException e) {
       e.printStackTrace();
       throw new IllegalArgumentException("Unexpected JsonMappingException " + e.toString());
    } catch (IOException e) {
       e.printStackTrace();
       throw new IllegalArgumentException("Unexpected IOException " + e.toString());
    }

    String fullValue = headerValueString + "." + claimSetValueString;

    String signatureValueString = null;

    {
       KeyStore ks = null;
       try {
          ks = KeyStore.getInstance("PKCS12");
       } catch (KeyStoreException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected KeyStoreException " + e.toString());
       }
       try {
         ks.load(new ByteArrayInputStream(privateKeyBytes), "notasecret".toCharArray());
       } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected NoSuchAlgorithmException " + e.toString());
       } catch (CertificateException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected CertificateException " + e.toString());
       } catch (FileNotFoundException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected FileNotFoundException " + e.toString());
       } catch (IOException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected IOException " + e.toString());
       }

       Enumeration aliasEnum = null;
       try {
          aliasEnum = ks.aliases();
       } catch (KeyStoreException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected KeyStoreException " + e.toString());
       }

       Key key = null;

       while (aliasEnum.hasMoreElements()) {
          String keyName = (String) aliasEnum.nextElement();
          try {
             key = ks.getKey(keyName, "notasecret".toCharArray());
          } catch (UnrecoverableKeyException e) {
             e.printStackTrace();
             throw new IllegalArgumentException("Unexpected UnrecoverableKeyException " + e.toString());
          } catch (KeyStoreException e) {
             e.printStackTrace();
             throw new IllegalArgumentException("Unexpected KeyStoreException " + e.toString());
          } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
             throw new IllegalArgumentException("Unexpected NoSuchAlgorithmException " + e.toString());
          }
          break;
       }

       Signature signer = null;
       try {
          signer = Signature.getInstance("SHA256withRSA");
       } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected NoSuchAlgorithmException " + e.toString());
       }

       try {
          signer.initSign((PrivateKey) key);
       } catch (InvalidKeyException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected InvalidKeyException " + e.toString());
       }
       try {
          signer.update(fullValue.getBytes("UTF-8"));
       } catch (SignatureException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected SignatureException " + e.toString());
       } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected UnsupportedEncodingException " + e.toString());
       }
       try {
          byte[] signature = signer.sign();
          signatureValueString = Base64.encodeBase64URLSafeString(signature);
       } catch (SignatureException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unexpected SignatureException " + e.toString());
       }
    }

    String assertionString = fullValue + "." + signatureValueString;
    String grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    int SERVICE_TIMEOUT_MILLISECONDS = 60000;

    int SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS = 60000;

    // DON'T NEED clientId on the toke request...
    // addCredentials(clientId, clientSecret, nakedUri.getHost());
    // setup request interceptor to do preemptive auth
    // ((DefaultHttpClient)
    // client).addRequestInterceptor(getPreemptiveAuth(), 0);

    HttpParams httpParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParams,
          SERVICE_TIMEOUT_MILLISECONDS);
    HttpConnectionParams.setSoTimeout(httpParams,
          SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS);
    // support redirecting to handle http: => https: transition
    HttpClientParams.setRedirecting(httpParams, true);
    // support authenticating
    HttpClientParams.setAuthenticating(httpParams, true);

    httpParams.setParameter(ClientPNames.MAX_REDIRECTS, 1);
    httpParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
    // setup client
    HttpClientFactory factory = (HttpClientFactory) cc.getBean(BeanDefs.HTTP_CLIENT_FACTORY);
    HttpClient client = factory.createHttpClient(httpParams);

    URI nakedUri = null;
    try {
       nakedUri = new URI("https://accounts.google.com/o/oauth2/token");
    } catch (URISyntaxException e) {
       e.printStackTrace();
       throw new IllegalArgumentException("Unexpected URISyntaxException " + e.toString());
    }

    HttpPost httppost = new HttpPost(nakedUri);

    // THESE ARE POST BODY ARGS...
    List<NameValuePair> qparams = new ArrayList<NameValuePair>();
    qparams.add(new BasicNameValuePair("grant_type", grantType));
    qparams.add(new BasicNameValuePair("assertion", assertionString));
    UrlEncodedFormEntity postentity = null;
    try {
       postentity = new UrlEncodedFormEntity(qparams, "UTF-8");
    } catch (UnsupportedEncodingException e) {
       e.printStackTrace();
       throw new IllegalArgumentException("Unexpected UnsupportedEncodingException " + e.toString());
    }

    httppost.setEntity(postentity);

    HttpContext localContext = new BasicHttpContext();

    HttpResponse response = null;
    try {
       response = client.execute(httppost, localContext);
       int statusCode = response.getStatusLine().getStatusCode();

       if (statusCode != HttpStatus.SC_OK) {
          throw new IllegalArgumentException(
                "Error with Oauth2 token request - reason: "
                      + response.getStatusLine().getReasonPhrase()
                      + " status code: " + statusCode);
       } else {
          HttpEntity entity = response.getEntity();

          if (entity != null
                && entity.getContentType().getValue().toLowerCase()
                      .contains("json")) {
             ObjectMapper mapper = new ObjectMapper();
             BufferedReader reader = new BufferedReader(
                   new InputStreamReader(entity.getContent()));
             Map<String, Object> userData = mapper.readValue(reader, Map.class);

             String accessToken = (String) userData.get("access_token");
             String tokenType = (String) userData.get("token_type");

             if ( "Bearer".equals(tokenType)) {
               return accessToken;
             }
             throw new IllegalArgumentException(
                 "Error with Oauth2 token request - token_type is not Bearer: " +
                     tokenType);
          } else {
             throw new IllegalArgumentException(
                   "Error with Oauth2 token request - missing body");
          }
       }
    } catch (IOException e) {
      e.printStackTrace();
       throw new IllegalArgumentException(e.toString());
    }

  }

  @Override
  protected void insertData(Submission submission, CallingContext cc)
      throws ODKExternalServiceException {
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

  private void executeInsertData(String tableId, SubmissionSet set, List<String> headers,
      CallingContext cc) throws ODKExternalServiceException {

    try {
      Row row = set.getFormattedValuesAsRow(null, formatter, true, cc);

      String insertQuery = FusionTableConsts.INSERT_STMT + tableId
          + createCsvString(headers.iterator()) + FusionTableConsts.VALUES_STMT
          + createCsvString(row.getFormattedValues().iterator());
      executeStmt(POST, FUSION_TABLE_QUERY_API, insertQuery, null, cc);
    } catch (ODKExternalServiceCredentialsException e) {
      fsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
      try {
        persist(cc);
      } catch (Exception e1) {
        e1.printStackTrace();
        throw new ODKExternalServiceException(
            "Unable to set OperationalStatus to Bad credentials: " + e1);
      }
      throw e;
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }
  }

  /**
   * Executes the given statement as a FusionTables API call, using the given
   * authToken for authorization.
   *
   * @param tablesUrl -- fusionTables URL on which to issue the request
   * @param statement -- Either sql= parameter if a FT query or an application/json body (must be NULL for GET and DELETE requests).
   * @param qparams -- arguments on the URL or null.
   * @param cc -- calling context
   * @return the HTTP response of the statement execution
   * @throws ServiceException
   *           if there was a failure signing the request with OAuth2 credentials
   * @throws IOException
   *           if there was a problem communicating over the network
   * @throws ODKExternalServiceException
   *           if FusionTables returns a response with an HTTP response code
   *           other than 200.
   */
  private String executeStmt(String method, String tablesUrl, String statement, List<NameValuePair> qparams, CallingContext cc) throws ServiceException,
      IOException, ODKExternalServiceException {

    try {
      return coreExecuteStmt( method, tablesUrl, statement, qparams, cc);
    } catch ( ODKExternalServiceCredentialsException e) {
      try {
        accessToken = getOAuth2AccessToken(FUSION_TABLE_OAUTH2_SCOPE, cc);
        ServerPreferencesProperties.setServerPreferencesProperty(cc,
            ServerPreferencesProperties.GOOGLE_FUSION_TABLE_OAUTH2_ACCESS_TOKEN, accessToken);
      } catch (Exception e1) {
        throw new ODKExternalServiceCredentialsException("Unable to obtain OAuth2 access token: "
            + e1.toString());
      }
      return coreExecuteStmt( method, tablesUrl, statement, qparams, cc);
    }
  }

  private String coreExecuteStmt(String method, String tablesUrl, String statement, List<NameValuePair> qparams, CallingContext cc) throws ServiceException,
      IOException, ODKExternalServiceException {
    boolean isQuery = FUSION_TABLE_QUERY_API.equals(tablesUrl);

    HttpParams httpParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParams,
        FusionTableConsts.SERVICE_TIMEOUT_MILLISECONDS);
    HttpConnectionParams.setSoTimeout(httpParams,
        FusionTableConsts.SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS);

    HttpClientFactory factory = (HttpClientFactory) cc.getBean(BeanDefs.HTTP_CLIENT_FACTORY);
    HttpClient client = factory.createHttpClient(httpParams);

    // support redirecting to handle http: => https: transition
    HttpClientParams.setRedirecting(httpParams, true);
    // support authenticating
    HttpClientParams.setAuthenticating(httpParams, true);

    httpParams.setParameter(ClientPNames.MAX_REDIRECTS, 1);
    httpParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

    // context holds authentication state machine, so it cannot be
    // shared across independent activities.
    HttpContext localContext = new BasicHttpContext();

    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);

    HttpUriRequest request = null;
    if ( statement == null && (POST.equals(method) || PATCH.equals(method) || PUT.equals(method)) ) {
      throw new ODKExternalServiceException("No body supplied for POST, PATCH or PUT request");
    } else if ( statement != null && !(POST.equals(method) || PATCH.equals(method) || PUT.equals(method)) ) {
      throw new ODKExternalServiceException("Body was supplied for GET or DELETE request");
    }

    URI nakedUri;
    try {
      nakedUri = new URI(tablesUrl);
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }

    if ( qparams == null ) {
      qparams = new ArrayList<NameValuePair>();
    }
    qparams.add(new BasicNameValuePair("access_token", accessToken));
    URI uri;
    try {
      uri = URIUtils.createURI(nakedUri.getScheme(), nakedUri.getHost(), nakedUri.getPort(),
          nakedUri.getPath(), URLEncodedUtils.format(qparams, "UTF-8"), null);
    } catch (URISyntaxException e1) {
      e1.printStackTrace();
      logger.error(e1.toString());
      throw new ODKExternalServiceException(e1);
    }
    System.out.println(uri.toString());

    HttpEntity entity = null;
    if ( statement != null ) {
      if ( isQuery ) {
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("sql", statement));
        entity = new UrlEncodedFormEntity(formParams,
            FusionTableConsts.FUSTABLE_ENCODE);
      } else {
        entity = new StringEntity(statement, "application/json", "UTF-8");
      }
    }

    if ( GET.equals(method) ) {
      HttpGet get = new HttpGet(uri);
      request = get;
    } else if ( DELETE.equals(method) ) {
      HttpDelete delete = new HttpDelete(uri);
      request = delete;
    } else if ( PATCH.equals(method) ) {
      HttpPatch patch = new HttpPatch(uri);
      patch.setEntity(entity);
      request = patch;
    } else if ( POST.equals(method) ) {
      HttpPost post = new HttpPost(uri);
      post.setEntity(entity);
      request = post;
    } else if ( PUT.equals(method) ) {
      HttpPut put = new HttpPut(uri);
      put.setEntity(entity);
      request = put;
    } else {
      throw new ODKExternalServiceException("Unexpected request method");
    }

    HttpResponse resp = client.execute(request);
    String response = WebUtils.readResponse(resp);

    int statusCode = resp.getStatusLine().getStatusCode();
    if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
      // TODO: handle refresh with refreshToken...
      throw new ODKExternalServiceCredentialsException(response.toString() + statement);
    } else if (statusCode != HttpServletResponse.SC_OK) {
      throw new ODKExternalServiceException(response.toString() + statement);
    }
    return response;
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
      resultRequest = executeStmt(POST, FUSION_TABLE_TABLE_API, createStmt, null, cc);


    } catch (ODKExternalServiceException e) {
      logger.error("Failed to create fusion table: " + e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      logger.error("Failed to create fusion table: " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }

    try {
      Map<String,Object> result = mapper.readValue(resultRequest, Map.class);
      String tableId = (String) result.get("tableId");
      if ( tableId != null && tableId.length() > 0 ) {

        // NOTE: tableId is also the Google Drive fileId

        // obtain the set of permissions on Google Drive
        resultRequest = executeStmt(GET, "https://www.googleapis.com/drive/v2/files/" + URLEncoder.encode(tableId, "UTF-8") + "/permissions", null, null, cc);
        result = mapper.readValue(resultRequest, Map.class);

        String permETag = null;
        boolean found = false;
        List<Object> items = (List<Object>) result.get("items");
        for ( Object item : items ) {
          Map<String,Object> perm = (Map<String,Object>) item;
          if ( perm.get("name").equals(objectEntity.getOwnerEmail().substring(SecurityUtils.MAILTO_COLON.length())) ) {
            found = true;
            break;
          }
        }

        if ( !found ) {
          Map<String,Object> newPerm = new HashMap<String,Object>();
          newPerm.put("kind", "drive#permission");
          newPerm.put("role", "owner");
          newPerm.put("type", "user");
          newPerm.put("value", objectEntity.getOwnerEmail().substring(SecurityUtils.MAILTO_COLON.length()));
          String body = mapper.writeValueAsString(newPerm);
          resultRequest = executeStmt(POST, "https://www.googleapis.com/drive/v2/files/" + URLEncoder.encode(tableId, "UTF-8") + "/permissions",body, null, cc);
        }

        return tableId;
      } else {
        throw new ODKExternalServiceException(ErrorConsts.ERROR_OBTAINING_FUSION_TABLE_ID);
      }
    } catch (JsonParseException e) {
      logger.error("Failed to create fusion table: " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    } catch (JsonMappingException e) {
      logger.error("Failed to create fusion table: " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    } catch (IOException e) {
      logger.error("Failed to create fusion table: " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    } catch (ServiceException e) {
      logger.error("Failed to create fusion table: " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    }
  }

  private String createFusionTableStatement(IForm form, FormElementModel rootNode) throws JsonGenerationException, JsonMappingException, IOException {

    Map<String,Object> tableResource = new HashMap<String,Object>();
    tableResource.put("kind", "fusiontables#table");
    tableResource.put("name", rootNode.getElementName());
    // round to minutes...
    long nowRounded = 60000L * (System.currentTimeMillis() / 60000L);
    Date d = new Date(nowRounded);
    String timestamp = WebUtils.iso8601Date(d);
    tableResource.put("description", form.getViewableName() + " " + timestamp + " - " + rootNode.getElementName());
    tableResource.put("isExportable", true);

    List<String> headers = headerFormatter.generateHeaders(form, rootNode, null);

    // types are in the same order as the headers...
    List<ElementType> types = headerFormatter.getHeaderTypes();

    List<Map<String,Object>> columnResources = new ArrayList<Map<String,Object>>();
    for ( int i = 0 ; i < headers.size() ; ++i ) {
      String colName = headers.get(i);
      ElementType type = types.get(i);

      Map<String,Object> colResource = new HashMap<String,Object>();
      colResource.put("kind", "fusiontables#column");
      colResource.put("name", colName);
      colResource.put("type", FusionTableConsts.typeMap.get(type).getFusionTypeValue());

      columnResources.add(colResource);
    }
    tableResource.put("columns", columnResources);

    String createStmt = mapper.writeValueAsString(tableResource);

    return createStmt;
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

  private String getAlternateLink(String id) {
    Map<String, String> properties = new HashMap<String, String>();
    if (id.toLowerCase().equals(id.toUpperCase())) {
      properties.put("dsrcid", id);
    } else {
      properties.put("docid", id);
    }
    return HtmlUtil.createLinkWithProperties("http://www.google.com/fusiontables/DataSource", properties);
  }

  @Override
  public String getDescriptiveTargetString() {
    Map<String, String> properties = new HashMap<String, String>();
    String id = objectEntity.getFusionTableId();
    if (id == null) {
      return "Not yet created";
    }
    if (id.toLowerCase().equals(id.toUpperCase())) {
      properties.put("dsrcid", id);
    } else {
      properties.put("docid", id);
    }
    return HtmlUtil.createHrefWithProperties("http://www.google.com/fusiontables/DataSource",
        properties, "View Fusion Table");
  }

  protected CommonFieldsBase retrieveObjectEntity() {
    return objectEntity;
  }

  @Override
  protected List<? extends CommonFieldsBase> retrieveRepeatElementEntities() {
    return repeatElementEntities;
  }
}
