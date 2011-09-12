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

/**
 * Constants used for parsing in ODK aggregate 
 *  
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class ParserConsts {

  public static final String DEFAULT_NAMESPACE = "ODK_DEFAULT";

  public static final String FORM_ID_ATTRIBUTE_NAME = "id";

  public static final String MODEL_VERSION_ATTRIBUTE_NAME = "version";

  public static final String UI_VERSION_ATTRIBUTE_NAME = "uiVersion";

  public static final String INSTANCE_ID_ATTRIBUTE_NAME = "instanceID";
  
  public static final String SUBMISSION_DATE_ATTRIBUTE_NAME = "submissionDate";

  public static final String IS_COMPLETE_ATTRIBUTE_NAME = "isComplete";

  public static final String MARKED_AS_COMPLETE_DATE_ATTRIBUTE_NAME = "markedAsCompleteDate";
  
  public static final String NAMESPACE_ATTRIBUTE = "xmlns";

  public static final String FORWARD_SLASH = "/";
  
  public static final String FORWARD_SLASH_SUBSTITUTION = "&frasl;";
  
  public static final String VALUE_FORMATTED = "  Value: ";

  public static final String ATTRIBUTE_FORMATTED = " Attribute> ";

  public static final String NODE_FORMATTED = "Node: ";

  /**
   * The max file size that can be uploaded/parsed
   */
  public final static int FILE_SIZE_MAX = 5000000;

  /**
   * Namespace of ODK extensions to the OpenRosa standards
   */
  public static final String NAMESPACE_ODK = "http://www.opendatakit.org/xforms";

  /**
   * additional attribute used to supply the base64 public key for encrypted submissions
   */
  public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey";

}
