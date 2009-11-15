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
 * HTML Constants used in ODK aggregate to create proper HTML
 *  
 * @author wbrunette@gmail.com
 *
 */
public class HtmlConsts {
  
  // tag brackets
  public static final String BEGIN_OPEN_TAG = "<";
  public static final String BEGIN_CLOSE_TAG = "</";
  public static final String END_TAG = ">";
  public static final String END_SELF_CLOSING_TAG = "/>";
  
  // html
  public static final String SPACE = "&nbsp;";
  public static final String GREATER_THAN = "&gt;";
  public static final String LESS_THAN = "&lt;";
  public static final String TAB = SPACE+SPACE+SPACE;
  
  // html control tag names
  public static final String HTML = "html";
  public static final String BODY = "body";
  public static final String BREAK = "br";
  public static final String TITLE = "title";
  public static final String FORM = "form";
  public static final String HEAD = "head";
  public static final String H1 = "h1";
  public static final String H2 = "h2";
  public static final String H3 = "h3";
  public static final String P = "p";
  public static final String TABLE = "table";
  public static final String TABLE_HEADER = "th";
  public static final String TABLE_ROW = "tr";
  public static final String TABLE_DATA = "td";
  
  // html formats
  public static final String TABLE_BORDER_PROPERTY = " border=\"1\"";
  
  // table html control tags
  public static final String HTML_OPEN = HtmlUtil.createBeginTag(HTML);
  public static final String HTML_CLOSE = HtmlUtil.createEndTag(HTML);
  public static final String BODY_OPEN = HtmlUtil.createBeginTag(BODY);
  public static final String BODY_CLOSE = HtmlUtil.createEndTag(BODY);
  public static final String LINE_BREAK = HtmlUtil.createBeginTag(BREAK);
  public static final String FORM_CLOSE = HtmlUtil.createEndTag(FORM);
  
  public static final String TABLE_OPEN = BEGIN_OPEN_TAG + TABLE + TABLE_BORDER_PROPERTY + END_TAG;
  public static final String TABLE_CLOSE = HtmlUtil.createEndTag(TABLE);
  public static final String TABLE_ROW_OPEN = HtmlUtil.createBeginTag(TABLE_ROW);
  public static final String TABLE_ROW_CLOSE = HtmlUtil.createEndTag(TABLE_ROW);
  
  // input types
  public static final String INPUT_TYPE_SUBMIT = "submit";
  public static final String INPUT_TYPE_FILE = "file";
  public static final String INPUT_TYPE_TEXT = "text";
  public static final String INPUT_TYPE_HIDDEN = "hidden";
  public static final String INPUT_TYPE_RADIO = "radio";
  
  public static final String HTTP = "http://";
  public static final int WEB_PORT = 80;
  public static final String CHECKBOX_HEADER = "Select";
  static final String INPUT_TYPE_CHECKBOX = "checkbox";
  static final String CHECKED = "checked";
  
}
