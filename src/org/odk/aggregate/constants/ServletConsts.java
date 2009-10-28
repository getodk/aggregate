/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.constants;

/**
 * Constant values used in ODK aggregate to aid with servlet management 
 *  
 * @author wbrunette@gmail.com
 *
 */
public class ServletConsts {

  // TODO: remove temporary debug variable
  /**
   * TEMPORARY: used for turning on and off debug code
   */
  public final static boolean DEBUG = false;

  
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
   * The name of the property that includes the ODK form key
   */
  public static final String ODK_FORM_KEY = "odkFormKey";
  /**
   * The name of the property that includes the ODK id
   */
  public static final String ODK_ID = "odkId";
  /**
   * The name of the property that includes the submission key
   */
  public static final String SUBMISSION_KEY = "submissionKey";
  /**
   * The name of the property that includes the entity property name
   */
  public static final String PROPERTY_NAME = "propertyName";
  /**
   * The name of the property that determines how to format webpage
   */
  public static final String HUMAN_READABLE = "readable";
  /**
   * The name of the property that includes the key of the parent submission set
   */
  public static final String PARENT_KEY = "parentKey";
  /**
   * The name of the property that includes the form element key
   */
  public static final String FORM_ELEMENT_KEY = "formElementKey";
  /**
   * The name of the property that specifies the entity kind
   */
  public static final String KIND = "kind";
  /**
   * The name of the property that specifies URL of the page that requested the new page
   */
  public static final String BACK_URL = "backUrl";
  /**
   * The name of the property that specifies the type of interaction with an external service
   */
  public static final String EXTERNAL_SERVICE_TYPE = "externalServiceType";
  /**
   * The name of the property that specifies the session key for the doc service
   */
  public static final String DOC_AUTH = "docAuth";
  /**
   * The name of the property that specifies the session key for the doc service
   */
  public static final String SPREAD_AUTH = "spreadAuth";
  
  
  
  
  /**
   * Constant for "text/enriched" content type for response message
   */
  public static final String RESP_TYPE_ENRICHED = "text/enriched";
  /**
   * Constant for "text/html" content type for response message
   */
  public static final String RESP_TYPE_HTML = "text/html";
  /**
   * Constant for "text/plain" content type for response message
   */
  public static final String RESP_TYPE_PLAIN = "text/plain";
  /**
   * Constant for "image/jpeg" content type for response message
   */  
  public static final String RESP_TYPE_IMAGE_JPEG = "image/jpeg";
  /**
   * Constant for "text/xml" content type for response message
   */  
  public static final String RESP_TYPE_XML = "text/xml";
  
  
  /**
   * Encoding scheme for servlets
   */
  public static final String ENCODE_SCHEME = "UTF-8";

  
  public static final String BEGIN_PARAM = "?";
  public static final String PARAM_DELIMITER = "&";
  
  // types of posts
  public static final String MULTIPART_FORM_DATA = "multipart/form-data";
  public static final String POST = "post";
  public static final String GET = "get";
  
  // web addresses
  public static final String UPLOAD_SUBMISSION_ADDR = "UploadSubmission.html";
  public static final String WEB_ROOT = "/";


  // href link text
  public static final String FORMS_LINK_TEXT = "List of Forms";
  public static final String UPLOAD_SUB_LINK_TEXT = "Upload a Submission";
  public static final String UPLOAD_FORM_LINK_TEXT = "Upload Form";


  public static final String BLOB_KEY = "blobKey";


  public static final String INDEX = "index";


  public static final String BACKWARD = "backward";


  public static final int MAX_ENTITY_PER_PAGE = 20;

  
  public static final String NEXT_LINK_TEXT = " (Next " + MAX_ENTITY_PER_PAGE + " Results) NEXT"+HtmlConsts.GREATER_THAN+HtmlConsts.GREATER_THAN;

  
  public static final String BACK_LINK_TEXT = HtmlConsts.LESS_THAN + HtmlConsts.LESS_THAN +"BACK (Previous " + MAX_ENTITY_PER_PAGE + " Results)";

  
  public static final String DOWNLOAD_XML_BUTTON_TXT = "Download XML";

  
  public static final String ATTACHMENT_FILENAME_TXT = "attachment; filename=\"";

  
  public static final String CONTENT_DISPOSITION = "Content-Disposition";

  
  public static final String CSV_FILENAME_APPEND = "_results.csv";
  
  public static final String KML_FILENAME_APPEND = "_results.kml";

  public static final String CREATE_EXTERNAL_SERVICE_BUTTON_LABEL = "Create Connection To External Service";


  public static final String SPREADSHEET_NAME_PARAM = "SpreadsheetName";


  public static final String SPEADSHEET_NAME_LABEL = "Name of Spreadsheet:";


  public static final String TOKEN_PARAM = "TOKEN";


  public static final String ODK_PERMANENT_ACCESS_WARNING = "NOTE: A selection of 'CONTINUOUS' means ODK Aggregate will maintian permanent access to the spreadsheet";


  public static final String CONTINUOUS_DATA_TRANSFER_BUTTON_TXT = "Create Worksheet with 'CONTINUOUS' Data Transfer";


  public static final String ONE_TIME_DATA_TRANSFER_BUTTON_TXT = "Create Worksheet with 'ONE TIME' Data Transfer";


  public static final String SPREADSHEET_SCOPE = "http://spreadsheets.google.com/feeds/";


  public static final String WORKSHEETS_FEED_PREFIX = SPREADSHEET_SCOPE + "worksheets/";
  
  public static final String FEED_PERMISSIONS = "/private/full";


  public static final String DOCS_SCOPE = "http://docs.google.com/feeds/";
  

  public static final String DOC_FEED = DOCS_SCOPE + "documents" + FEED_PERMISSIONS;


  public static final String DOCS_PRE_KEY = "spreadsheet%3A";


  public static final String AUTHORIZE_SPREADSHEET_CREATION = "Authorize Doc Service for Spreadsheet Creation";


  public static final String GOOGLE_DOC_EXPLANATION = "Create Google Doc Spreadsheet for Form: ";


  public static final String CONTINUOUS_TRANSFER_PARAM = "ContinuousTransfer";


  public static final String AUTHORIZE_DATA_TRANSFER_BUTTON_TXT = "Authorize Spreadsheet Service to transfer Data to Spreadsheet";


  public static final String SPREADSHEET_EXPLANATION = "Populate Spreadsheet with Data from Form: ";


  public static final String ALREADY_EXISTS_PARAM = "alreadyExists";
  
}
