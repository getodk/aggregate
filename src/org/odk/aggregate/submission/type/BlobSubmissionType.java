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

import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.exception.ODKConversionException;
import org.odk.aggregate.submission.SubmissionBlob;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.gson.JsonObject;

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
   * @param propertyName Name of submission element
   */
  public BlobSubmissionType(String propertyName) {
    super(propertyName, true);
  }

  /**
   * Convert value from byte array to data store blob type. Store blob in blob
   * storage and save the key of the blob storage into submission set.
   * 
   * @param byteArray byte form of the value
   * @param submissionSetKey key of submission set that will reference the blob
   * @param contentType type of binary data (NOTE: only used for binary data)
   * @throws ODKConversionException
   * 
   */
  @Override
  public void setValueFromByteArray(byte[] byteArray, Key submissionSetKey, String submissionType) {
    SubmissionBlob blob = new SubmissionBlob(new Blob(byteArray), submissionSetKey, submissionType);
    setValue(blob.getKey());
  }

  /**
   * Cannot convert blob from a string
   * 
   * @param value
   * @throws ODKConversionException
   */
  @Override
  public void setValueFromString(String value) throws ODKConversionException {
    throw new ODKConversionException(ErrorConsts.NO_STRING_TO_BLOB_CONVERT);
  }

  /**
   * Add submission field value to JsonObject
   * @param JSON Object to add value to
   */
  @Override
  public void addValueToJsonObject(JsonObject jsonObject, List<String> propertyNames) {
    if(getValue() == null) {
      return;
    }
    if(!propertyNames.contains(propertyName)){
      return;
    }
    try {
      SubmissionBlob blobStore = new SubmissionBlob(getValue());
      Blob imageBlob = blobStore.getBlob();
      if (imageBlob != null) {
        jsonObject.addProperty(propertyName, Base64.encode(imageBlob.getBytes()));
      }

    } catch (EntityNotFoundException e) {
      // TODO: consider better error handling, right now just skip adding to object
    }
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
