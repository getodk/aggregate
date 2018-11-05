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
package org.opendatakit.aggregate.constants.format;

import org.opendatakit.aggregate.constants.HtmlUtil;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class FormTableConsts {

  // form table headers
  public static final String FT_HEADER_XFORM = "Xform Definition";
  public static final String FT_HEADER_CSV = "Submissions in CSV";
  public static final String FT_HEADER_RESULTS = "Submission Results";
  public static final String FT_HEADER_USER = "User";
  public static final String FT_HEADER_FORM_ID = "Identifier";
  public static final String FT_HEADER_NAME = "Name";
  public static final String FT_HEADER_KML = "KML File";
  public static final String FT_HEADER_EXTERNAL_SERVICE = "Send Submissions to External Service";
  public static final String FT_HEADER_QUERY = "Query Results";

  // button text
  public static final String XML_BUTTON_TXT = "View XML";
  public static final String CSV_BUTTON_TXT = "Create CSV File";
  public static final String RESULTS_BUTTON_TXT = "View Submissions";
  public static final String EXTERNAL_SERVICE_BUTTON_TXT = "Add External Service Connection";
  public static final String KML_BUTTON_TXT = "Create KML File";
  public static final String QUERY_BUTTON_TXT = "Query";
  // xml form list tags
  public static final String URL_ATTR = "url";
  // link text
  public static final String VIEW_LINK_TEXT = "View";
  public static final String DOWNLOAD_LINK_TEXT = "Download";
  public static final String FORMS_TAG = "forms";
  public static final String FORM_TAG = "form";
  public static final String BEGIN_FORMS_TAG = HtmlUtil.createBeginTag(FORMS_TAG);
  public static final String END_FORMS_TAG = HtmlUtil.createEndTag(FORMS_TAG);
  public static final String END_FORM_TAG = HtmlUtil.createEndTag(FORM_TAG);


}
