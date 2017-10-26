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

package org.opendatakit.aggregate.submission;

import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Interface for submission field that can be used to store
 * a submission field in the datastore 
 *
 * @author wbrunette@gmail.com
 * 
 * @param <T>
 *  a GAE datastore type
 * @author mitchellsundt@gmail.com
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
 * @throws ODKDatastoreException 
   */
  public void setValueFromString(String value) throws ODKConversionException, ODKDatastoreException;

  /**
   * Convert value from byte array to data store blob type. Store blob in blob
   * storage and save the key of the blob storage into submission set. There can
   * only be one un-named file. If a value for the unrootedFilePath already exists,
   * and if it is different than the supplied byte array, the existing value will
   * not be changed unless overwiteOK is true.
   * 
   * @param byteArray
   *          byte form of the value
   * @param contentType
   *          type of binary data (NOTE: only used for binary data)
   * @param unrootedFilePath
   *          the filename for this byte array
   * @param overwriteOK
   *          true if overwriting an existing value is OK.
   * @param cc
   *          calling context
   * @return the outcome of the storage attempt. md5 hashes are used to
   *         determine file equivalence.
   * @throws ODKDatastoreException
   * 
   */
  public BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(byte[] byteArray,
      String contentType, String unrootedFilePath, boolean overwriteOK, CallingContext cc)
      throws ODKDatastoreException;
  
}
