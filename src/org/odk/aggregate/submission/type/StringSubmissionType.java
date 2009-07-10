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

package org.odk.aggregate.submission.type;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

import org.odk.aggregate.constants.BasicConsts;

/**
 * Data Storage Converter for String Type. The GAE datastore string type can
 * only store a limited character strings, while strings of arbitrary length can 
 * be stored in a text object in the datastore (however, text objects are not
 * searchable). String submission type abstracts these details so that the
 * user thinks they are storing a string of any length with only the first
 * part of the string searchable
 * 
 * @author wbrunette@gmail.com
 *
 */
public class StringSubmissionType extends SubmissionTypeBase<String> {

  /**
   * Constant string used to post affix to property name to create the db
   * entity property name to represent the text property
   */
  private static final String TEXT_PROPERTY_ID = "text";
 
  /**
   * Constant string used to post affix to property name to create the db
   * entity property name to represent the string property
   */
  private static final String STRING_PROPERTY_ID = "string";

  /**
   * Max size of string that can be stored in the
   */
  public static final int GAE_MAX_STRING_LEN = 250;

  /**
   * The full string stored in text if string is too long to be stored
   * in a GAE datastore string
   */
  protected Text full_value;
 
  /**
   * Constructor 
   * 
   * @param propertyName
   *    Name of submission element 
   */
  public StringSubmissionType(String propertyName) {
    super(propertyName, false);
  }

  /**
   * Set the string value
   */
  @Override
  public void setValue(String value) {
    if (value.length() < GAE_MAX_STRING_LEN) {
      this.value = value;
      this.full_value = null;
    } else {
      this.value = value.substring(0, GAE_MAX_STRING_LEN - 1);
      this.full_value = new Text(value);
    }
  }

  /**
   * Set the string value
   * 
   * @param value string form of the value
   */
  @Override
  public void setValueFromString(String value) {
    setValue(value);
  }

  /**
   * Get the string's value
   */
  @Override
  public String getValue() {
    if (full_value == null) {
      return value;
    } else {
      return full_value.getValue();
    }
  }

  /**
   * Get string values from database entity
   */
  @Override
  public void getValueFromEntity(Entity dbEntity) {
    value = (String) dbEntity.getProperty(propertyName + STRING_PROPERTY_ID);
    full_value = (Text) dbEntity.getProperty(propertyName + TEXT_PROPERTY_ID);
  }

  /**
   * Add string values to database entity
   */
  @Override
  public void addValueToEntity(Entity dbEntity) {
    dbEntity.setProperty(propertyName + STRING_PROPERTY_ID, value);
    dbEntity.setProperty(propertyName + TEXT_PROPERTY_ID, full_value);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof StringSubmissionType)) {
      return false;
    }
    // super will compare value
    if (!super.equals(obj)) {
      return false;
    }
    StringSubmissionType other = (StringSubmissionType) obj;
    if (full_value == null && other.full_value == null) {
      return true;
    } else if (full_value != null) {
      return full_value.equals(other.full_value);
    } else {
      return false;
    }
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    String value = getValue();
    return super.hashCode() + (value == null ? 0 : value.hashCode());
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (propertyName != null ? propertyName : BasicConsts.EMPTY_STRING) 
      + BasicConsts.TO_STRING_DELIMITER + (getValue() != null ? getValue() : BasicConsts.EMPTY_STRING);
  }

}
