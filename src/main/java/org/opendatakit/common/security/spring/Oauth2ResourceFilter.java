/*
 * Copyright (C) 2012 University of Washington
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
package org.opendatakit.common.security.spring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.opendatakit.common.utils.HttpClientFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * If the session does not already have an Authentication element,
 * this filter looks for an Oauth 2.0 access token that gives access
 * to a user's Google E-mail address (i.e., has a userinfo.email scope).
 * This is considered sufficient to assume the user's identity provided
 * the 'Enable Tokens' checkbox is checked.
 *
 * This e-mail needs to be in the registered
 * users table and assigned permissions.
 *
 * Ideally, we would have a custom scope in the Google Oauth 2.0 grant
 * for this Aggregate instance.  That is not yet possible.
 *
 * parseToken() is copied verbatim from Spring Security Oauth2 code.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class Oauth2ResourceFilter extends GenericFilterBean {

  private static final int SERVICE_TIMEOUT_MILLISECONDS = 30000;
  private static final int SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS = 60000;
  private static final String ACCESS_TOKEN = "access_token";
  private static final String BEARER_TYPE = "Bearer";

  private Log logger = LogFactory.getLog(Oauth2ResourceFilter.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  private AuthenticationProvider authenticationProvider = null;
  private HttpClientFactory httpClientFactory = null;

  private static Map<String, Oauth2AuthenticationToken> authTokenMap
      = new HashMap<String, Oauth2AuthenticationToken>();

  private static synchronized Oauth2AuthenticationToken lookupToken(String accessToken) {

    Oauth2AuthenticationToken ti = authTokenMap.get(accessToken);
    if ( ti != null && ( ti.getExpiration().compareTo(new Date()) < 0 )) {
        // expired...
        authTokenMap.remove(ti);
        ti = null;
    }

    return ti;
  }

  private static synchronized void insertToken(Oauth2AuthenticationToken token) {
    authTokenMap.put(token.getAccessToken(), token);
  }

  public Oauth2ResourceFilter() {
    super();
  }

  public AuthenticationProvider getAuthenticationProvider() {
    return authenticationProvider;
  }

  public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
  }

  public HttpClientFactory getHttpClientFactory() {
    return httpClientFactory;
  }

  public void setHttpClientFactory(HttpClientFactory factory) {
    httpClientFactory = factory;
  }

  @Override
  public void afterPropertiesSet() throws ServletException {
    super.afterPropertiesSet();
    if ( httpClientFactory == null ) {
      throw new IllegalStateException("httpClientFactory must be defined");
   }
   if ( authenticationProvider == null ) {
      throw new IllegalStateException("authenticationProvider must be defined");
   }
  }

  protected String parseToken(HttpServletRequest request) {
    // first check the header...
    String token = parseHeaderToken(request);

    // bearer type allows a request parameter as well
    if (token == null) {
       logger.debug("Token not found in headers. Trying request parameters.");
       token = request.getParameter(ACCESS_TOKEN);
       if (token == null) {
          logger.debug("Token not found in request parameters.  Not an OAuth2 request.");
       }
    }

    return token;
 }

 /**
  * Parse the OAuth header parameters. The parameters will be oauth-decoded.
  *
  * @param request The request.
  * @return The parsed parameters, or null if no OAuth authorization header was supplied.
  */
 protected String parseHeaderToken(HttpServletRequest request) {
    Enumeration<String> headers = request.getHeaders("Authorization");
    while (headers.hasMoreElements()) {
       String value = headers.nextElement();
       if ((value.toLowerCase().startsWith(BEARER_TYPE.toLowerCase()))) {
          String authHeaderValue = value.substring(BEARER_TYPE.length()).trim();

          if (authHeaderValue.contains("oauth_signature_method") || authHeaderValue.contains("oauth_verifier")) {
             // presence of oauth_signature_method or oauth_verifier implies an oauth 1.x request
             continue;
          }

          int commaIndex = authHeaderValue.indexOf(',');
          if (commaIndex > 0) {
             authHeaderValue = authHeaderValue.substring(0, commaIndex);
          }

          // todo: parse any parameters...

          return authHeaderValue;
       }
       else {
          // todo: support additional authorization schemes for different token types, e.g. "MAC" specified by
          // http://tools.ietf.org/html/draft-hammer-oauth-v2-mac-token
       }
    }

    return null;
 }

 private Map<String,Object> getJsonResponse(String url, String accessToken) {

   Map<String,Object> nullData = new HashMap<String,Object>();

   // OK if we got here, we have a valid token.
   // Issue the request...
     URI nakedUri;
     try {
       nakedUri = new URI(url);
     } catch (URISyntaxException e2) {
       e2.printStackTrace();
       logger.error(e2.toString());
       return nullData;
     }

     List<NameValuePair> qparams = new ArrayList<NameValuePair>();
     qparams.add(new BasicNameValuePair("access_token", accessToken));
     URI uri;
     try {
       uri = new URI( nakedUri.getScheme(), nakedUri.getUserInfo(), nakedUri.getHost(),
           nakedUri.getPort(), nakedUri.getPath(), URLEncodedUtils.format(qparams, "UTF-8"), null);
     } catch (URISyntaxException e1) {
       e1.printStackTrace();
       logger.error(e1.toString());
       return nullData;
     }

     // DON'T NEED clientId on the toke request...
     // addCredentials(clientId, clientSecret, nakedUri.getHost());
     // setup request interceptor to do preemptive auth
     // ((DefaultHttpClient) client).addRequestInterceptor(getPreemptiveAuth(), 0);

     // setup client
     SocketConfig socketConfig = SocketConfig.copy(SocketConfig.DEFAULT)
         .setSoTimeout(SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS)
         .build();
     RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
         .setConnectTimeout(SERVICE_TIMEOUT_MILLISECONDS)
         .setAuthenticationEnabled(true)
         .setRedirectsEnabled(true)
         .setMaxRedirects(1)
         .setCircularRedirectsAllowed(true)
         .build();
     CloseableHttpClient client = httpClientFactory.createHttpClient(socketConfig, null, requestConfig);

     HttpGet httpget = new HttpGet(uri);
     logger.info(httpget.getURI().toString());

     HttpResponse response = null;
     try {
         response = client.execute(httpget, new BasicHttpContext());
         int statusCode = response.getStatusLine().getStatusCode();

         if ( statusCode != HttpStatus.SC_OK ) {
           logger.error("not 200: " + statusCode);
           return nullData;
         } else {
           HttpEntity entity = response.getEntity();

           if (entity != null && entity.getContentType().getValue().toLowerCase()
                           .contains("json")) {
             BufferedReader reader = null;
             InputStreamReader isr = null;
             try {
               reader = new BufferedReader(isr = new InputStreamReader(entity.getContent(), CharEncoding.UTF_8));
               @SuppressWarnings("unchecked")
			   Map<String,Object> userData = mapper.readValue(reader, Map.class);
               return userData;
             } finally {
               if ( reader != null ) {
                 try {
                   reader.close();
                 } catch ( IOException e ) {
                   // ignore
                 }
               }
               if ( isr != null ) {
                 try {
                   isr.close();
                 } catch ( IOException e ) {
                   // ignore
                 }
               }
             }
           } else {
             logger.error("unexpected body");
             return nullData;
           }
         }
     } catch ( IOException e ) {
       logger.error(e.toString());
       return nullData;
     } catch ( Exception e ) {
       logger.error(e.toString());
       return nullData;
     }
 }

 private Oauth2AuthenticationToken assertToken(String accessToken) {

   Oauth2AuthenticationToken ti = lookupToken(accessToken);
   if ( ti != null ) {
     return ti;
   }

   Map<String,Object> responseData;

   responseData = getJsonResponse("https://www.googleapis.com/oauth2/v1/tokeninfo", accessToken);

   Integer expiresInSeconds = (Integer) responseData.get("expires_in");
   if ( expiresInSeconds == null || expiresInSeconds == 0 ) {
     return null;
   }
   Date deadline = null;
   deadline = new Date( System.currentTimeMillis() + 1000L*expiresInSeconds);

   String email = (String) responseData.get("email");
   if ( email == null ) {
     responseData = getJsonResponse("https://www.googleapis.com/oauth2/v1/userinfo", accessToken);

     email = (String) responseData.get("email");
     if ( email == null ) {
       return null;
     }
   }

   ti = new Oauth2AuthenticationToken( accessToken, email, deadline);
   insertToken(ti);
   return ti;
 }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    if ( SecurityContextHolder.getContext().getAuthentication() == null ) {
      try {
        String accessToken = parseToken(request);

        if (accessToken != null) {
          Oauth2AuthenticationToken token = assertToken(accessToken);

          if ( token != null ) {
            // In the common case, if token is non-null, the user should be known to the server.
            Authentication auth =
                authenticationProvider.authenticate(token);

            SecurityContextHolder.getContext().setAuthentication(auth);
          }
        }
      } catch ( AuthenticationException ex ) {
        // if the authentication fails to recognize the user, silently ignore the failure.
        // Warnings were already logged by the AuthenticationProvider.
      }
    }

    chain.doFilter(request, response);
  }

}
