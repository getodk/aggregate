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
 * Constant strings used to identify properties in an entity
 *  
 * @author wbrunette@gmail.com
 *
 */
public class PersistConsts {

  /**
   * Constant string that is the propertyName for the submission submit time
   */
  public static final String SUBMITTED_TIME_PROPERTY_TAG = "SUBMITTED_TIME";
  
  /**
   * Constant string that is the propertyName for the name of the submission set
   */
  public static final String SET_NAME_PROPERTY = "SET_NAME";
  
  /**
   * Constant string that is the propertyName for the odk identifier
   */
  public static final String ODKID_PROPERTY = "ODKID";
  
  /**
   * Constant string that is the propertyName for key to the parent submission set
   */
  public static final String PARENT_KEY_PROPERTY = "PARENT_KEY";

  
  public static final String ORDER_PROPERTY = "ORDER";
  
  public static final String BLOB_PROPERTY = "BLOB";
  
  public static final String CONTENT_TYPE_PROPERTY = "CONTENT_TYPE";

  public static final String BLOB_STORE_KIND = "blob_store";

  /**
   * Constant string used to post affix to property name to create the db
   * entity property name to represent the text property
   */
  public static final String TEXT_PROPERTY_ID = "text";

  /**
   * Constant string used to post affix to property name to create the db
   * entity property name to represent the string property
   */
  public static final String STRING_PROPERTY_ID = "string";

  /**
   * Max size of string that can be stored in the
   */
  public static final int GAE_MAX_STRING_LEN = 250;

  public static final String LONGITUDE_PROPERTY = "LONGITUDE";

  public static final String LATITUDE_PROPERTY = "LATITUDE";

}
