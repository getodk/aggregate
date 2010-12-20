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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceAuthenticationError;
import org.opendatakit.aggregate.exception.ODKExternalServiceNotAuthenticated;
import org.opendatakit.aggregate.externalservice.OAuthToken;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.servlet.CommonServletBase;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

/**
 * Base class for Servlets that contain useful utilities
 * 
 */
@SuppressWarnings("serial")
public class ServletUtilBase extends CommonServletBase {
	
  protected ServletUtilBase() {
    super(ServletConsts.APPLICATION_NAME);
  }

  /**
   * Takes request and verifies the user has logged in. If the user has not
   * logged in generates the appropriate text for response to user
   * 
   * @param req
   *          The HTTP request received at the server
   * @param resp
   *          The HTTP response to be sent to client
   * @return boolean value of whether the user is logged in
   * @throws IOException
   *           Throws IO Exception if problem occurs creating the login link in
   *           response
   */
  protected boolean verifyCredentials(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = (UserService) ContextFactory.get().getBean(BeanDefs.USER_BEAN);
    return verifyCredentials(req, resp, userService);
  }

  @Override
  protected void emitPageHeader(PrintWriter out, HttpServletRequest req, boolean displayLinks) {
    if (displayLinks) {
      out.write(generateNavigationInfo(req));
      out.write(HtmlConsts.TAB + HtmlConsts.TAB);

      UserService userService = (UserService) ContextFactory.get().getBean(BeanDefs.USER_BEAN);
      out.write(HtmlUtil.createHref(appBasePath(req) + userService.createLogoutURL(ServletConsts.WEB_ROOT), LOGOUT
          + userService.getCurrentUser().getNickname()));
      out.write(HtmlConsts.TAB + "<FONT SIZE=1>" + ServletConsts.VERSION + "</FONT>");
    }
  }
  
  /**
   * Generate error response for ODK ID not found
   * 
   * @param resp
   *          The HTTP response to be sent to client
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void odkIdNotFoundError(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_NOT_FOUND, ErrorConsts.ODKID_NOT_FOUND);
  }

  /**
   * Generate error response for missing the Key parameter
   * 
   * @param resp
   *          The HTTP response to be sent to client
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void errorMissingKeyParam(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.ODK_KEY_PROBLEM);
  }

  /**
   * Generate error response for invalid parameters
   * 
   * @param resp
   *          The HTTP response to be sent to client
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void errorBadParam(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.INVALID_PARAMS);
  }

  /**
   * Generate error response for missing the Key parameter
   * 
   * @param resp
   *          The HTTP response to be sent to client
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void errorRetreivingData(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.INCOMPLETE_DATA);
  }

  /**
   * Generate common navigation links
 * @param req 
   * 
   * @return a string with href links
   */
  public final String generateNavigationInfo(HttpServletRequest req) {
    StringBuilder html = new StringBuilder();
    html.append(HtmlUtil.createHref(appBasePath(req) + FormsServlet.ADDR, ServletConsts.FORMS_LINK_TEXT));
    html.append(HtmlConsts.TAB + HtmlConsts.TAB);
    html.append(HtmlUtil.createHref(appBasePath(req) + ResultServlet.ADDR, ServletConsts.RESULT_FILES_LINK_TEXT));
    html.append(HtmlConsts.TAB + HtmlConsts.TAB);
    html.append(HtmlUtil.createHref(appBasePath(req) + ExternalServicesListServlet.ADDR, ServletConsts.EXTERNAL_SERVICES_LINK_TEXT));
    html.append(HtmlConsts.TAB + HtmlConsts.TAB);
    html.append(HtmlUtil.createHref(appBasePath(req) + UploadAppletServlet.ADDR, ServletConsts.UPLOAD_APPLET_LINK_TEXT));
    html.append(HtmlConsts.TAB + HtmlConsts.TAB);
    html.append(HtmlUtil.createHref(appBasePath(req) + FormUploadServlet.ADDR, ServletConsts.UPLOAD_FORM_LINK_TEXT));
    html.append(HtmlConsts.TAB + HtmlConsts.TAB);
    html.append(HtmlUtil.createHref(appBasePath(req) + FormDeleteServlet.ADDR, ServletConsts.DELETE_FORM_LINK_TEXT));
    html.append(HtmlConsts.TAB + HtmlConsts.TAB);
    html.append(HtmlUtil.createHref(appBasePath(req) + SubmissionServlet.ADDR, ServletConsts.UPLOAD_SUB_LINK_TEXT));
    return html.toString();
  }

  protected OAuthToken verifyGDataAuthorization(HttpServletRequest req, HttpServletResponse resp) 
  		throws IOException, ODKExternalServiceAuthenticationError, ODKExternalServiceNotAuthenticated {
	  
		boolean receivingToken = getParameter(req, ServletConsts.OAUTH_TOKEN_PARAMETER) != null;
		if (receivingToken)
		{
		  	GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
			oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
			oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
			GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
			oauthHelper.getOAuthParametersFromCallback(req.getQueryString(), oauthParameters);
			try {
				oauthHelper.getAccessToken(oauthParameters);
			} catch (OAuthException e) {
		        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		        		ErrorConsts.OAUTH_SECURITY_ERROR_WHILE_RETRIEVING_SESSION_TOKEN);
				throw new ODKExternalServiceAuthenticationError();
			}
			
			return new OAuthToken(oauthParameters.getOAuthToken(), oauthParameters.getOAuthTokenSecret());
		}
		else
		{
			return null;
		}
  }

  protected String generateAuthButton(String buttonText, Map<String, String> params,
      HttpServletRequest req, HttpServletResponse resp, String... scopes) throws IOException {

	GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
	oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
	oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
	String scope = BasicConsts.EMPTY_STRING;
	for (String singleScope : scopes)
	{
		scope += singleScope + BasicConsts.SPACE;
	}
	oauthParameters.setScope(scope);
	
	GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
	try 
	{
		oauthHelper.getUnauthorizedRequestToken(oauthParameters);
	} 
	catch (OAuthException e) 
	{
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ErrorConsts.OAUTH_SERVER_REJECTED_ONE_TIME_USE_TOKEN);
	}

	params.put(ServletConsts.OAUTH_TOKEN_SECRET_PARAMETER, oauthParameters.getOAuthTokenSecret());
	String callbackUrl = ServletConsts.HTTP
		+ HtmlUtil.createLinkWithProperties(getServerURL(req) + req.getServletPath(), params);
	oauthParameters.setOAuthCallback(callbackUrl);
	String requestUrl = oauthHelper.createUserAuthorizationUrl(oauthParameters);

    StringBuilder form = new StringBuilder();
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createFormBeginTag(requestUrl, null, HtmlConsts.POST));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, buttonText));
    form.append(HtmlConsts.FORM_CLOSE);

    return form.toString();
  }
}
