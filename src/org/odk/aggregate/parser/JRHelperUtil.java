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

package org.odk.aggregate.parser;

import java.util.Arrays;
import java.util.List;

import org.odk.aggregate.constants.HtmlConsts;

/**
 * Contains helpful utility functions to make java rosa parsing easier
 *
 * @author wbrunette@gmail.com
 *
 */
public class JRHelperUtil {

  /**
   * List of XML tags that should be removed to be java rosa compliant
   */
  protected static List<String> NON_JAVA_ROSA_COMPLAINT_TAGS =
      Arrays.asList("submit", "submission", "message");

  /**
   * Removes XML tags that are not Java Rosa compilant
   * 
   * @param xml
   *    xform xml definition
   * @return
   *    java rosa acceptable xml tags xform defintion
   */
  public static String removeNonJavaRosaCompliantTags(String xml) {
    String cleanXml = xml;
    for (String tag : NON_JAVA_ROSA_COMPLAINT_TAGS) {
      String startTag = HtmlConsts.BEGIN_OPEN_TAG + tag;
      while (cleanXml.contains(startTag)) {
        int startTagIndex = cleanXml.indexOf(startTag);
        int endIndex = -1;
        
        // check for end tag, if not found check for self closing
        String endTag = HtmlConsts.BEGIN_CLOSE_TAG + tag + HtmlConsts.END_TAG;
        int endTagIndex = cleanXml.indexOf(endTag);
        if(endTagIndex > -1) {
          endIndex = endTagIndex + endTag.length();
        } else {
          int endSelfClosingIndex = cleanXml.indexOf(HtmlConsts.END_SELF_CLOSING_TAG, startTagIndex);
          int startNextTagIndex = cleanXml.indexOf(HtmlConsts.BEGIN_OPEN_TAG, startTagIndex+2);
          if(endSelfClosingIndex > -1 && (startNextTagIndex > endSelfClosingIndex || startNextTagIndex == -1)) {
            endIndex = endSelfClosingIndex + HtmlConsts.END_SELF_CLOSING_TAG.length();
          }
        }
        
        if (startTagIndex > -1 && endIndex > -1) {
          cleanXml = cleanXml.substring(0, startTagIndex) + cleanXml.substring(endIndex);
        } else { 
          // unable to properly remove tag, therefore break
          break;
        }
      }
    }
    return cleanXml;
  }
}
