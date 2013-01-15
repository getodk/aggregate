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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
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
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

import com.google.gdata.util.ServiceException;

public abstract class OAuth2ExternalService extends AbstractExternalService {
  protected static ObjectMapper mapper = new ObjectMapper();

  protected static final String FUSION_TABLE_QUERY_API = "https://www.googleapis.com/fusiontables/v1/query";
  protected static final String FUSION_TABLE_TABLE_API = "https://www.googleapis.com/fusiontables/v1/tables";


  // these do not take entity bodies...
  protected static final String DELETE = "DELETE";
  protected static final String GET = "GET";

  // these do...
  protected static final String POST = "POST";
  protected static final String PUT = "PUT";
  protected static final String PATCH = "PATCH";

  // and also share all session cookies and credentials across all sessions...
  // these are thread-safe, so this is OK.
  private static final CookieStore cookieStore = new BasicCookieStore();
  private static final CredentialsProvider credsProvider = new BasicCredentialsProvider();

  protected OAuth2ExternalService(IForm form, FormServiceCursor formServiceCursor, ElementFormatter formatter, HeaderFormatter headerFormatter, CallingContext cc){
    super(form, formServiceCursor, formatter, headerFormatter, cc);
  }

  /**
   * This method should cache the accessToken in memory and only access or update the database if
   * the forceRefresh parameter is true, in which case it should trigger getOAuth2AccessToken
   * to obtain a new token.
   *
   * @param forceRefresh
   * @param cc
   * @return
   * @throws ODKExternalServiceCredentialsException
   */
  protected abstract String getAccessToken(boolean forceRefresh, CallingContext cc) throws ODKExternalServiceCredentialsException;

  /**
   * Requests an access token for the given scopes.
   *
   * Precondition: a service account and private key have been configured for
   * the server.
   *
   * @param scopes
   * @param cc
   * @return access token (must be a Bearer token).
   * @throws ODKEntityNotFoundException
   * @throws ODKOverQuotaException
   */
  protected String getOAuth2AccessToken(String scopes, CallingContext cc)
      throws ODKEntityNotFoundException, ODKOverQuotaException {

    String serviceEmailAddress = ServerPreferencesProperties.getServerPreferencesProperty(cc,
        ServerPreferencesProperties.GOOGLE_API_SERVICE_ACCOUNT_EMAIL);
    String privateKeyString = ServerPreferencesProperties.getServerPreferencesProperty(cc,
        ServerPreferencesProperties.PRIVATE_KEY_FILE_CONTENTS);

    if (serviceEmailAddress == null || privateKeyString == null
        || serviceEmailAddress.length() == 0 || privateKeyString.length() == 0) {
      throw new IllegalArgumentException(
          "No OAuth2 credentials. Have you supplied any OAuth2 credentials on the Site Admin / Preferences page?");
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
      claimSetValueString = Base64.encodeBase64URLSafeString(claimSetValue);
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

      Enumeration<String> aliasEnum = null;
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
        throw new IllegalArgumentException("Unexpected UnsupportedEncodingException "
            + e.toString());
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
    HttpConnectionParams.setConnectionTimeout(httpParams, SERVICE_TIMEOUT_MILLISECONDS);
    HttpConnectionParams.setSoTimeout(httpParams, SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS);
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
        throw new IllegalArgumentException("Error with Oauth2 token request - reason: "
            + response.getStatusLine().getReasonPhrase() + " status code: " + statusCode);
      } else {
        HttpEntity entity = response.getEntity();

        if (entity != null && entity.getContentType().getValue().toLowerCase().contains("json")) {
          ObjectMapper mapper = new ObjectMapper();
          BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
          @SuppressWarnings("unchecked")
          Map<String, Object> userData = mapper.readValue(reader, Map.class);

          String accessToken = (String) userData.get("access_token");
          String tokenType = (String) userData.get("token_type");

          if ("Bearer".equals(tokenType)) {
            return accessToken;
          }
          throw new IllegalArgumentException(
              "Error with Oauth2 token request - token_type is not Bearer: " + tokenType);
        } else {
          throw new IllegalArgumentException("Error with Oauth2 token request - missing body");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e.toString());
    }

  }

  protected String executeDrivePermission(String tableId, String email, Log logger, String typeQualifierForErrorMessage, CallingContext cc)
      throws ODKExternalServiceException {
    String resultRequest;
    try {
        // NOTE: tableId is also the Google Drive fileId

        // obtain the set of permissions on Google Drive
        resultRequest = executeStmt(GET, "https://www.googleapis.com/drive/v2/files/" + URLEncoder.encode(tableId, "UTF-8") + "/permissions", null, null, cc);
        @SuppressWarnings("unchecked")
        Map<String,Object> result = mapper.readValue(resultRequest, Map.class);

        boolean found = false;
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) result.get("items");
        for ( Object item : items ) {
          @SuppressWarnings("unchecked")
          Map<String,Object> perm = (Map<String,Object>) item;
          if ( perm.get("name").equals(email.substring(SecurityUtils.MAILTO_COLON.length())) ) {
            found = true;
            break;
          }
        }

        if ( !found ) {
          Map<String,Object> newPerm = new HashMap<String,Object>();
          newPerm.put("kind", "drive#permission");
          newPerm.put("role", "owner");
          newPerm.put("type", "user");
          newPerm.put("value", email.substring(SecurityUtils.MAILTO_COLON.length()));
          String body = mapper.writeValueAsString(newPerm);
          resultRequest = executeStmt(POST, "https://www.googleapis.com/drive/v2/files/" + URLEncoder.encode(tableId, "UTF-8") + "/permissions",body, null, cc);
        }

        return tableId;
    } catch (JsonParseException e) {
      logger.error("Failed to grant permissions on " + typeQualifierForErrorMessage + ": " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    } catch (JsonMappingException e) {
      logger.error("Failed to grant permissions on " + typeQualifierForErrorMessage + ": " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    } catch (IOException e) {
      logger.error("Failed to grant permissions on " + typeQualifierForErrorMessage + ": " + e.getMessage());
      e.printStackTrace();
      throw new ODKExternalServiceException(e);
    } catch (ServiceException e) {
      logger.error("Failed to grant permissions on " + typeQualifierForErrorMessage + ": " + e.getMessage());
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
  protected String executeStmt(String method, String tablesUrl, String statement, List<NameValuePair> qparams, CallingContext cc) throws ServiceException,
      IOException, ODKExternalServiceException {

    // get the access token
    String accessToken = getAccessToken(false, cc);

    try {
      return coreExecuteStmt( method, tablesUrl, statement, qparams, accessToken, cc);
    } catch ( ODKExternalServiceCredentialsException e) {
      try {
        // refresh the access token
        accessToken = getAccessToken(true, cc);
      } catch (Exception e1) {
        throw new ODKExternalServiceCredentialsException("Unable to obtain OAuth2 access token: "
            + e1.toString());
      }
      // try again...
      return coreExecuteStmt( method, tablesUrl, statement, qparams, accessToken, cc);
    }
  }

  private String coreExecuteStmt(String method, String tablesUrl, String statement, List<NameValuePair> qparams, String accessToken, CallingContext cc) throws ServiceException,
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
      uri = new URI( nakedUri.getScheme(), nakedUri.getUserInfo(), nakedUri.getHost(),
          nakedUri.getPort(), nakedUri.getPath(), URLEncodedUtils.format(qparams, "UTF-8"), null);
    } catch (URISyntaxException e1) {
      e1.printStackTrace();
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
        // the alternative -- using ContentType.create(,) throws an exception???
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

}
