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

package org.opendatakit.aggregate.constants;


/**
 * Constant values used in ODK aggregate to aid with servlet management
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public final class ServletConsts {

  // TODO: remove temporary debug variable
  /**
   * TEMPORARY: used for turning on and off debug code
   */
  public final static boolean DEBUG = false;

  // system constants
  public static final String APPLICATION_NAME = "ODK AGGREGATE";

  public static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
  public static final String OPEN_ROSA_VERSION = "1.0";

  public static final String OPEN_ROSA_ACCEPT_CONTENT_LENGTH_HEADER = "X-OpenRosa-Accept-Content-Length";

  /**
   * Flag on submissions and form uploads indicating that this is
   * a partial submission or form upload.
   */
  public static final String TRANSFER_IS_INCOMPLETE = "*isIncomplete*";
  /**
   * Name of form field that contains XML submission
   */
  public static final String XML_SUBMISSION_FILE = "xml_submission_file";
  /**
   * Name of form field that contains the form name value (form upload)
   */
  public final static String FORM_NAME_PRAM = "form_name";
  /**
   * Name of form field that contains the xform xml definittion (form upload)
   */
  public final static String FORM_DEF_PRAM = "form_def_file";

  /**
   * The name of the property that includes the form id
   */
  public static final String FORM_ID = "formId";

  /**
   * For OdkTables:
   * The argument for a tableId.
   */
  public static final String TABLE_ID = "tableId";
  public static final String ODK_TABLES_SERVLET_BASE_PATH = "odktables";

  /**
   * For PersistentResults and MiscTasks generator gae servlets.
   * the key holding the Uri of the persistent result or misc task record.
   */
  public static final String PERSISTENT_RESULTS_KEY = "persistentResult";

  public static final String MISC_TASKS_KEY = "miscTask";

  public static final String ATTEMPT_COUNT = "attemptCount";

  public static final String BACKEND_GAE_SERVICE = "background";

  public static final String HOST = "Host";
  /**
   * The name of the property that determines how to format webpage
   */
  public static final String HUMAN_READABLE = "readable";

  /**
   * The name of the property that specifies the type of interaction with an
   * external service
   */
  public static final String EXTERNAL_SERVICE_TYPE = "externalServiceType";

  // href link text
  public static final String BRIEFCASE_LINK_TEXT = "Download Entire Dataset (Briefcase)";
  public static final String UPLOAD_SUBMISSIONS_LINK_TEXT = "Upload Submissions";
  public static final String UPLOAD_XFORM_LINK_TEXT = "Upload a Form Definition";

  public static final String BLOB_KEY = "blobKey";

  public static final String AS_ATTACHMENT = "as_attachment";

  public static final String OAUTH_CONSUMER_KEY = "anonymous";

  public static final String OAUTH_CONSUMER_SECRET = "anonymous";

  public static final String OAUTH_TOKEN_PARAMETER = "oauth_token";

  public static final String OAUTH_TOKEN_SECRET_PARAMETER = "oauth_token_secret";

  public static final String DOWNLOAD_XML_BUTTON_TXT = "Download XML";

  public static final String CSV_FILENAME_APPEND = "_results.csv";

  public static final String KML_FILENAME_APPEND = "_results.kml";

  public static final String JSON_FILENAME_APPEND = "_results.json";

  public static final String RECORD_KEY = "record";

  public static final int EXPORT_CURSOR_CHUNK_SIZE = 100;
  /**
   * The name of the parameter that specifies the cursor location for retrieving
   * data from the data table (fragmented Csv servlet)
   */
  public static final String CURSOR = "cursor";
  /**
   * The name of the parameter that specifies how many rows to return from the
   * cursor (fragmented Csv servlet).
   */
  public static final String NUM_ENTRIES = "numEntries";

  public static final String CHECK_INTERVAL_PARAM = "checkIntervalMilliseconds";

  public static final String START_DATE = "startDate";

  /**
   * Script path to include...
   */
  public static final String UPLOAD_SCRIPT_RESOURCE = "javascript/upload_control.js";

  public static final String UPLOAD_STYLE_RESOURCE = "stylesheets/upload.css";

  public static final String UPLOAD_BUTTON_STYLE_RESOURCE = "stylesheets/button.css";

  public static final String AGGREGATE_STYLE = "AggregateUI.css";

}
