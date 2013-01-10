/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.constants.externalservice.SpreadsheetConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.FusionTable;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.externalservice.OhmageJsonServer;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.servlet.OAuth2Servlet;
import org.opendatakit.aggregate.servlet.OAuthServlet;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServicesAdminServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.aggregate.client.externalserv.ServicesAdminService {

  /**
	 *
	 */
  private static final long serialVersionUID = 51251316598366231L;

  @Override
  public ExternServSummary[] getExternalServices(String formId) throws AccessDeniedException,
      FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    if (formId.equals(BasicConsts.EMPTY_STRING))
      return null;

    try {
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      List<ExternalService> esList = FormServiceCursor.getExternalServicesForForm(form, cc);

      ExternServSummary[] externServices;
      if (esList.size() > 0) {
        externServices = new ExternServSummary[esList.size()];

        for (int i = 0; i < esList.size(); i++) {
          externServices[i] = esList.get(i).transform();
        }

        return externServices;

      } else {
        return null;
      }

    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  @Override
  public String generateOAuthUrl(String uri) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {

      String scope = null;
      FormServiceCursor fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
      switch (fsc.getExternalServiceType()) {
      case GOOGLE_FUSIONTABLES:
        scope = FusionTableConsts.FUSION_SCOPE;
        break;
      case GOOGLE_SPREADSHEET:
        scope = SpreadsheetConsts.DOCS_SCOPE + BasicConsts.SPACE
            + SpreadsheetConsts.SPREADSHEETS_SCOPE;
        break;
      default:
        break;
      }

      // make sure a scope was determined before proceeding
      if (scope == null) {
        return null;
      }

      GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
      oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
      oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
      oauthParameters.setScope(scope);

      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
      oauthHelper.getUnauthorizedRequestToken(oauthParameters);
      Map<String, String> params = new HashMap<String, String>();
      params.put(UIConsts.FSC_URI_PARAM, uri);
      params.put(ServletConsts.OAUTH_TOKEN_SECRET_PARAMETER, oauthParameters.getOAuthTokenSecret());

      String addr = cc.getServerURL() + BasicConsts.FORWARDSLASH + OAuthServlet.ADDR;
      String callbackUrl = HtmlUtil.createLinkWithProperties(addr, params);

      oauthParameters.setOAuthCallback(callbackUrl);
      return oauthHelper.createUserAuthorizationUrl(oauthParameters);

    } catch (OAuthException e) {
      e.printStackTrace();
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
    return null;
  }


  public String generateOAuth2Url(String uri) throws RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {

      String scope = null;
      FormServiceCursor fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
      switch (fsc.getExternalServiceType()) {
      case GOOGLE_FUSIONTABLES:
        scope = FusionTableConsts.FUSION_SCOPE;
        break;
      case GOOGLE_SPREADSHEET:
        scope = SpreadsheetConsts.DOCS_SCOPE + BasicConsts.SPACE
            + SpreadsheetConsts.SPREADSHEETS_SCOPE;
        break;
      default:
        break;
      }

      // make sure a scope was determined before proceeding
      if (scope == null) {
        return null;
      }

      GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
      oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
      oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
      oauthParameters.setScope(scope);

      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
      oauthHelper.getUnauthorizedRequestToken(oauthParameters);
      Map<String, String> params = new HashMap<String, String>();
      params.put(UIConsts.FSC_URI_PARAM, uri);
      params.put(ServletConsts.OAUTH_TOKEN_SECRET_PARAMETER, oauthParameters.getOAuthTokenSecret());

      String addr = cc.getServerURL() + BasicConsts.FORWARDSLASH + OAuth2Servlet.ADDR;
      String callbackUrl = HtmlUtil.createLinkWithProperties(addr, params);

      oauthParameters.setOAuthCallback(callbackUrl);
      return oauthHelper.createUserAuthorizationUrl(oauthParameters);

    } catch (OAuthException e) {
      e.printStackTrace();
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
    return null;
  }

  @Override
  public String createFusionTable(String formId, ExternalServicePublicationOption esOption, String ownerEmail)
      throws AccessDeniedException, FormNotAvailableException, RequestFailureException,
      DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      // Form is being deleted. Disallow exports.
      if (deletionTimestamp != null) {
        throw new RequestFailureException(
            "Form is marked for deletion - publishing request for fusion table aborted.");
      }
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
      }
      if ( ownerEmail == null || ownerEmail.length() == 0 ) {
        throw new RequestFailureException(
            "Owner email must be supplied.  Are you logged in through a gmail account?");
      }
      FusionTable fusion = new FusionTable(form, esOption, ownerEmail, cc);
      fusion.initiate(cc);
      return fusion.getFormServiceCursor().getUri();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public String createGoogleSpreadsheet(String formId, String name,
      ExternalServicePublicationOption esOption) throws AccessDeniedException,
      FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      // Form is being deleted. Disallow exports.
      if (deletionTimestamp != null) {
        throw new RequestFailureException(
            "Form is marked for deletion - publishing request for fusion table aborted.");
      }
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
      }
      GoogleSpreadsheet spreadsheet = new GoogleSpreadsheet(form, name, esOption, cc);
      return spreadsheet.getFormServiceCursor().getUri();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public String createOhmageJsonServer(String formId, String url,
        ExternalServicePublicationOption esOption)
            throws AccessDeniedException,
            FormNotAvailableException, RequestFailureException, DatastoreFailureException {
     HttpServletRequest req = this.getThreadLocalRequest();
     CallingContext cc = ContextFactory.getCallingContext(this, req);

     try {
        FormActionStatusTimestamp deletionTimestamp = MiscTasks
              .getFormDeletionStatusTimestampOfFormId(formId, cc);
        if (deletionTimestamp != null) {
          throw new RequestFailureException(
              "Form is marked for deletion - publishing request for Ohmage JSON server aborted.");
        }
        IForm form = FormFactory.retrieveFormByFormId(formId, cc);
        if (!form.hasValidFormDefinition()) {
          throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID);
        }
        OhmageJsonServer server = new OhmageJsonServer(form, url, esOption, cc);
        return server.getFormServiceCursor().getUri();
     } catch (ODKOverQuotaException e) {
       e.printStackTrace();
       throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
     } catch (ODKFormNotFoundException e) {
       e.printStackTrace();
       throw new FormNotAvailableException(e);
     } catch (ODKDatastoreException e) {
       e.printStackTrace();
       throw new DatastoreFailureException(e);
     }
  }

  @Override
  public Boolean deletePublisher(String uri) throws AccessDeniedException,
      FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    FormServiceCursor fsc = null;
    ExternalService es = null;
    try {
      fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
      if (fsc != null) {
        es = fsc.getExternalService(cc);
      }
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch ( ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException("Publisher not found");
    } catch ( ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }

    if (es != null) {
      try {
        es.delete(cc);
        // success!
        return true;
      } catch (ODKOverQuotaException e) {
        e.printStackTrace();
        throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
      } catch ( ODKDatastoreException e) {
        e.printStackTrace();
        throw new DatastoreFailureException(e);
      }
    }
    return false;
  }

  @Override
  public String refreshCredentials(String uri) throws AccessDeniedException,
      FormNotAvailableException, RequestFailureException, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    FormServiceCursor fsc = null;
    ExternalService es = null;
    try {
      fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
      if (fsc != null) {
        es = fsc.getExternalService(cc);
      }
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch ( ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException("Publisher not found");
    } catch ( ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }

    if (es == null) {
      throw new RequestFailureException("Service description not found for this publisher");
    }
    if ( fsc.getOperationalStatus() != OperationalStatus.BAD_CREDENTIALS ) {
      throw new RequestFailureException(
          "Credentials have not failed for this publisher -- rejecting change request");
    }

    // generate the URL for the auth sequence
    return generateOAuthUrl(uri);
  }
}
