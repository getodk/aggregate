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

import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;

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

  // TODO: remove constant and turn into a property file that is only present on
  // version
  /**
   * Current version of Aggregate
   */
  public final static String VERSION = "v1.x ALPHA TWO";

  // system constants
  public static final String APPLICATION_NAME = "ODK AGGREGATE";

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
   * For PersistentResults generator gae servlets, the key holding the Uri of the persistent result.
   */
  public static final String PERSISTENT_RESULTS_KEY = "persistentResult";

  public static final String ATTEMPT_COUNT = "attemptCount";

  /**
   * The name of the property that determines how to format webpage
   */
  public static final String HUMAN_READABLE = "readable";

  /**
   * The name of the property that specifies the type of interaction with an
   * external service
   */
  public static final String EXTERNAL_SERVICE_TYPE = "externalServiceType";
  // web addresses
  public static final String WEB_ROOT = "/";

  // href link text
  public static final String FORMS_LINK_TEXT = "List Forms";
  public static final String UPLOAD_SUB_LINK_TEXT = "Upload a Submission";
  public static final String UPLOAD_FORM_LINK_TEXT = "Upload Form";
  public static final String DELETE_FORM_LINK_TEXT = "Delete Form";
  public static final String BRIEFCASE_LINK_TEXT = "Briefcase";
  public static final String UPLOAD_APPLET_LINK_TEXT = "Upload Submissons from a Phone";
  public static final String RESULT_FILES_LINK_TEXT = "Downloadable Data Files";


  public static final String BLOB_KEY = "blobKey";

  public static final String AS_ATTACHMENT = "as_attachment";

  public static final String INDEX = "index";

  public static final String BACKWARD = "backward";

  public static final String AUTHENTICATION = "auth";
  
  public static final String AUTHENTICATION_OAUTH = "oauth";

  public static final String OAUTH_CONSUMER_KEY = "anonymous";
  
  public static final String OAUTH_CONSUMER_SECRET = "anonymous";
  
  public static final String OAUTH_TOKEN_PARAMETER = "oauth_token";
  
  public static final String OAUTH_TOKEN_SECRET_PARAMETER = "oauth_token_secret";
  
  public static final int MAX_ENTITY_PER_PAGE = 20;

  public static final String NEXT_LINK_TEXT = " (Next " + MAX_ENTITY_PER_PAGE + " Results) NEXT"
      + HtmlConsts.GREATER_THAN + HtmlConsts.GREATER_THAN;

  public static final String BACK_LINK_TEXT = HtmlConsts.LESS_THAN + HtmlConsts.LESS_THAN
      + "BACK (Previous " + MAX_ENTITY_PER_PAGE + " Results)";

  public static final String DOWNLOAD_XML_BUTTON_TXT = "Download XML";

  public static final String CSV_FILENAME_APPEND = "_results.csv";

  public static final String KML_FILENAME_APPEND = "_results.kml";

  public static final String PROCESS_RECORD_PREFIX = "record";

  public static final String PROCESS_TYPE = "processType";

  public static final String PROCESS_NUM_RECORDS = "numRecords";

  public static final String QUERY_VALUE_PARAM = "value";

  public static final String QUERY_OP_PARAM = "operation";

  public static final String QUERY_FIELD_PARAM = "field";

  public static final int FORM_DELETE_RECORD_QUERY_LIMIT = 20;

  // TODO: revise so no retrieval form limit
  public static final int FETCH_LIMIT = 1000;

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

  /**
   * When constructing up an odkId, allow xpath-style restrictions on 'key'
   * attribute of the form identity and repeating elements.
   */
  public static final String ELEMENT_REFERENCE_KEY_BEGIN_STRING = "[@key=" + BasicConsts.QUOTE;
  public static final String ELEMENT_REFERENCE_KEY_END_STRING = BasicConsts.QUOTE + "]";

  public static final String HTTP = "http://";

  public static final String AUTH_SUB_SCOPE = "Scope";
  
  public static final String CHECK_INTERVAL_PARAM = "checkIntervalMilliseconds";
  
}
