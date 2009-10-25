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
 * Constants used for parsing in ODK aggregate 
 *  
 * @author wbrunette@gmail.com
 *
 */
public class ParserConsts {

  public static final String DEFAULT_NAMESPACE = "DEFAULT";

  public static final String ODK_ATTRIBUTE_NAME = "id";

  public static final String NAMESPACE_ATTRIBUTE = "xmlns";

  public static final String VALUE_FORMATTED = "  Value: ";

  public static final String ATTRIBUTE_FORMATTED = " Attribute> ";

  public static final String NODE_FORMATTED = "Node: ";

  /**
   * The max file size that can be uploaded/parsed
   */
  public final static int FILE_SIZE_MAX = 500000;
}
