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
 * Static HTML utility functions used to generate proper HTML for ODK Aggregate
 * visual outputs
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class HtmlStrUtil {

  private static final int INPUT_WIDGET_SIZE_LIMIT = 80;// same size as URI...

  protected static final String HREF = "href";
  protected static final String A = "a";
  protected static final String INPUT = "input";
  protected static final String ATTR_VALUE = "value";
  protected static final String ATTR_NAME = "name";
  protected static final String ATTR_TYPE = "type";
  protected static final String ATTR_METHOD = "method";
  protected static final String ATTR_ACCEPT_CHARSET = "accept-charset";
  protected static final String ATTR_ENCTYPE = "enctype";
  protected static final String ATTR_ACTION = "action";
  protected static final String ATTR_SIZE = "size";
  protected static final String ATTR_CHECKED = "checked";
  protected static final String ATTR_SRC = "src";

  public static final String createEndTag(String tag) {
    return HtmlConsts.BEGIN_CLOSE_TAG + tag + HtmlConsts.END_TAG;
  }

  public static final String createBeginTag(String tag) {
    return HtmlConsts.BEGIN_OPEN_TAG + tag + HtmlConsts.END_TAG;
  }

  public static final String createSelfClosingTag(String tag) {
    return HtmlConsts.BEGIN_OPEN_TAG + tag + HtmlConsts.END_SELF_CLOSING_TAG;
  }

  public static final String createAttribute(String name, String value) {
    return name + BasicConsts.EQUALS + BasicConsts.QUOTE + value + BasicConsts.QUOTE;
  }

  public static final String wrapWithHtmlTags(String htmlTag, String text) {
    return createBeginTag(htmlTag) + text + createEndTag(htmlTag);
  }

  public static final String createHref(String url, String displayText) {
    return HtmlConsts.BEGIN_OPEN_TAG + A + BasicConsts.SPACE + createAttribute(HREF, url)
        + HtmlConsts.END_TAG + displayText + createEndTag(A);
  }

  public static final String createInput(String type, String name, String value, boolean isChecked, int size, String extras) {
    StringBuilder html = new StringBuilder();
    html.append(HtmlConsts.BEGIN_OPEN_TAG + INPUT);
    if (type != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_TYPE, type));
    }
    if (name != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_NAME, name));
    }
    if (value != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_VALUE, value));
    }
    html.append(BasicConsts.SPACE);
    html.append(createAttribute(ATTR_SIZE, Integer.toString(size)));
    
    if ( extras != null) {
    	html.append(BasicConsts.SPACE);
    	html.append(extras);
    }
    
    if ( isChecked ) {
    	html.append(BasicConsts.SPACE);
    	html.append(ATTR_CHECKED);
    }
    html.append(HtmlConsts.END_SELF_CLOSING_TAG);
    return html.toString();
  }

  public static final String createInput(String type, String name, String value, int size) {
	  return createInput(type, name, value, false, size, null);
  }

  public static final String createInput(String type, String name, String value) {
	  return createInput(type, name, value, false, INPUT_WIDGET_SIZE_LIMIT, null);
  }
  
  public static final String createNonSavingPasswordInput(String name) {
	  return createInput(HtmlConsts.INPUT_TYPE_PASSWORD, name, "", 
			  				false, INPUT_WIDGET_SIZE_LIMIT, "autocomplete=\"off\"");
  }
  
  public static final String createInput(String type, String name, String value, boolean checked) {
    return createInput(type, name, value, checked, INPUT_WIDGET_SIZE_LIMIT, null);
  }
  
  public static final String createFormBeginTag(String action, String encodingType, String method) {
    StringBuilder html = new StringBuilder();
    html.append(HtmlConsts.BEGIN_OPEN_TAG + HtmlConsts.FORM);
    if (action != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_ACTION, action));
    }
    if (encodingType != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_ENCTYPE, encodingType));
    }
    if (method != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_METHOD, method));
    }
    html.append(BasicConsts.SPACE);
    html.append(createAttribute(ATTR_ACCEPT_CHARSET, HtmlConsts.UTF8_ENCODE));
    html.append(HtmlConsts.END_TAG);
    return html.toString();
  }
}
