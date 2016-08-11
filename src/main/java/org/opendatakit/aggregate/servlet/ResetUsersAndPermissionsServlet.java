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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvReader;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.EmailParser.Email;
import org.opendatakit.common.security.common.EmailParser.Email.Form;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet for uploading a .csv file containing a list of users and their privileges.
 * This must contain all the users and their privileges on the system.  Passwords
 * can be managed separately using the UserManagePasswordsServlet or the UI.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class ResetUsersAndPermissionsServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 3078038743780061673L;

  /**
   * URI from base
   */
  public static final String ADDR = UIConsts.USERS_AND_PERMS_UPLOAD_SERVLET_ADDR;

  /**
   * Name of form field that contains the users and capabilities csv file.
   */
  public final static String ACCESS_DEF_PRAM = "access_def_file";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Define Users and Permssions via .csv Upload";

  private static final String UPLOAD_PAGE_BODY_START =

  "<div class=\"gwt-HTML\"><table class=\"gwt-TabPanel\"><tbody>"
      + "<tr><td><form id=\"ie_backward_compatible_form\""
      + " accept-charset=\"UTF-8\" method=\"POST\" encoding=\"multipart/form-data\" enctype=\"multipart/form-data\""
      + " action=\"";// emit the ADDR
  private static final String UPLOAD_PAGE_BODY_MIDDLE = "\">"
      + "     <table id=\"uploadTable\">"
      + "      <tr>"
      + "         <td><label for=\"access_def_file\">users and capabilities csv file:</label></td>"
      + "      </tr><tr>"
      + "         <td><input id=\"access_def_file\" type=\"file\" size=\"80\" class=\"gwt-Button\""
      + "            name=\"access_def_file\" /></td>"
      + "      </tr><tr>"
      + "         <td><input id=\"reset_permissions\" type=\"submit\" name=\"button\" class=\"gwt-Button\" value=\"Update Permissions\" /></td>"
      + "         <td />"
      + "      </tr>"
      + "     </table>\n"
      + "     </form>"
      + "<br><br></td></tr>"
      + "<tr><td><p id=\"subHeading\"><h2>Usage</h2></p>"
      + "<p>Use Excel or OpenOffice to create a spreadsheet with all of the server's users and their capabilities.</p>"
      + "<p>Save that spreadsheet as a .csv file and upload it to ODK Aggregate. The server will:</p>"
      + "<ol><li>remove any users not defined in this file,</li>"
      + "<li>create users if they do not yet exist on the server, and</li>"
      + "<li>alter all users' capabilities so that they match those defined in the .csv file.</li>"
      + "</ol>"
      + "<p>The .csv file can begin with any number of rows containing site-specific information <em>provided</em> these"
      + " rows contain fewer than 4 columns.</p>"
      + "<p>The first 4-cell-or-wider row is expected to contain the column headings"
      + " for the users-and-capabilities table; each subsequent row defines a user on the system."
      + " Blank rows are allowed and are ignored. Unrecognized column headings are ignored"
      + " (these may be used for comments or other site-specific purposes).</p>"
      + "<p>The users-and-capabilities table's column headings which ODK Aggregate interprets are:</p>"
      + "<ul>"
      + "<li><strong>Username</strong> - one of 'anonymousUser', an ODK Aggregate username, or an e-mail address.</li>"
      + "<li><strong>Full Name</strong> - the friendly name displayed when referring to this username.</li>"
      + "<li><strong>Account Type</strong> - one of 'ODK', 'Google' or empty (for anonymousUser)</li>"
      + "<li><strong>Data Collector</strong> - any mark in this column grants this capability to this user.</li>"
      + "<li><strong>Data Viewer</strong> - any mark in this column grants this capability to this user.</li>"
      + "<li><strong>Form Manager</strong> - any mark in this column grants this capability to this user.</li>"
      + "<li><strong>Synchronize Tables</strong> - any mark in this column grants this capability to this user.</li>"
      + "<li><strong>Tables Super-user</strong> - any mark in this column grants this capability to this user.</li>"
      + "<li><strong>Administer Tables</strong> - any mark in this column grants this capability to this user.</li>"
      + "<li><strong>Site Administrator</strong> - any mark in this column grants this capability to this user.</li>"
      + "</ul>"
      + "<p>Of these, only 'Username' and 'Account Type' are required to be present.</p><p>The server will prohibit some"
      + " actions, such as deleting the super-user, or granting Site Administrator privileges to the anonymousUser</p>"
      + "</td></tr></tbody></table></div>\n";
  
  private static final Log logger = LogFactory.getLog(ResetUsersAndPermissionsServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    if (req.getScheme().equals("http")) {
      logger.warn("Resetting users and capabilities over http");
    }
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    Double openRosaVersion = getOpenRosaVersion(req);
    if (openRosaVersion != null) {
      /*
       * If we have an OpenRosa version header, assume that this is due to a
       * channel redirect (http: => https:) and that the request was originally
       * a HEAD request. Reply with a response appropriate for a HEAD request.
       *
       * It is unclear whether this is a GAE issue or a Spring Frameworks issue.
       */
      logger.warn("Inside doGet -- replying as doHead");
      doHead(req, resp);
      return;
    }

    StringBuilder headerString = new StringBuilder();
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.AGGREGATE_STYLE));
    headerString.append("\" />");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_BUTTON_STYLE_RESOURCE));
    headerString.append("\" />");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_TABLE_STYLE_RESOURCE));
    headerString.append("\" />");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_NAVIGATION_STYLE_RESOURCE));
    headerString.append("\" />");

    // header info
    beginBasicHtmlResponse(TITLE_INFO, headerString.toString(), resp, cc);
    PrintWriter out = resp.getWriter();
    out.write(UPLOAD_PAGE_BODY_START);
    out.write(cc.getWebApplicationURL(ADDR));
    out.write(UPLOAD_PAGE_BODY_MIDDLE);
    finishBasicHtmlResponse(resp);
  }

  /**
   * Handler for HTTP head request. This is used to verify that channel security
   * and authentication have been properly established when uploading user
   * permission definitions via a program (e.g., Briefcase).
   */
  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    logger.info("Inside doHead");

    addOpenRosaHeaders(resp);
    String serverUrl = cc.getServerURL();
    String url = serverUrl + BasicConsts.FORWARDSLASH + ADDR;
    resp.setHeader("Location", url);
    resp.setStatus(204); // no content...
  }

  /**
   * Processes the multipart form that contains the csv file which holds the 
   * list of users and thier permissions. Returns success if the changes have
   * been applied; false otherwise.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    if (req.getScheme().equals("http")) {
      logger.warn("Resetting users and capabilities over http");
    }
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    Double openRosaVersion = getOpenRosaVersion(req);

    // verify request is multipart
    if (!ServletFileUpload.isMultipartContent(req)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_MULTI_PART_CONTENT);
      return;
    }

    StringBuilder warnings = new StringBuilder();
    // TODO Add in form title process so it will update the changes in the XML
    // of form

    try {
      // process form
      MultiPartFormData resetUsersAndPermissions = new MultiPartFormData(req);

      MultiPartFormItem usersAndPermissionsCsv = resetUsersAndPermissions
          .getFormDataByFieldName(ACCESS_DEF_PRAM);
      
      String inputCsv = null;

      if (usersAndPermissionsCsv != null) {
        // TODO: changed added output stream writer. probably something better
        // exists
        inputCsv = usersAndPermissionsCsv.getStream().toString(HtmlConsts.UTF8_ENCODE);
      }

      StringReader csvContentReader = null;
      RFC4180CsvReader csvReader = null;
      try {
        // we need to build up the UserSecurityInfo records for all the users
        ArrayList<UserSecurityInfo> users = new ArrayList<UserSecurityInfo>(); 
        
        // build reader for the csv content
        csvContentReader = new StringReader(inputCsv);
        csvReader = new RFC4180CsvReader(csvContentReader);
        
        // get the column headings -- these mimic those in Site Admin / Permissions table. 
        // Order is irrelevant; no change-password column.
        //
        String[] columns;
        int row = 0;
        
        for (;;) {
          ++row;
          columns = csvReader.readNext();
          
          if ( columns == null ) {
            logger.error("users and capabilities .csv upload - empty csv file");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                ErrorConsts.MISSING_PARAMS + "\nusers and capabilities .csv is empty");
            return;
          }
          
          // count non-blank columns
          int nonBlankColCount = 0;
          for ( String col : columns ) {
            if ( col != null && col.trim().length() != 0 ) {
              ++nonBlankColCount;
            }
          }

          // if there are fewer than 4 columns, it must be a comment field.
          // if there are 4 or more columns, then we expect it to be the column headers
          // for the users and capabilities table. We could require just 3, but that
          // would not be very useful or realistic.
          if ( nonBlankColCount < 4 ) continue;
          
          break;
        }
        if ( row != 1 ) {
          logger.warn("users and capabilities .csv upload -- interpreting row " + row + " as the column header row");
          warnings.append("<tr><td>Interpreting row " + row + " as the column header row.</td></tr>");
        }
       
        // TODO: validate column headings....
        int idxUsername = -1;
        int idxFullName = -1;
        int idxUserType = -1;
        int idxDataCollector = -1;
        int idxDataViewer = -1;
        int idxFormManager = -1;
        int idxSyncTables = -1;
        int idxTablesSU = -1;
        int idxTablesAdmin = -1;
        int idxSiteAdmin = -1;
        
        for ( int i = 0 ; i < columns.length ; ++i ) {
          String heading = columns[i];
          if ( heading == null || heading.trim().length() == 0 ) {
            continue;
          }
          heading = heading.trim();
          // 'Username' is required
          if ( "Username".compareToIgnoreCase(heading) == 0 ) {
            if ( idxUsername != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Username' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Username' is repeated");
              return;
            }
            idxUsername = i;
          }
          // 'Full Name' is optional. The value in 'Username' will be used to construct this if unspecified.
          else if ( "Full Name".compareToIgnoreCase(heading) == 0 ) {
            if ( idxFullName != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Full Name' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Full Name' is repeated");
              return;
            }
            idxFullName = i;
          }
          // 'Account Type' is required
          else if ( "Account Type".compareToIgnoreCase(heading) == 0 ) {
            if ( idxUserType != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Account Type' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Account Type' is repeated");
              return;
            }
            idxUserType = i;
          }
          // Permissions columns begin here. All are optional
          else if ( "Data Collector".compareToIgnoreCase(heading) == 0 ) {
            if ( idxDataCollector != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Data Collector' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Data Collector' is repeated");
              return;
            }
            idxDataCollector = i;
          }
          else if ( "Data Viewer".compareToIgnoreCase(heading) == 0 ) {
            if ( idxDataViewer != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Data Viewer' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Data Viewer' is repeated");
              return;
            }
            idxDataViewer = i;
          }
          else if ( "Form Manager".compareToIgnoreCase(heading) == 0 ) {
            if ( idxFormManager != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Form Manager' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Form Manager' is repeated");
              return;
            }
            idxFormManager = i;
          }
          else if ( "Synchronize Tables".compareToIgnoreCase(heading) == 0 ) {
            if ( idxSyncTables != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Synchronize Tables' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Synchronize Tables' is repeated");
              return;
            }
            idxSyncTables = i;
          }
          else if ( "Tables Super-user".compareToIgnoreCase(heading) == 0 ) {
            if ( idxTablesSU != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Tables Super-user' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Tables Super-user' is repeated");
              return;
            }
            idxTablesSU = i;
          }
          else if ( "Administer Tables".compareToIgnoreCase(heading) == 0 ) {
            if ( idxTablesAdmin != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Administer Tables' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Administer Tables' is repeated");
              return;
            }
            idxTablesAdmin = i;
          }
          else if ( "Site Administrator".compareToIgnoreCase(heading) == 0 ) {
            if ( idxSiteAdmin != -1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file -- column header 'Site Administrator' is repeated");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Site Administrator' is repeated");
              return;
            }
            idxSiteAdmin = i;
          } else {
            logger.warn("users and capabilities .csv upload - invalid csv file -- column header '" + heading + "' is not recognized");
            warnings.append("<tr><td>Column header '" + heading + "' is not recognized and will be ignored.</tr></td>");
          }
        }

        if ( idxUsername == -1 ) {
          logger.error("users and capabilities .csv upload - invalid csv file");
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
              ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Username' is missing");
          return;
        }
        if ( idxUserType == -1 ) {
          logger.error("users and capabilities .csv upload - invalid csv file");
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
              ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- column header 'Account Type' is missing");
          return;
        }
        
        while ( (columns = csvReader.readNext()) != null ) {
          ++row;

          // empty -- silently skip
          if ( columns.length == 0 ) continue;
          
          // count non-blank columns
          int nonBlankColCount = 0;
          for ( String col : columns ) {
            if ( col != null && col.trim().length() != 0 ) {
              ++nonBlankColCount;
            }
            
          }
          
          // all blank-- silently skip
          if ( nonBlankColCount == 0 ) continue;
          
          // ignore rows where...
          // the row is not long enough to include the Username and Account Type columns
          if ( columns.length <= idxUsername || columns.length <= idxUserType ) {
            warnings.append("<tr><td>Ignoring row " + row + " -- does not specify a Username and/or Account Type.</tr></td>");
            continue;
          }

          // ignore rows where...
          // Username is not specified or it is not the anonymousUser and Account Type is blank
          if ( (columns[idxUsername] == null || columns[idxUsername].trim().length() == 0) ||
               (!columns[idxUsername].equals(User.ANONYMOUS_USER) && (columns[idxUserType] == null || columns[idxUserType].trim().length() == 0)) ) {
            warnings.append("<tr><td>Ignoring row " + row + " -- Username is not the " + User.ANONYMOUS_USER + " and no Account Type specified.</tr></td>");
            continue;
          }
          
          
          String accType = (idxUserType == -1) ? "ODK" : columns[idxUserType];
          UserType type = (accType == null) ? UserType.ANONYMOUS : UserType.REGISTERED;

          if ( (type != UserType.ANONYMOUS) && (columns[idxUsername] == null) ) {
            logger.error("users and capabilities .csv upload - invalid csv file");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- username not specified");
            return;
          }
          
          String username; 
          String email;
          String fullname = (idxFullName == -1 || columns.length < idxFullName) ? null : columns[idxFullName]; 
          
          if ( accType == null ) {
            username = User.ANONYMOUS_USER;
            email = null;
            fullname = User.ANONYMOUS_USER_NICKNAME;
          } else if ("ODK".equals(accType)) {

            Collection<Email> emails = EmailParser.parseEmails(columns[idxUsername]);
            if ( emails.size() != 1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- username \'" +
                      columns[idxUsername] + "\' contains illegal characters (e.g., spaces)");
              return;
            }
            email = null;
            Email parsedValue = emails.iterator().next();
            if ( parsedValue.getType() == Form.EMAIL ) {
              username = parsedValue.getEmail().substring(EmailParser.K_MAILTO.length());
            } else {
              username = parsedValue.getUsername();
            }
            if ( fullname == null ) {
              fullname = parsedValue.getFullName();
            }
          } else if ("Google".equals(accType)) {
            username = null;
            Collection<Email> emails = EmailParser.parseEmails(columns[idxUsername]);
            
            if ( emails == null || emails.size() == 0 ) {
              logger.error("users and capabilities .csv upload - invalid csv file");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- username \'" +
                      columns[idxUsername] + "\' could not be parsed into valid e-mail");
              return;
            }
            
            if ( emails.size() != 1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- username \'" +
                      columns[idxUsername] + "\' could not be parsed into a valid e-mail");
              return;
            }
            
            // will execute loop once
            email = null;
            for ( Email e : emails ) {
              if ( e.getType() != Email.Form.EMAIL ) {
                logger.error("users and capabilities .csv upload - invalid csv file");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- username \'" +
                        columns[idxUsername] + "\' could not be parsed into a valid e-mail");
                return;
              }
              email = e.getEmail();
              if ( fullname == null ) {
                fullname = e.getFullName();
              }
            }
          } else {
            logger.error("users and capabilities .csv upload - invalid csv file");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- Account Type \'" +
                    accType + "\' is neither 'ODK' nor 'Google' nor blank (anonymous)");
            return;
          }
          UserSecurityInfo info = new UserSecurityInfo( username, fullname, email, type);
          // now add permissions
          TreeSet<GrantedAuthorityName> authorities = new TreeSet<GrantedAuthorityName>();
          
          if ( idxDataCollector != -1 && columns.length > idxDataCollector && columns[idxDataCollector] != null && columns[idxDataCollector].trim().length() != 0 ) {
            authorities.add(GrantedAuthorityName.GROUP_DATA_COLLECTORS);
          }
          if ( idxDataViewer != -1 && columns.length > idxDataViewer && columns[idxDataViewer] != null && columns[idxDataViewer].trim().length() != 0 ) {
            authorities.add(GrantedAuthorityName.GROUP_DATA_VIEWERS);
          }
          if ( idxFormManager != -1 && columns.length > idxFormManager && columns[idxFormManager] != null && columns[idxFormManager].trim().length() != 0 ) {
            authorities.add(GrantedAuthorityName.GROUP_FORM_MANAGERS);
          }
          if ( idxSyncTables != -1 && columns.length > idxSyncTables && columns[idxSyncTables] != null && columns[idxSyncTables].trim().length() != 0 ) {
            authorities.add(GrantedAuthorityName.GROUP_SYNCHRONIZE_TABLES);
          }
          if ( idxTablesSU != -1 && columns.length > idxTablesSU && columns[idxTablesSU] != null && columns[idxTablesSU].trim().length() != 0 ) {
            authorities.add(GrantedAuthorityName.GROUP_SUPER_USER_TABLES);
          }
          if ( idxTablesAdmin != -1 && columns.length > idxTablesAdmin && columns[idxTablesAdmin] != null && columns[idxTablesAdmin].trim().length() != 0 ) {
            authorities.add(GrantedAuthorityName.GROUP_ADMINISTER_TABLES);
          }
          if ( idxSiteAdmin != -1 && columns.length > idxSiteAdmin && columns[idxSiteAdmin] != null && columns[idxSiteAdmin].trim().length() != 0 ) {
            authorities.add(GrantedAuthorityName.GROUP_SITE_ADMINS);
          }

          info.setAssignedUserGroups(authorities);
          users.add(info);
        }
        
        // allGroups is empty. This is currently not used.
        ArrayList<GrantedAuthorityName> allGroups = new ArrayList<GrantedAuthorityName>();
        
        // now scan for duplicate entries for the same username
        {
          HashMap<String,HashSet<UserSecurityInfo>> multipleRows = new HashMap<String,HashSet<UserSecurityInfo>>();
          for ( UserSecurityInfo i : users ) {
            if ( i.getType() != UserType.REGISTERED ) {
              continue;
            }
            if ( i.getUsername() != null ) {
              HashSet<UserSecurityInfo> existing;
              existing = multipleRows.get(i.getUsername());
              if ( existing == null ) {
                existing = new HashSet<UserSecurityInfo>();
                multipleRows.put(i.getUsername(), existing);
              }
              existing.add(i);
            }
          }
          for ( Entry<String, HashSet<UserSecurityInfo>> entry : multipleRows.entrySet() ) {
            if ( entry.getValue().size() != 1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- " +
                      "multiple rows define the capabilities for the same username: " + entry.getKey());
              return;
            }
          }
        }
        
        // and scan for duplicate entries for the same e-mail address
        {
          HashMap<String,HashSet<UserSecurityInfo>> multipleRows = new HashMap<String,HashSet<UserSecurityInfo>>();
          for ( UserSecurityInfo i : users ) {
            if ( i.getType() != UserType.REGISTERED ) {
              continue;
            }
            if ( i.getEmail() != null ) {
              HashSet<UserSecurityInfo> existing;
              existing = multipleRows.get(i.getEmail());
              if ( existing == null ) {
                existing = new HashSet<UserSecurityInfo>();
                multipleRows.put(i.getEmail(), existing);
              }
              existing.add(i);
            }
          }
          for ( Entry<String, HashSet<UserSecurityInfo>> entry : multipleRows.entrySet() ) {
            if ( entry.getValue().size() != 1 ) {
              logger.error("users and capabilities .csv upload - invalid csv file");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- " +
                      "multiple rows define the capabilities for the same e-mail: " +
                      entry.getKey().substring(EmailParser.K_MAILTO.length()));
              return;
            }
          }
        }
        
        // now scan for the anonymousUser
        UserSecurityInfo anonUser = null;
        for ( UserSecurityInfo i : users ) {
          if ( i.getType() == UserType.ANONYMOUS ) {
            if ( anonUser != null ) {
              logger.error("users and capabilities .csv upload - invalid csv file");
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  ErrorConsts.MISSING_PARAMS + "\nusers and capabilities invalid .csv -- " +
                      "multiple rows define the capabilities for the anonymousUser - did you forget to specify Account Type?");
              return;
            }
            anonUser = i;
          }
        }

        // and figure out whether the anonymousUser currently has ROLE_ATTACHMENT_VIEWER capabilities
        // (these allow Google Earth to access the server). 
        //
        // If it does, preserve that capability.
        // To do this, fetch the existing info for anonymous...
        UserSecurityInfo anonExisting = new UserSecurityInfo(User.ANONYMOUS_USER,
            User.ANONYMOUS_USER_NICKNAME, null, UserSecurityInfo.UserType.ANONYMOUS);
        SecurityServiceUtil.setAuthenticationListsForSpecialUser(anonExisting,
              GrantedAuthorityName.USER_IS_ANONYMOUS, cc);
        // test if the existing anonymous had the capability
        if ( anonExisting.getAssignedUserGroups().contains(GrantedAuthorityName.ROLE_ATTACHMENT_VIEWER) ) {
          if ( anonUser == null ) {
            // no anonUser specified in the incoming .csv -- add it with just that capability.
            TreeSet<GrantedAuthorityName> auths = new TreeSet<GrantedAuthorityName>();
            auths.add(GrantedAuthorityName.ROLE_ATTACHMENT_VIEWER);
            anonExisting.setAssignedUserGroups(auths);
            users.add(anonExisting);
          } else {
            // add this capability to the existing set of capabilities
            anonUser.getAssignedUserGroups().add(GrantedAuthorityName.ROLE_ATTACHMENT_VIEWER);
          }
        }

        SecurityServiceUtil.setStandardSiteAccessConfiguration( users, allGroups, cc ); 
        
        // GAE requires some settle time before these entries will be
        // accurately retrieved. Do not re-fetch the form after it has been
        // uploaded.
        resp.setStatus(HttpServletResponse.SC_OK);
        if (openRosaVersion == null) {
          // web page -- show HTML response
          resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
          resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
          PrintWriter out = resp.getWriter();

          StringBuilder headerString = new StringBuilder();
          headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
          headerString.append(cc.getWebApplicationURL(ServletConsts.AGGREGATE_STYLE));
          headerString.append("\" />");
          headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
          headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_BUTTON_STYLE_RESOURCE));
          headerString.append("\" />");
          headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
          headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_TABLE_STYLE_RESOURCE));
          headerString.append("\" />");
          headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
          headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_NAVIGATION_STYLE_RESOURCE));
          headerString.append("\" />");

          // header info
          beginBasicHtmlResponse(TITLE_INFO, headerString.toString(), resp, cc);
          if (warnings.length() != 0) {
            out.write("<p>users and capabilities .csv uploaded with warnings.</p>"
                + "<table>");
            out.write(warnings.toString());
            out.write("</table>");
          } else {
            out.write("<p>Successful users and capabilities .csv upload.</p>");
          }
          out.write("<p>Click ");
  
          out.write(HtmlUtil.createHref(cc.getWebApplicationURL(ADDR), "here", false));
          out.write(" to return to Upload users and capabilities .csv page.</p>");
          finishBasicHtmlResponse(resp);
        } else {
          addOpenRosaHeaders(resp);
          resp.setContentType(HtmlConsts.RESP_TYPE_XML);
          resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
          PrintWriter out = resp.getWriter();
          out.write("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">");
          if (warnings.length() != 0) {
            StringBuilder b = new StringBuilder();
            b.append("<p>users and capabilities .csv uploaded with warnings.</p>"
                + "<table>");
            b.append(warnings.toString());
            b.append("</table>");
            out.write("<message>");
            out.write(StringEscapeUtils.escapeXml10(b.toString()));
            out.write("</message>");
          } else {
            out.write("<message>Successful users and capabilities .csv upload.</message>");
          }
          out.write("</OpenRosaResponse>");
        }
  
      } catch (DatastoreFailureException e) {
        logger.error("users and capabilities .csv upload persistence error: " + e.toString());
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.toString());
      } catch (AccessDeniedException e) {
        logger.error("users and capabilities .csv upload access denied error: " + e.toString());
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            e.toString());
      } finally {
        if (csvReader != null ) {
          csvReader.close();
        }
        if (csvContentReader != null ) {
          csvContentReader.close();
        }
      }

    } catch (FileUploadException e) {
      logger.error("users and capabilities .csv upload persistence error: " + e.toString());
      e.printStackTrace(resp.getWriter());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.UPLOAD_PROBLEM);
    }
 }
}