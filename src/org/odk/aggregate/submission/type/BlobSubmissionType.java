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

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.exception.ODKConversionException;
import org.odk.aggregate.submission.SubmissionBlob;

/**
 * Data Storage Converter for Blob Type
 *
 * @author wbrunette@gmail.com
 *
 */
public class BlobSubmissionType extends SubmissionSingleValueBase<Key> {
  /**
   * Constructor 
   * 
   * @param propertyName
   *    Name of submission element 
   */
  public BlobSubmissionType(String propertyName) {
    super(propertyName, true);
  }

  /**
   * Convert value from byte array to data store blob type. Store blob
   * in blob storage and save the key of the blob storage into submission set.
   * 
   * @param byteArray
   *    array of bytes
   *   
   * @param submissionSetKey
   *    submission set key that will reference the blob 
   *   
   */
  @Override
  public void setValueFromByteArray(byte [] byteArray, Key submissionSetKey) {
    SubmissionBlob blob = new SubmissionBlob(new Blob(byteArray), submissionSetKey);
    setValue(blob.getKey());
  }
  
  /**
   * Cannot convert blob from a string
   * 
   * @param value 
   * @throws ODKConversionException
   */  
  @Override
  public void setValueFromString(String value) throws ODKConversionException{
    throw new ODKConversionException(ErrorConsts.NO_STRING_TO_BLOB_CONVERT);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof BlobSubmissionType)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return true;
  }
}
