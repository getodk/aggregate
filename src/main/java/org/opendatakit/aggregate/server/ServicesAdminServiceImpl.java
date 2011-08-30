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
import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.constants.externalservice.SpreadsheetConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.FusionTable;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.servlet.OAuthServlet;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

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
  public ExternServSummary[] getExternalServices(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    if(formId.equals(BasicConsts.EMPTY_STRING))
      return null;
    
    try {
      Form form = Form.retrieveFormByFormId(formId, cc);
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

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String generateOAuthUrl(String uri) {
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
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public String createFusionTable(String formId, ExternalServicePublicationOption esOption) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks.getFormDeletionStatusTimestampOfFormId(formId, cc);
      // TODO: better error reporting -- form is being deleted. Disallow creation of publishers.
      if ( deletionTimestamp != null ) return null;
      Form form = Form.retrieveFormByFormId(formId, cc);
	  if ( form.getFormDefinition() == null ) return null; // ill-formed definition
      FusionTable fusion = new FusionTable(form, esOption, cc);
      return fusion.getFormServiceCursor().getUri();
    } catch (ODKFormNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public String createGoogleSpreadsheet(String formId, String name, ExternalServicePublicationOption esOption) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks.getFormDeletionStatusTimestampOfFormId(formId, cc);
      // TODO: better error reporting -- form is being deleted. Disallow creation of publishers.
      if ( deletionTimestamp != null ) return null;
      Form form = Form.retrieveFormByFormId(formId, cc);
	  if ( form.getFormDefinition() == null ) return null; // ill-formed definition
      GoogleSpreadsheet spreadsheet = new GoogleSpreadsheet(form, name, esOption, cc);
      return spreadsheet.getFormServiceCursor().getUri();
    } catch (ODKFormNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Boolean deletePublisher(String uri) throws AccessDeniedException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

	FormServiceCursor fsc = null;
	ExternalService es = null;
	try {
		fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
		if ( fsc != null ) {
			es = fsc.getExternalService(cc);
		}
	} catch (ODKEntityNotFoundException e) {
		// silent failure...
		return false;
	}

	if ( es != null ) {
		try {
			es.delete(cc);
			// success!
			return true; 
		} catch (ODKDatastoreException e) {
			// this one we log...
			e.printStackTrace();
			return false;
		}
	}
	return false;
  }
}
