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
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.exception.ODKConversionException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.submission.SubmissionField;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.gson.JsonObject;

public abstract class SubmissionFieldBase<T> implements SubmissionField<T>{

  /**
   * Submission property/element name
   */
  protected String propertyName;
  /**
   * Can only be created with binary data
   */
  protected boolean binaryCreation;

  public SubmissionFieldBase(String propertyName, boolean binary) {
    this.propertyName = propertyName;
    this.binaryCreation = binary;
  }

  /**
   * Get Property Name
   *
   * @return
   *    property name
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Returns whether submission type is constructed from a binary object
   * 
   * @return
   *    true if should be constructed with byte array, false otherwise
   */
  public boolean isBinary() {
    return binaryCreation;
  }

  
  /**
   * Get the value of submission field
   * 
   * @return
   *    value
   */
  public abstract T getValue();
  
  /**
   * Parse the value from string format and convert to proper type for
   * submission field
   * 
   * @param value string form of the value
   * @throws ODKConversionException
   */
  public abstract void setValueFromString(String value) throws ODKConversionException;
  
  
  /**
   * Get submission field value from database entity
   *
   * @param dbEntity entity to obtain value
   * @param form the form definition object
   * @throws ODKIncompleteSubmissionData
   */
  public abstract void getValueFromEntity(Entity dbEntity, Form form) throws ODKIncompleteSubmissionData;
  
  /**
   * Add submission field value to database entity
   * 
   * @param dbEntity entity to add value to
   */
  public abstract void addValueToEntity(Entity dbEntity);
  
  /**
   * Add submission field value to JsonObject
   * @param JSON Object to add value to
   */  
  public abstract void addValueToJsonObject(JsonObject jsonObject, List<String> propertyNames);
  
  
  /**
   * Convert byte array to proper type for submission field
   * 
   * @param byteArray byte form of the value
   * @param submissionSetKey key of submission set that will reference the blob
   * @param contentType type of binary data (NOTE: only used for binary data)
   * @throws ODKConversionException
   * 
   */ 
  public void setValueFromByteArray(byte [] byteArray, Key submissionSetKey, String contentType) throws ODKConversionException {
    if(!binaryCreation) {
      setValueFromString(new String(byteArray));
    } else {
      throw new ODKConversionException(ErrorConsts.BINARY_ERROR);
    }
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SubmissionFieldBase<?>)) {
      return false;
    }
    
    SubmissionFieldBase<?> other = (SubmissionFieldBase<?>) obj;
    
    return (propertyName == null ? (other.propertyName == null) : (propertyName.equals(other.propertyName)))
      && (binaryCreation == other.binaryCreation);    
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if(propertyName != null) hashCode += propertyName.hashCode();
    if(binaryCreation) hashCode++;    
    return hashCode; 
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (propertyName != null ? propertyName : BasicConsts.EMPTY_STRING); 

  }
  
}
