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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKExternalServiceAuthenticationError;
import org.opendatakit.aggregate.exception.ODKExternalServiceNotAuthenticated;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.servlet.CommonServletBase;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;

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
	 *            The HTTP request received at the server
	 * @param resp
	 *            The HTTP response to be sent to client
	 * @return boolean value of whether the user is logged in
	 * @throws IOException
	 *             Throws IO Exception if problem occurs creating the login link
	 *             in response
	 */
	protected boolean verifyCredentials(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
	    UserService userService = (UserService) ContextFactory.get().getBean(ServletConsts.USER_BEAN);
		return verifyCredentials(req, resp, userService);
	}

	@Override
	protected void emitPageHeader(PrintWriter out, boolean displayLinks) {
		if (displayLinks) {
			out.write(generateNavigationInfo());
			out.write(HtmlConsts.TAB + HtmlConsts.TAB);

			UserService userService = (UserService) ContextFactory.get()
					.getBean(ServletConsts.USER_BEAN);
			out.write(HtmlUtil.createHref(userService.createLogoutURL("/"),
					LOGOUT + userService.getCurrentUser().getNickname()));
			out.write(HtmlConsts.TAB + "<FONT SIZE=1>" + ServletConsts.VERSION
					+ "</FONT>");
		}
	}

	/**
	 * Generate error response for ODK ID not found
	 * 
	 * @param resp
	 *            The HTTP response to be sent to client
	 * @throws IOException
	 *             caused by problems writing error information to response
	 */
	protected void odkIdNotFoundError(HttpServletResponse resp)
			throws IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND,
				ErrorConsts.ODKID_NOT_FOUND);
	}

	/**
	 * Generate error response for missing the Key parameter
	 * 
	 * @param resp
	 *            The HTTP response to be sent to client
	 * @throws IOException
	 *             caused by problems writing error information to response
	 */
	protected void errorMissingKeyParam(HttpServletResponse resp)
			throws IOException {
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
				ErrorConsts.ODK_KEY_PROBLEM);
	}

	/**
	 * Generate error response for missing the Key parameter
	 * 
	 * @param resp
	 *            The HTTP response to be sent to client
	 * @throws IOException
	 *             caused by problems writing error information to response
	 */
	protected void errorRetreivingData(HttpServletResponse resp)
			throws IOException {
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				ErrorConsts.INCOMPLETE_DATA);
	}

	/**
	 * Generate common navigation links
	 * 
	 * @return a string with href links
	 */
	public String generateNavigationInfo() {
		String html = HtmlUtil.createHref(FormsServlet.ADDR,
				ServletConsts.FORMS_LINK_TEXT);
		html += HtmlConsts.TAB + HtmlConsts.TAB;
		html += HtmlUtil.createHref(FormUploadServlet.ADDR,
				ServletConsts.UPLOAD_FORM_LINK_TEXT);
		html += HtmlConsts.TAB + HtmlConsts.TAB;
		html += HtmlUtil.createHref(FormDeleteServlet.ADDR,
				ServletConsts.DELETE_FORM_LINK_TEXT);
		html += HtmlConsts.TAB + HtmlConsts.TAB;
		html += HtmlUtil.createHref(ServletConsts.UPLOAD_SUBMISSION_ADDR,
				ServletConsts.UPLOAD_SUB_LINK_TEXT);
		return html + HtmlConsts.TAB;
	}

	protected String verifyGDataAuthorization(HttpServletRequest req,
			HttpServletResponse resp, String scope) throws IOException,
			ODKExternalServiceAuthenticationError,
			ODKExternalServiceNotAuthenticated {
		String queryString = URLDecoder.decode(req.getQueryString(), "UTF-8");
		String onetimeUseToken = AuthSubUtil.getTokenFromReply(queryString);

		String sessionToken = null;

		if (onetimeUseToken == null) {
			throw new ODKExternalServiceNotAuthenticated();
		} else {

			try {
				sessionToken = AuthSubUtil.exchangeForSessionToken(
						onetimeUseToken, null);
			} catch (AuthenticationException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Server rejected one time use token.");
				throw new ODKExternalServiceAuthenticationError();

			} catch (GeneralSecurityException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Security error while retrieving session token.");
				throw new ODKExternalServiceAuthenticationError();
			}

			try {
				Map<String, String> tokenInfo = AuthSubUtil.getTokenInfo(
						sessionToken, null);

				String tokenScope = tokenInfo.get("Scope");

				if (!tokenScope.equals(scope)) {
					resp.sendRedirect(generateAuthorizationURL(req, scope));
					throw new ODKExternalServiceNotAuthenticated();
				}
			} catch (AuthenticationException e) {
				resp.sendRedirect(generateAuthorizationURL(req, scope));
				throw new ODKExternalServiceAuthenticationError();
			} catch (GeneralSecurityException e) {
				resp.sendRedirect(generateAuthorizationURL(req, scope));
				throw new ODKExternalServiceAuthenticationError();
			}
		}

		return sessionToken;
	}

	// TODO: see if can integrate better with helper functions in
	// SpreadsheetServlet
	protected String generateAuthorizationURL(HttpServletRequest req,
			String scope) {
		String returnUrl = "http://" + getServerURL(req) + req.getRequestURI()
				+ HtmlConsts.BEGIN_PARAM + req.getQueryString();
		String requestUrl = AuthSubUtil.getRequestUrl(returnUrl, scope, false,
				true);
		return requestUrl;
	}

	protected String generateAuthButton(String scope, String buttonText,
			Map<String, String> params, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {

		String returnUrl = "http://"
				+ HtmlUtil.createLinkWithProperties(getServerURL(req)
						+ req.getRequestURI(), params);

		String requestUrl = AuthSubUtil.getRequestUrl(returnUrl, scope, false,
				true);

		StringBuilder form = new StringBuilder();
		form.append(HtmlConsts.LINE_BREAK);
		form.append(HtmlUtil.createFormBeginTag(requestUrl, null,
				HtmlConsts.POST));
		form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
				buttonText));
		form.append(HtmlConsts.FORM_CLOSE);

		return form.toString();
	}

	protected List<EntityKey> convertSubmissionStringKeys(
			List<String> paramKeys, String submissionKey,
			FormDefinition formDefinition, Datastore ds, User user) {
		FormDataModel fdm = formDefinition.getElementByName(submissionKey);
		List<EntityKey> keys = new ArrayList<EntityKey>();
		for (String paramKey : paramKeys) {
			EntityKey key = new EntityKey(fdm.getBackingObjectPrototype(),
					paramKey);
			keys.add(key);
		}
		return keys;
	}

}
