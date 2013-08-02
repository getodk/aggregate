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

package org.opendatakit.common.web.constants;

/**
 * HTML Constants used in ODK aggregate to create proper HTML
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class HtmlConsts {

	// tag brackets
	public static final String BEGIN_OPEN_TAG = "<";
	public static final String BEGIN_CLOSE_TAG = "</";
	public static final String END_TAG = ">";
	public static final String END_SELF_CLOSING_TAG = "/>";

	// html
	public static final String SPACE = "&nbsp;";
	public static final String GREATER_THAN = "&gt;";
	public static final String LESS_THAN = "&lt;";
	public static final String TAB = SPACE + SPACE + SPACE;

	// html control tag names
	public static final String HTML = "html";
	public static final String BODY = "body";
	public static final String BREAK = "br";
	public static final String DIV = "div";
	public static final String TITLE = "title";
	public static final String FORM = "form";
	public static final String HEAD = "head";
	public static final String H1 = "h1";
	public static final String H2 = "h2";
	public static final String H3 = "h3";
	public static final String P = "p";
	public static final String PRE = "pre";
	public static final String B = "b";
	public static final String UL = "ul";
	public static final String LI = "li";
	public static final String TABLE = "table";
	public static final String TABLE_HEADER = "th";
	public static final String TABLE_ROW = "tr";
	public static final String TABLE_DATA = "td";
	public static final String CENTERING_DIV = "div align=\"CENTER\"";
	public static final String HEADING_TABLE_HEADER = "th align=\"LEFT\"";
	public static final String HEADING_IMAGE_TABLE_DATA = "td align=\"LEFT\" width=\"1%\"";
	public static final String HEADING_TABLE_DATA = "td align=\"LEFT\" width=\"10%\"";
	public static final String HEADING_RIGHT_TABLE_DATA = "td align=\"RIGHT\" width=\"10%\"";
	public static final String HEADING_HR = "hr width=\"100%\"";
	public static final String TITLE_HR = "hr size=\"3\" width=\"100%\"";

	// html formats
	public static final String TABLE_BORDER_PROPERTY = " border=\"1\"";
	public static final String PAGE_HEADING_TABLE_PROPERTIES = " width=\"100%\" cellspacing=\"5\"";
	public static final String HEADING_TABLE_PROPERTIES = " width=\"95%\" cellspacing=\"5\"";

	// table html control tags
	public static final String HTML_OPEN = HtmlStrUtil.createBeginTag(HTML);
	public static final String HTML_CLOSE = HtmlStrUtil.createEndTag(HTML);
	public static final String BODY_OPEN = HtmlStrUtil.createBeginTag(BODY);
	public static final String BODY_CLOSE = HtmlStrUtil.createEndTag(BODY);
	public static final String LINE_BREAK = HtmlStrUtil.createBeginTag(BREAK);
	public static final String FORM_CLOSE = HtmlStrUtil.createEndTag(FORM);

	public static final String PAGE_HEADING_TABLE_OPEN = BEGIN_OPEN_TAG + TABLE
	+ PAGE_HEADING_TABLE_PROPERTIES + END_TAG;
	public static final String HEADING_TABLE_OPEN = BEGIN_OPEN_TAG + TABLE
			+ HEADING_TABLE_PROPERTIES + END_TAG;
	public static final String BORDERLESS_TABLE_OPEN = BEGIN_OPEN_TAG + TABLE + END_TAG;
	public static final String TABLE_OPEN = BEGIN_OPEN_TAG + TABLE
			+ TABLE_BORDER_PROPERTY + END_TAG;
	public static final String TABLE_CLOSE = HtmlStrUtil.createEndTag(TABLE);
	public static final String TABLE_ROW_OPEN = HtmlStrUtil
			.createBeginTag(TABLE_ROW);
	public static final String TABLE_ROW_CLOSE = HtmlStrUtil
			.createEndTag(TABLE_ROW);
	public static final String TABLE_DATA_CLOSE = HtmlStrUtil
			.createEndTag(TABLE_DATA);

	// input types
	public static final String INPUT_TYPE_SUBMIT = "submit";
	public static final String INPUT_TYPE_FILE = "file";
	public static final String INPUT_TYPE_TEXT = "text";
	public static final String INPUT_TYPE_PASSWORD = "password";
	public static final String INPUT_TYPE_HIDDEN = "hidden";
	public static final String INPUT_TYPE_RADIO = "radio";

	public static final int WEB_PORT = 80;
	public static final int SECURE_WEB_PORT = 443;
	public static final String CHECKBOX_HEADER = "Select";
	public static final String INPUT_TYPE_CHECKBOX = "checkbox";
	public static final String CHECKED = "checked";
	/**
	 * Constant for "text/enriched" content type for response message
	 */
	public static final String RESP_TYPE_ENRICHED = "text/enriched; charset=utf-8";
	/**
	 * Constant for "text/html" content type for response message
	 */
	public static final String RESP_TYPE_HTML = "text/html; charset=utf-8";
	/**
	 * Constant for "text/plain" content type for response message
	 */
	public static final String RESP_TYPE_PLAIN = "text/plain; charset=utf-8";
	/**
	 * Constant for "image/jpeg" content type for response message
	 */
	public static final String RESP_TYPE_IMAGE_JPEG = "image/jpeg";
	/**
	 * Constant for "text/xml" content type for response message
	 */
	public static final String RESP_TYPE_XML = "text/xml; charset=utf-8";
	
	  /**
    * Constant for "text/csv" content type for response message
    */
   public static final String RESP_TYPE_CSV = "text/csv; charset=utf-8";

   public static final String RESP_TYPE_KML = "application/vnd.google-earth.kml+xml; charset=utf-8";
   
   public static final String RESP_TYPE_JSON = "application/json; charset=utf-8";
   
	/**
	 * Encoding scheme for servlets
	 */
	public static final String UTF8_ENCODE = "UTF-8";
	// types of posts
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String POST = "POST";
	public static final String GET = "GET";
	public static final String PUT = "PUT";
	public static final String BEGIN_PARAM = "?";
	public static final String PARAM_DELIMITER = "&";
	public static final String ATTACHMENT_FILENAME_TXT = "attachment; filename=\"";
	public static final String CONTENT_DISPOSITION = "Content-Disposition";

}
