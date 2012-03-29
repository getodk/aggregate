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

import java.util.Date;

/**
 * Constants used in ODK aggregate that are shared everywhere
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class BasicConsts {

  public static final String NULL = "NULL";
  
  // general constants
  public static final String NEW_LINE = "\n";
  public static final String COMMA = ",";
  public static final String SPACE = " ";
  public static final String TAB = "\t";
  public static final String GREATER_THAN = ">";
  public static final String LESS_THAN = "<";
  public static final String EQUALS = "=";
  public static final String EMPTY_STRING = "";
  public static final String FORWARDSLASH = "/";
  public static final String TRUE = "true";
  public static final String QUOTE = "\"";
  public static final String QUOTE_QUOTE = "\"\"";
  public static final String SINGLE_QUOTE = "'";
  public static final String SEMI_COLON = ";";
  public static final String COLON = ":";
  public static final String UNDERSCORE = "_";
  public static final String DASH = "-";
  public static final String LEFT_BRACE = "{";
  public static final String RIGHT_BRACE = "}";
  public static final String LEFT_BRACKET = "[";
  public static final String RIGHT_BRACKET = "]";
  public static final String LEFT_PARENTHESIS = "(";
  public static final String RIGHT_PARENTHESIS = ")";
  // constant as only needs to be created once
  public static final Date EPOCH = new Date(0);
}
