/*
 * Copyright (C) 2011 University of Washington.
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

package org.odk.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.javarosa.core.model.utils.DateUtils;
import org.odk.aggregate.BriefcaseAuth;
import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.parser.SubmissionKey;
import org.odk.aggregate.parser.SubmissionKeyPart;
import org.odk.aggregate.submission.Submission;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Servlet to generate the XML representation of a given submission entry and
 * return the attachments associated with that submission.
 * 
 * Used by Briefcase 2.0 download.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionDownloadServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -5861240658170389989L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/downloadSubmission";

  /**
   * Handler for HTTP Get request that responds with the XML
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String briefcaseParam = req.getHeader(ServletConsts.BRIEFCASE_APP_TOKEN_HEADER);

    // copied from FormUploadServlet
    String authParam = getParameter(req, ServletConsts.AUTHENTICATION);

    User user = null;

    if (authParam != null && authParam.equalsIgnoreCase(ServletConsts.AUTHENTICATION_OAUTH)) {
      // Try OAuth authentication
      try {
        OAuthService oauth = OAuthServiceFactory.getOAuthService();
        user = oauth.getCurrentUser();
        if (user == null) {
          resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorConsts.OAUTH_ERROR);
          return;
        }
      } catch (OAuthRequestException e) {
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorConsts.OAUTH_ERROR + "\n Reason: "
            + e.getLocalizedMessage());
        return;
      }
    } else if (briefcaseParam != null && briefcaseParam.length() != 0) {
      // Verify briefcase token
      if (!BriefcaseAuth.verifyBriefcaseAuthToken(briefcaseParam)) {
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid briefcase application token");
        return;
      }
    } else {
      // Try User Service authentication
      UserService userService = UserServiceFactory.getUserService();
      user = userService.getCurrentUser();

      // verify user is logged in
      if (!verifyCredentials(req, resp)) {
        return;
      }
    }

    EntityManager em = EMFactory.get().createEntityManager();

    // verify parameters are present
    String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

    // formId is actually an ODK Aggregate 1.x SubmissionKey
    // parse it here...
    SubmissionKey key = new SubmissionKey(formId);
    List<SubmissionKeyPart> parts = key.splitSubmissionKey();

    Form form;
    try {
      form = Form.retrieveForm(em, parts.get(0).getElementName());
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key entityKey = KeyFactory.stringToKey(parts.get(1).getAuri());
    Entity subEntity;
    try {
      subEntity = ds.get(entityKey);
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      errorRetreivingData(resp);
      return;
    }
    Submission sub;
    try {
      sub = new Submission(subEntity, form);
    } catch (ODKIncompleteSubmissionData e) {
      e.printStackTrace();
      errorRetreivingData(resp);
      return;
    }

    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
    resp.setContentType(ServletConsts.RESP_TYPE_XML);
    addOpenRosaHeaders(resp);

    PrintWriter out = resp.getWriter();
    out.write("<submission xmlns=\"http://opendatakit.org/submissions\">");
    out.write("<data>");
    StringBuilder attr = new StringBuilder();
    attr.append(" id=\"");
    attr.append(StringEscapeUtils.escapeXml(parts.get(0).getElementName()));
    attr.append("\" instanceID=\"");
    attr.append(StringEscapeUtils.escapeXml("md5:"
        + getMD5Hash(KeyFactory.keyToString(sub.getKey()))));
    attr.append("\" submissionDate=\"");
    attr.append(StringEscapeUtils.escapeXml(DateUtils.formatDateTime(sub.getSubmittedTime(),DateUtils.FORMAT_ISO8601)));
    attr.append("\"");
    StringBuilder b = new StringBuilder();
    FormElement element = form.getBeginningElement(sub.getPropertyName());
    sub.generateXmlSerialization(element, b, attr.toString());
    out.write(b.toString());
    out.write("</data>\n");
    b.setLength(0);
    sub.generateXmlAttachmentSerialization(b, HtmlUtil.createUrl(getServerURL(req)));
    out.write(b.toString());
    out.write("</submission>");
    resp.setStatus(HttpServletResponse.SC_OK);

    em.close();
  }

  private String getMD5Hash(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] asBytes = value.getBytes();
      md.update(asBytes);

      byte[] messageDigest = md.digest();

      BigInteger number = new BigInteger(1, messageDigest);
      String md5 = number.toString(16);
      while (md5.length() < 32)
        md5 = "0" + md5;
      return md5;
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unexpected problem computing md5 hash", e);
    }
  }

}
