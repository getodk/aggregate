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

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Static HTML utility functions used to generate proper HTML for ODK Aggregate
 * visual outputs
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class HtmlUtil extends org.opendatakit.common.utils.HtmlUtil {

  private static final String LOST_FORM_RE_ENCODING = "We lost the form somehow! Please report the bug!";

  public static String encodeFormInHiddenInput(String formXml, String xmlFileName) throws IOException {

    if (formXml == null) {
      throw new IOException(LOST_FORM_RE_ENCODING);
    }

    if(xmlFileName == null){
      xmlFileName = "default.xml";
    }
    
    StringBuilder html = new StringBuilder();
    html.append(HtmlConsts.BEGIN_OPEN_TAG + INPUT);
    html.append(BasicConsts.SPACE);
    html.append(createAttribute(ATTR_TYPE, HtmlConsts.INPUT_TYPE_HIDDEN));
    html.append(BasicConsts.SPACE);
    html.append(createAttribute(ATTR_NAME, ServletConsts.FORM_DEF_PRAM));
    html.append(BasicConsts.SPACE);
    html.append(createAttribute(ATTR_VALUE, StringEscapeUtils.escapeHtml4(formXml)));
    html.append(BasicConsts.SPACE);
    html.append(createAttribute(ATTR_SRC, xmlFileName));
    html.append(BasicConsts.SPACE);
    html.append(HtmlConsts.END_SELF_CLOSING_TAG);
    return html.toString();
  }

  public static String wrapResultTableWithHtmlTags(boolean addCheckboxes, String checkboxName, List<String> headers, List<Row> rows) {
    StringBuilder html = new StringBuilder();
    html.append(HtmlConsts.TABLE_OPEN);

    if (addCheckboxes) {
      html.append(wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, HtmlConsts.CHECKBOX_HEADER));
    }
    
    for (String header : headers) {
      html.append(wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header));
    }

    for (int recordNum = 0; recordNum < rows.size(); recordNum++) {
      Row row = rows.get(recordNum);
      html.append(HtmlConsts.TABLE_ROW_OPEN);
      if (addCheckboxes) {
        html.append(wrapWithHtmlTags(HtmlConsts.TABLE_DATA, createInput(
            HtmlConsts.INPUT_TYPE_CHECKBOX, checkboxName, 
            		row.getSubmissionKey().toString())));
      }
      for (Object item : row.getFormattedValues()) {
        if(item != null) {
          html.append(wrapWithHtmlTags(HtmlConsts.TABLE_DATA, item.toString()));
        } else {
          html.append(wrapWithHtmlTags(HtmlConsts.TABLE_DATA, BasicConsts.EMPTY_STRING));
        }
      }
      html.append(HtmlConsts.TABLE_ROW_CLOSE);
    }
    html.append(HtmlConsts.TABLE_CLOSE);

    return html.toString();
  }

}
