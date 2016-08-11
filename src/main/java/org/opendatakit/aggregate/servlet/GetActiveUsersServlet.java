/*
 * Copyright (C) 2016 University of Washington.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet to return a list of the active users on the system. This servlet requires
 * that the caller be a registered user of the system (access is denied to anonymous).
 * 
 * The returned list will contain just the current user_id if the user does not have
 * Site Admin, Tables Admin or Tables super-user privileges.
 * 
 * The list is a JSON serialization of:
 * 
 * ArrayList<Map<String,String>>
 * 
 * Where the map contains 2 entries:
 *    user_id:  
 *    full_name:
 *    
 * user_id is of the form:
 *    anonymous
 *    username:myname
 *    mailto:my@emailcorp.com
 * 
 * full_name is the full name for that user and is guaranteed to not be null.
 * If the user does not have Site Admin, Tables Admin or Tables Super-user privileges, 
 * then we return a list with just that user's user id and friendly name. 
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class GetActiveUsersServlet extends HttpServlet {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -9115712148453254161L;

  /**
   * URI from base
   */
  public static final String ADDR = "users/list";

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String USER_ID = "user_id";
  private static final String FULL_NAME = "full_name";
  
  private static final Log logger = LogFactory.getLog(GetActiveUsersServlet.class);

  /**
   * Handler for HTTP Get request that returns the list of roles assigned to this user.
   * 
   * Assumed to return a entity body that is a JSON serialization of a list.
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    Set<GrantedAuthority> grants = cc.getCurrentUser().getDirectAuthorities();
    RoleHierarchy rh = (RoleHierarchy) cc.getBean(SecurityBeanDefs.ROLE_HIERARCHY_MANAGER);
    Collection<? extends GrantedAuthority> roles = rh.getReachableGrantedAuthorities(grants);
    boolean returnFullList = false;
    for ( GrantedAuthority a : roles ) {
      if (a.getAuthority().equals(GrantedAuthorityName.GROUP_SITE_ADMINS.name()) ||
          a.getAuthority().equals(GrantedAuthorityName.GROUP_ADMINISTER_TABLES.name()) ||
          a.getAuthority().equals(GrantedAuthorityName.GROUP_SUPER_USER_TABLES.name())) {
        returnFullList = true;
        break;
      }
    }
    
    // returned object (will be JSON serialized).
    ArrayList<HashMap<String,String>> listOfUsers = new ArrayList<HashMap<String,String>>();
    
    HashMap<String,String> hashMap; 
    if ( !returnFullList ) {
      // only return ourself -- we don't have privileges to see everyone
      hashMap = new HashMap<String,String>();
      User user = cc.getCurrentUser();
      if ( user.isAnonymous() ) {
        hashMap.put(USER_ID, "anonymous"); 
        hashMap.put(FULL_NAME, User.ANONYMOUS_USER_NICKNAME);
      } else {
        RegisteredUsersTable entry;
        try {
          entry = RegisteredUsersTable.getUserByUri(user.getUriUser(), cc.getDatastore(), cc.getCurrentUser());
        } catch (ODKDatastoreException e) {
          logger.error("Retrieving users persistence error: " + e.toString());
          e.printStackTrace();
          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.toString());
          return;
        }
        if ( user.getEmail() == null ) {
          hashMap.put(USER_ID, "username:" + entry.getUsername());
          if ( user.getNickname() == null ) {
            hashMap.put(FULL_NAME, entry.getUsername());
          } else {
            hashMap.put(FULL_NAME, user.getNickname());
          }
        } else {
          hashMap.put(USER_ID, entry.getEmail());
          if ( user.getNickname() == null ) {
            hashMap.put(FULL_NAME, entry.getEmail().substring(EmailParser.K_MAILTO.length()));
          } else {
            hashMap.put(FULL_NAME, user.getNickname());
          }
        }
      }
      listOfUsers.add(hashMap);
    } else {
      // we have privileges to see all users -- return the full mapping
      try {
        ArrayList<UserSecurityInfo> allUsers = SecurityServiceUtil.getAllUsers(false, cc);
        for (UserSecurityInfo i : allUsers ) {
          hashMap = new HashMap<String,String>();
          if ( i.getType() == UserType.ANONYMOUS ) {
            hashMap.put(USER_ID, "anonymous"); 
            hashMap.put(FULL_NAME, User.ANONYMOUS_USER_NICKNAME);
          } else if ( i.getEmail() == null ) {
            hashMap.put(USER_ID, "username:" + i.getUsername()); 
            if ( i.getFullName() == null ) {
              hashMap.put(FULL_NAME, i.getUsername());
            } else {
              hashMap.put(FULL_NAME, i.getFullName());
            }
          } else {
            // already has the mailto: prefix
            hashMap.put(USER_ID, i.getEmail()); 
            if ( i.getFullName() == null ) {
              hashMap.put(FULL_NAME, i.getEmail().substring(EmailParser.K_MAILTO.length()));
            } else {
              hashMap.put(FULL_NAME, i.getFullName());
            }
          }
          listOfUsers.add(hashMap);
        }
      } catch (DatastoreFailureException e) {
        logger.error("Retrieving users persistence error: " + e.toString());
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.toString());
        return;
      } catch (AccessDeniedException e) {
        logger.error("Retrieving users access denied error: " + e.toString());
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            e.toString());
        return;
      }
    }    
    
    resp.addHeader(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION);
    resp.addHeader("Access-Control-Allow-Origin", "*");
    resp.addHeader("Access-Control-Allow-Credentials", "true");
    resp.addHeader(HttpHeaders.HOST, cc.getServerURL());
    resp.setContentType(HtmlConsts.RESP_TYPE_JSON);
    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
    
    PrintWriter out = resp.getWriter();
    out.write(mapper.writeValueAsString(listOfUsers));
    out.flush();

    resp.setStatus(HttpStatus.SC_OK);
  }
}
