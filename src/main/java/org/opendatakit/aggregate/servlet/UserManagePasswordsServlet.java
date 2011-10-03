/*
 * Copyright (C) 2010 University of Washington
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * JSON servlet used by the GWT layer to send change password requests over
 * https if https is available, regardless of whether the GWT layer itself is
 * running under http.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserManagePasswordsServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 3078038743780061473L;

  /**
   * URI from base
   */
  public static final String ADDR = "ssl/user-manage-passwords";

  public static final String CALLBACK = "callback";
  public static final String ECHO = "echo";
  public static final String USERNAME = "username";
  public static final String DIGEST_AUTH_HASH = "digestAuthHash";
  public static final String BASIC_AUTH_HASH = "basicAuthHash";
  public static final String BASIC_AUTH_SALT = "basicAuthSalt";
  public static final String STATUS = "status";

  /**
   * Returns an object possibly wrapped by a callback method name. The return is
   * of the form:
   * 
   * <pre>
   * cccc ({ "username" : "nnnn", "status" : "oooo", "echo" : "ssss" })
   * </pre>
   * 
   * Where:
   * <ul>
   * <li>cccc = callback parameter value</li>
   * <li>nnnn = username parameter value</li>
   * <li>oooo = outcome of change password action</li>
   * <li>ssss = echo parameter value</li>
   * </ul>
   * 
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    if (req.getScheme().equals("http")) {
      LogFactory.getLog(UserManagePasswordsServlet.class).warn("Setting user passwords over http");
    }
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    resp.setContentType("text/javascript; charset=UTF-8");
    resp.setHeader("Cache-Control", "no-cache");
    resp.setHeader("Pragma", "no-cache");
    PrintWriter out = resp.getWriter();

    String callback = req.getParameter(CALLBACK);
    String echo = req.getParameter(ECHO);

    if (callback != null) {
      out.write(callback);
    }
    out.write("({");

    String username = req.getParameter(USERNAME);
    String digestAuthHash = req.getParameter(DIGEST_AUTH_HASH);
    String basicAuthHash = req.getParameter(BASIC_AUTH_HASH);
    String basicAuthSalt = req.getParameter(BASIC_AUTH_SALT);

    String outcome;

    if (username == null || username.length() == 0) {
      username = "";
      outcome = "No username specified";
    } else {
      CredentialsInfo credential = new CredentialsInfo();
      credential.setUsername(username);
      credential.setDigestAuthHash(digestAuthHash);
      credential.setBasicAuthHash(basicAuthHash);
      credential.setBasicAuthSalt(basicAuthSalt);

      try {
        SecurityServiceUtil.setUserCredentials(credential, cc);
        outcome = "OK";
      } catch (AccessDeniedException e1) {
        outcome = "Bad username";
      } catch (DatastoreFailureException e1) {
        outcome = ErrorConsts.PERSISTENCE_LAYER_PROBLEM;
      }
    }

    username.replace(BasicConsts.QUOTE, BasicConsts.EMPTY_STRING); // shouldn't
                                                                   // be
                                                                   // allowed...
    out.write(BasicConsts.QUOTE + USERNAME + BasicConsts.QUOTE + BasicConsts.COLON
        + BasicConsts.QUOTE + username + BasicConsts.QUOTE);
    outcome.replace(BasicConsts.QUOTE, BasicConsts.EMPTY_STRING); // shouldn't
                                                                  // be
                                                                  // allowed...
    out.write(BasicConsts.COMMA);
    out.write(BasicConsts.QUOTE + STATUS + BasicConsts.QUOTE + BasicConsts.COLON
        + BasicConsts.QUOTE + outcome + BasicConsts.QUOTE);
    if (echo != null) {
      echo.replace(BasicConsts.QUOTE, BasicConsts.EMPTY_STRING); // shouldn't be
                                                                 // allowed...
      out.write(BasicConsts.COMMA);
      out.write(BasicConsts.QUOTE + ECHO + BasicConsts.QUOTE + BasicConsts.COLON
          + BasicConsts.QUOTE + echo + BasicConsts.QUOTE);
    }
    out.write("})");
    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
  }
}