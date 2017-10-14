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

package org.opendatakit.aggregate.constants.common;

public class UIConsts {
  public static final String VERSION_STRING = "v1.4.15 Production";

  public static final String KML_NONE_OPTION = "None";
  public static final String KML_NONE_ENCODE_KEY = "*NONE*";
  
  public static final String URI_DEFAULT = "no uuid";
  public static final String FSC_URI_PARAM = "fsc";
  public static final String PREVIEW_PARAM = "previewImage";
  public static final String PREVIEW_SET = "&" + PREVIEW_PARAM + "=true";
  public static final String PREVIEW_IMAGE_STYLENAME = "thumbnail";
  public static final String HOST_PAGE_BASE_ADDR = "Aggregate.html";
  public static final String VERTICAL_FLOW_PANEL_STYLENAME = "verticalFlowPanel";

  public static final String FILTER_NONE = "none";

  public static final String FORM_UPLOAD_SERVLET_ADDR = "formUpload";
  public static final String USERS_AND_PERMS_UPLOAD_SERVLET_ADDR = "ssl/reset-users-and-permissions";
  public static final String GET_USERS_AND_PERMS_CSV_SERVLET_ADDR = "ssl/get-users-and-permissions";
  
  public static final String SERVICE_ACCOUNT_PRIVATE_KEY_UPLOAD_ADDR = "ssl/oauth2-service-account";

  public static final String ENKETO_SERVICE_ACCOUNT_PRIVATE_KEY_UPLOAD_ADDR = "ssl/enketo-service-account";
  public static final String ENKETO_API_HANDLER_ADDR = "enk/enketoApiHandler";

  // url pattern for uploading files associated with ODKTables tables
  public static final String APP_LEVEL_FILE_UPLOAD_SERVLET_ADDR =
      "appLevelFileUpload";
  // url pattern for uploading files associated with ODKTables tables
  public static final String TABLE_FILE_UPLOAD_SERVLET_ADDR =
      "tableFileUpload";
  // url pattern for downloading files associated with odktables tables
  public static final String TABLE_FILE_DOWNLOAD_SERVLET_ADDR =
      "tableFileDownload";
  // url pattern for uploading a table from a CSV file
  public static final String UPLOAD_TABLE_FROM_CSV_SERVLET_ADDR =
      "uploadTableFromCSV";

  public static final String SUBMISSION_SERVLET_ADDR = "submission";
  public static final String ERROR_NO_FILTERS = "You need at least one filter to save a group.";
  public static final String ERROR_NO_NAME = "You need to provide a name for this filter group to continue";
  public static final String PROMPT_FOR_NAME_TXT = "Please enter a name for this group";
  public static final String REPROMPT_FOR_NAME_TXT = "That group already exists. Please enter a new name";
  public static final String PROMPT_FOR_REDCAP_APIKEY_TXT = "Please enter the REDCap API Key to access this server";
  public static final String REPROMPT_FOR_REDCAP_APIKEY_TXT = "That is not a valid REDCap API Key. Please enter the REDCap API Key to access this server";
  public static final String PROMPT_FOR_EMAIL_TXT = "Please enter the e-mail address that will be granted access to these documents";
  public static final String REPROMPT_FOR_EMAIL_TXT = "That is not a valid e-mail address. Please enter the e-mail address that will be granted access to these documents";

}
