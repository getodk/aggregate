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

package org.odk.aggregate.submission;

import org.odk.aggregate.exception.ODKConversionException;

import com.google.appengine.api.datastore.Key;

/**
 * Interface for submission field that can be used to store
 * a submission field in the datastore 
 *
 * @author wbrunette@gmail.com
 * 
 * @param <T>
 *  a GAE datastore type
 *
 */
public interface SubmissionField<T> extends SubmissionValue{
  
  /**
   * Returns whether submission type is constructed from a binary object
   * 
   * @return
   *    true if should be constructed with byte array, false otherwise
   */
  public boolean isBinary();
  
  /**
   * Get the value of submission field
   * 
   * @return
   *    value
   */
  public T getValue();

  /**
   * Parse the value from string format and convert to proper type for
   * submission field
   * 
   * @param value string form of the value
   * @throws ODKConversionException
   */
  public void setValueFromString(String value) throws ODKConversionException;

  /**
   * Convert byte array to proper type for submission field
   * 
   * @param byteArray byte form of the value
   * @param submissionSetKey key of submission set that will reference the blob
   * @param contentType type of binary data (NOTE: only used for binary data)
   * @throws ODKConversionException
   * 
   */  
  public void setValueFromByteArray(byte [] byteArray, Key submissionSetKey, String contentType) throws ODKConversionException;
  
}
