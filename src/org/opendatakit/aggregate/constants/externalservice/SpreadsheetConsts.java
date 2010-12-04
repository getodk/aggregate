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
package org.opendatakit.aggregate.constants.externalservice;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class SpreadsheetConsts {

  public static final String SPEADSHEET_NAME_LABEL = "Name of Spreadsheet:";
	/**
	 * The Google feed permissions. Of the form '/visibility/projection/'. <BR/>
	 * See http://code.google.com/apis/spreadsheets/data/3.0/reference.html#
	 * Visibility and
	 * http://code.google.com/apis/spreadsheets/data/3.0/reference
	 * .html#Projection for more info.
	 */
  public static final String FEED_PERMISSIONS = "/private/full/";
  public static final String SPREADSHEETS_SCOPE = "https://spreadsheets.google.com/feeds/";
	/**
	 * The spreadsheets feed prefix. <BR/> 
	 * Spreadsheets metafeed url: SPREADSHEETS_FEED <BR/>
	 * Specific spreadsheet url: SPREADSHEETS_FEED + spreadsheetKey <BR/>
	 * See http://code.google.com/apis/spreadsheets/data/3.0/reference.html#Feeds
	 * for more info.
	 */
  public static final String SPREADSHEETS_FEED = SPREADSHEETS_SCOPE + "spreadsheets" + FEED_PERMISSIONS;
  /**
   * The worksheets feed prefix.<BR/>
   * Worksheets metafeed url: WORKSHEETS_FEED + spreadsheetKey + FEED_PERMISSIONS <BR/>
   * Specific worksheet url: WORKSHEETS_FEED + spreadsheetKey + FEED_PERMISSIONS + worksheetId <BR/>
   * See http://code.google.com/apis/spreadsheets/data/3.0/reference.html#Feeds
   * for more info.
   */
  public static final String WORKSHEETS_FEED = SPREADSHEETS_SCOPE + "worksheets/";
  public static final String DOCS_SCOPE = "http://docs.google.com/feeds/";
  public static final String DOC_FEED = DOCS_SCOPE + "default" + FEED_PERMISSIONS;
  public static final String AUTHORIZE_SPREADSHEET_CREATION = "Authorize Doc Service for Spreadsheet Creation";
  public static final String GOOGLE_DOC_EXPLANATION = "Create Google Doc Spreadsheet for Form: ";
  public static final String DOCS_PRE_KEY = "spreadsheet%3A";
  
  /**
   * The name of the property that specifies the session key for the doc and spreadsheets services
   */
  public static final String OAUTH_TOKEN = "oauthToken";
  public static final String OAUTH_TOKEN_SECRET = "oauthTokenSecret";

  // bean constant
  public static final String WORKSHEET_BEAN = "worksheet_creator";

  public static final int WORKSHEET_CREATION_DELAY = 15000;
  public static final String COMPLETED_AUTH = "Completed Authorization <br>";
  
  public static final String UNSUPPORTED_CHAR_CLASS = "[*]";
  public static final String REPLACEMENT_CHAR = "-";
}
