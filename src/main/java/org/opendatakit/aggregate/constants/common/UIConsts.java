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
  public static final String VERSION_STRING = "v1.0.9 Production";

  public static final String URI_DEFAULT = "no uuid";
  public static final String FSC_URI_PARAM = "fsc";
  public static final String PREVIEW_PARAM = "previewImage";
  public static final String PREVIEW_SET = "&" + PREVIEW_PARAM + "=true";
  public static final String HOST_PAGE_BASE_ADDR = "Aggregate.html";
  public static final String VERTICAL_FLOW_PANEL_STYLENAME = "verticalFlowPanel";

  public static final String FILTER_NONE = "none";

  public static final String FORM_UPLOAD_SERVLET_ADDR = "formUpload";

  public static final String SUBMISSION_SERVLET_ADDR = "submission";
  public static final String ERROR_NO_FILTERS = "You need at least one filter to save a group.";
  public static final String ERROR_NO_NAME = "You need to provide a name for this filter group to continue";
  public static final String PROMPT_FOR_NAME_TXT = "Please enter a name for this group";
  public static final String REPROMPT_FOR_NAME_TXT = "That group already exists. Please enter a new name";

}
