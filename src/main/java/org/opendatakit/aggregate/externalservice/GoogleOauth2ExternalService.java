/*
 * Copyright (C) 2013 University of Washington
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.http.NameValuePair;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

/**
 * Refactoring and base implementation using the new gdata APIs for accessing
 * Google Spreadsheet, Fusion Table and Map Engine.
 *
 * @author wbrunette@gmail.com
 *
 */
public abstract class GoogleOauth2ExternalService extends AbstractExternalService {

  private static final String NO_EMAIL_SPECIFIED_ERROR = "No email specified to add file permission to";
  private static final String NO_PERM_RETURNED = "GOT No permssion returned in the response";

  private static final JsonFactory jsonFactory = new JacksonFactory();

  protected GoogleCredential credential;
  protected HttpTransport httpTransport;

  protected final Log oauth2logger;

  protected HttpRequestFactory requestFactory;

  protected GoogleOauth2ExternalService(String credentialScope, IForm form,
      FormServiceCursor formServiceCursor, ElementFormatter formatter,
      HeaderFormatter headerFormatter, Log logger, CallingContext cc)
      throws ODKExternalServiceCredentialsException, ODKExternalServiceException {
    super(form, formServiceCursor, formatter, headerFormatter, cc);

    this.oauth2logger = logger;

    try {
      this.credential = getCredential(credentialScope, cc);
      try {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.requestFactory = httpTransport.createRequestFactory(credential);
      } catch (GeneralSecurityException e) {
        throw new ODKExternalServiceCredentialsException(e);
      } catch (IOException e) {
        throw new ODKExternalServiceException(e);
      }

    } catch (ODKExternalServiceCredentialsException e) {
      this.credential = null;
      this.httpTransport = null;
      this.requestFactory = null;
      OperationalStatus currentStatus = fsc.getOperationalStatus();
      if (currentStatus == OperationalStatus.ACTIVE
          || currentStatus == OperationalStatus.ACTIVE_RETRY) {
        fsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
        try {
          Datastore ds = cc.getDatastore();
          User user = cc.getCurrentUser();
          ds.putEntity(fsc, user);
        } catch (Exception e1) {
          oauth2logger.error("Unable to persist bad credentials status" + e1.toString());
          throw new ODKExternalServiceException("unable to persist bad credentials status", e1);
        }
      }
      throw e;
    }

  }

  protected static GoogleCredential getCredential(String scopes, CallingContext cc)
      throws ODKExternalServiceCredentialsException {
    try {
      String serviceAccountUser = ServerPreferencesProperties.getServerPreferencesProperty(cc,
          ServerPreferencesProperties.GOOGLE_API_SERVICE_ACCOUNT_EMAIL);
      String privateKeyString = ServerPreferencesProperties.getServerPreferencesProperty(cc,
          ServerPreferencesProperties.PRIVATE_KEY_FILE_CONTENTS);

      if (serviceAccountUser == null || privateKeyString == null
          || serviceAccountUser.length() == 0 || privateKeyString.length() == 0) {
        throw new ODKExternalServiceCredentialsException(
            "No OAuth2 credentials. Have you supplied any OAuth2 credentials on the Site Admin / Preferences page?");
      }

      byte[] privateKeyBytes = Base64.decodeBase64(privateKeyString.getBytes(UTF_CHARSET));

      // TODO: CHANGE TO MORE OPTIMAL METHOD
      KeyStore ks = null;
      ks = KeyStore.getInstance("PKCS12");
      ks.load(new ByteArrayInputStream(privateKeyBytes), "notasecret".toCharArray());
      Enumeration<String> aliasEnum = null;
      aliasEnum = ks.aliases();

      Key key = null;
      while (aliasEnum.hasMoreElements()) {
        String keyName = (String) aliasEnum.nextElement();
        key = ks.getKey(keyName, "notasecret".toCharArray());
        break;
      }
      PrivateKey serviceAccountPrivateKey = (PrivateKey) key;

      HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
          .setJsonFactory(jsonFactory).setServiceAccountId(serviceAccountUser)
          .setServiceAccountScopes(Collections.singleton(scopes))
          .setServiceAccountPrivateKey(serviceAccountPrivateKey).build();
      credential.refreshToken();
      return credential;
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKExternalServiceCredentialsException(e);
    }
  }

