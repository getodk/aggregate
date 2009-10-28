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

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.form.Form;

import com.google.appengine.api.datastore.Entity;


/**
 * Base class for type conversion
 *
 * @author wbrunette@gmail.com
 *
 * @param <T>
 *  a GAE datastore type
 */
public abstract class SubmissionSingleValueBase<T> extends SubmissionFieldBase<T> {

  /**
   * Value of submission field
   */
  protected T value;

  /**
   * Constructor
   */
  public SubmissionSingleValueBase(String propertyName, boolean binary) {
    super(propertyName, binary);
  }

  /**
   * Get the value of submission field
   * 
   * @return
   *    value
   */
  @Override
  public T getValue() {
    return value;
  }

  /**
   * Set the value of submission field
   *
   * @param value 
   *    value to set
   */
  protected void setValue(T value) {
    this.value = value;
  }
  
  /**
   * Get submission field value from database entity
   * 
   *  @param dbEntity entity to obtain value
   */
  @Override
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
  @Override
  public void addValueToEntity(Entity dbEntity) {
    dbEntity.setProperty(propertyName, getValue());
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SubmissionSingleValueBase<?>)) {
      return false;
    }
    
    SubmissionSingleValueBase<?> other = (SubmissionSingleValueBase<?>) obj;
    
    return super.equals(obj)
      && (value == null ? (other.value == null) : (value.equals(other.value)));
    
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + (value != null ? value.hashCode() : 0); 
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return super.toString() + BasicConsts.TO_STRING_DELIMITER 
      + (value != null ? value.toString() : BasicConsts.EMPTY_STRING);
  }
}
