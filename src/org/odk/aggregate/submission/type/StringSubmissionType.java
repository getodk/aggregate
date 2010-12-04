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


import java.util.List;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.form.Form;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.gson.JsonObject;


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
public class StringSubmissionType extends SubmissionFieldBase<String> {

  /**
   * The string with a GAE_MAX_STRING_LEN character limit
   */
  private String shorten_string;
  
  /**
   * The full string stored in text if string is too long to be stored
   * in a GAE datastore string
   */
  private Text full_string;
 
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
  private void setValue(String value) {
    if (value.length() < PersistConsts.GAE_MAX_STRING_LEN) {
      this.shorten_string = value;
      this.full_string = null;
    } else {
      this.shorten_string = value.substring(0, PersistConsts.GAE_MAX_STRING_LEN - 1);
      this.full_string = new Text(value);
    }
  }

  /**
   * Add submission field value to JsonObject
   * @param JSON Object to add value to
   */  
  @Override
  public void addValueToJsonObject(JsonObject jsonObject, List<String> propertyNames) {
    if(!propertyNames.contains(propertyName)){
      return;
    }
    jsonObject.addProperty(propertyName, getValue());
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
    if (full_string == null) {
      return shorten_string;
    } else {
      return full_string.getValue();
    }
  }

  /**
   * Get string values from database entity
   */
  @Override
  public void getValueFromEntity(Entity dbEntity, Form form) {
    shorten_string = (String) dbEntity.getProperty(propertyName + PersistConsts.STRING_PROPERTY_ID);
    full_string = (Text) dbEntity.getProperty(propertyName + PersistConsts.TEXT_PROPERTY_ID);
  }

  /**
   * Add string values to database entity
   */
  @Override
  public void addValueToEntity(Entity dbEntity) {
    dbEntity.setProperty(propertyName + PersistConsts.STRING_PROPERTY_ID, shorten_string);
    dbEntity.setProperty(propertyName + PersistConsts.TEXT_PROPERTY_ID, full_string);
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
    return (full_string == null ? (other.full_string == null) : (full_string.equals(other.full_string)))
      && (shorten_string == null ? (other.shorten_string == null) : (shorten_string.equals(other.shorten_string)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + (full_string == null ? 0 : full_string.hashCode()) + (shorten_string == null ? 0 : shorten_string.hashCode());
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return super.toString()
      + BasicConsts.TO_STRING_DELIMITER + (getValue() != null ? getValue() : BasicConsts.EMPTY_STRING);
  }

}