  protected Drive getGoogleDrive() {
    return new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName(
        ServletConsts.APPLICATION_NAME).build();
  }

  protected void executeDrivePermission(Drive drive, String fileId, String email)
      throws ODKExternalServiceException {

    oauth2logger.info("Switching file permissions");

    if (email == null) {
      throw new ODKExternalServiceException(NO_EMAIL_SPECIFIED_ERROR);
    }

    try {
      String userName = email.substring(SecurityUtils.MAILTO_COLON.length());

      Permission newPermission = new Permission();
      newPermission.setKind("drive#permission");
      newPermission.setRole("owner");
      newPermission.setType("user");
      newPermission.setEmailAddress(userName);

      // NOTE: Dropped the check because name was not a good value to compare

      Drive.Permissions.Create createPerm = drive.permissions().create(fileId, newPermission);
      // ownership transfer now requires notification e-mail
      // createPerm.setSendNotificationEmail(false);
      createPerm.setTransferOwnership(true);
      Permission response = createPerm.execute();

      if (response == null) {
        oauth2logger.error(NO_PERM_RETURNED);
        throw new ODKExternalServiceException(NO_PERM_RETURNED);
      }

    } catch (IOException e) {
      throw new ODKExternalServiceException(e);
    }
  }

  protected void executeDrivePermission(String fileId, String email)
      throws ODKExternalServiceException {
    executeDrivePermission(getGoogleDrive(), fileId, email);
  }

  protected String executeStmt(String method, String urlString, String statement,
      List<NameValuePair> qparams, boolean isFTQuery, CallingContext cc) throws 
      IOException, ODKExternalServiceException, GeneralSecurityException {

    if (statement == null && (POST.equals(method) || PATCH.equals(method) || PUT.equals(method))) {
      throw new ODKExternalServiceException("No body supplied for POST, PATCH or PUT request");
    } else if (statement != null
        && !(POST.equals(method) || PATCH.equals(method) || PUT.equals(method))) {
      throw new ODKExternalServiceException("Body was supplied for GET or DELETE request");
    }

    GenericUrl url = new GenericUrl(urlString);
    if (qparams != null) {
      for (NameValuePair param : qparams) {
        url.set(param.getName(), param.getValue());
      }
    }

    HttpContent entity = null;
    if (statement != null) {
      if (isFTQuery) {
        Map<String, String> formContent = new HashMap<String, String>();
        formContent.put("sql", statement);
        UrlEncodedContent urlEntity = new UrlEncodedContent(formContent);
        entity = urlEntity;
        HttpMediaType t = urlEntity.getMediaType();
        if (t != null) {
          t.setCharsetParameter(Charset.forName(HtmlConsts.UTF8_ENCODE));
        } else {
          t = new HttpMediaType("application", "x-www-form-urlencoded");
          t.setCharsetParameter(Charset.forName(HtmlConsts.UTF8_ENCODE));
          urlEntity.setMediaType(t);
        }
      } else {
        // the alternative -- using ContentType.create(,) throws an exception???
        // entity = new StringEntity(statement, "application/json", UTF_8);
        entity = new ByteArrayContent("application/json",
            statement.getBytes(HtmlConsts.UTF8_ENCODE));
      }
    }

    HttpRequest request = requestFactory.buildRequest(method, url, entity);
    HttpResponse resp = request.execute();
    String response = WebUtils.readGoogleResponse(resp);

    int statusCode = resp.getStatusCode();
    if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
      throw new ODKExternalServiceCredentialsException(response.toString() + statement);
    } else if (statusCode != HttpServletResponse.SC_OK) {
      throw new ODKExternalServiceException(response.toString() + statement);
    }

    return response;
  }

}
