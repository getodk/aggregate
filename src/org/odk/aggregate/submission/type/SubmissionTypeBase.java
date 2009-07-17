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
import com.google.appengine.api.datastore.Key;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.exception.ODKConversionException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.submission.SubmissionField;


/**
 * Base class for type conversion
 *
 * @author wbrunette@gmail.com
 *
 * @param <T>
 *  a GAE datastore type
 */
public abstract class SubmissionTypeBase<T> implements SubmissionField<T> {

  /**
   * Submission property/element name
   */
  protected String propertyName;

  /**
   * Value of submission field
   */
  protected T value;

  /**
   * Can only be created with binary data
   */
  protected boolean binaryCreation;

  /**
   * Constructor
   */
  public SubmissionTypeBase(String propertyName, boolean binary) {
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
  public T getValue() {
    return value;
  }

  /**
   * Set the value of submission field
   *
   * @param value 
   *    value to set
   */
  public void setValue(T value) {
    this.value = value;
  }

  /**
   * Parse the value from string format and convert to proper type for
   * submission field
   * 
   * @param value string form of the value
   * @throws ODKConversionException
   */
  public abstract void setValueFromString(String value) throws ODKConversionException;

  /**
   * Convert byte array to proper type for submission field
   * 
   * @param byteArray byte form of the value
   * @throws ODKConversionException
   */  
  public void setValueFromByteArray(byte [] byteArray, Key submissionSetKey) throws ODKConversionException {
    if(!binaryCreation) {
      setValueFromString(new String(byteArray));
    } else {
      throw new ODKConversionException(ErrorConsts.BINARY_ERROR);
    }
  }
  
  /**
   * Get submission field value from database entity
   * 
   *  @param dbEntity entity to obtain value
   */
  public void getValueFromEntity(Entity dbEntity, Form form) {
    @SuppressWarnings("unchecked")
    T value = (T) dbEntity.getProperty(propertyName);
    setValue(value);
  }
  
  /**
   * Add submission field value to database entity
   * 
   * @param dbEntity entity to add value to
   */
  public void addValueToEntity(Entity dbEntity) {
    dbEntity.setProperty(propertyName, getValue());
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SubmissionTypeBase)) {
      return false;
    }
    
    @SuppressWarnings("unchecked")
    SubmissionTypeBase<T> other = (SubmissionTypeBase<T>) obj;
    
    return (propertyName == null ? (other.propertyName == null) : (propertyName.equals(other.propertyName)))
      && (value == null ? (other.value == null) : (value.equals(other.value)))
      && (binaryCreation == other.binaryCreation);
    
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
    if(propertyName != null) hashCode += propertyName.hashCode();
    if(value != null) hashCode += value.hashCode(); 
    if(binaryCreation) hashCode++;    
    return hashCode; 
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (propertyName != null ? propertyName : BasicConsts.EMPTY_STRING) 
      + BasicConsts.TO_STRING_DELIMITER + (value != null ? value.toString() : BasicConsts.EMPTY_STRING);
  }
}
