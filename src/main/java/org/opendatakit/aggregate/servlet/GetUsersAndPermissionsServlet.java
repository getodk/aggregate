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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvWriter;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet for downloading a .csv file containing a list of users and their privileges.
 * This must contain all the users and their privileges on the system.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class GetUsersAndPermissionsServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 307803874378006163L;

  /**
   * URI from base
   */
  public static final String ADDR = UIConsts.GET_USERS_AND_PERMS_CSV_SERVLET_ADDR;

  
  private static final Log logger = LogFactory.getLog(GetUsersAndPermissionsServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    if (req.getScheme().equals("http")) {
      logger.warn("Retrieving users and capabilities over http");
    }
    CallingContext cc = ContextFactory.getCallingContext(this, req);
   
    ArrayList<UserSecurityInfo> allUsers;
    
    try {
      allUsers = SecurityServiceUtil.getAllUsers(true, cc);
    } catch (DatastoreFailureException e) {
      logger.error("Retrieving users and capabilities .csv persistence error: " + e.toString());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.toString());
      return;
    } catch (AccessDeniedException e) {
      logger.error("Retrieving users and capabilities .csv access denied error: " + e.toString());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          e.toString());
      return;
    }

    StringWriter buffer = new StringWriter();
    RFC4180CsvWriter writer = new RFC4180CsvWriter(buffer);
    
    String[] columnContent = new String[10];
    
    columnContent[0] = "Username";
    columnContent[1] = "Full Name";
    columnContent[2] = "Account Type";
    columnContent[3] = "Data Collector";
    columnContent[4] = "Data Viewer";
    columnContent[5] = "Form Manager";
    columnContent[6] = "Synchronize Tables";
    columnContent[7] = "Tables Super-user";
    columnContent[8] = "Administer Tables";
    columnContent[9] = "Site Administrator";
    
    writer.writeNext(columnContent);
    for ( UserSecurityInfo i : allUsers ) {
      
      if ( i.getType() == UserType.REGISTERED ) {
        if ( i.getEmail() != null ) {
          columnContent[0] = i.getEmail().substring(EmailParser.K_MAILTO.length());
          columnContent[2] = "Google";
        } else {
          columnContent[0] = i.getUsername();
          columnContent[2] = "ODK";
        }
        columnContent[1] = i.getFullName();
      } else {
        columnContent[0] = User.ANONYMOUS_USER;
        columnContent[1] = User.ANONYMOUS_USER_NICKNAME;
        columnContent[2] = null;
      }
      TreeSet<GrantedAuthorityName> grants = i.getAssignedUserGroups();
      columnContent[3] = grants.contains(GrantedAuthorityName.GROUP_DATA_COLLECTORS) ? "X" : null;
      columnContent[4] = grants.contains(GrantedAuthorityName.GROUP_DATA_VIEWERS) ? "X" : null;
      columnContent[5] = grants.contains(GrantedAuthorityName.GROUP_FORM_MANAGERS) ? "X" : null;
      columnContent[6] = grants.contains(GrantedAuthorityName.GROUP_SYNCHRONIZE_TABLES) ? "X" : null;
      columnContent[7] = grants.contains(GrantedAuthorityName.GROUP_SUPER_USER_TABLES) ? "X" : null;
      columnContent[8] = grants.contains(GrantedAuthorityName.GROUP_ADMINISTER_TABLES) ? "X" : null;
      columnContent[9] = grants.contains(GrantedAuthorityName.GROUP_SITE_ADMINS) ? "X" : null;
      writer.writeNext(columnContent);
    }
    writer.close();
    // do not cache...
    resp.setHeader("Cache-Control:", "no-cache, no-store, must-revalidate");
    resp.setHeader("Pragma:", "no-cache");
    resp.setHeader("Expires:", "0");

    resp.setHeader("Last-Modified:",
        WebUtils.rfc1123Date(new Date()));
    resp.setContentType(HtmlConsts.RESP_TYPE_CSV);
    resp.addHeader(HtmlConsts.CONTENT_DISPOSITION, "attachment; filename=\"UsersAndCapabilities.csv\"");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.getOutputStream().write(buffer.getBuffer().toString().getBytes(CharEncoding.UTF_8));
  }
}