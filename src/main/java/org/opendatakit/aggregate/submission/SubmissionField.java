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
 * @param <T> a GAE datastore type
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface SubmissionField<T> extends SubmissionValue {

  boolean isBinary();

  T getValue();

  void setValueFromString(String value) throws ODKConversionException;

  BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(byte[] byteArray, String contentType, String unrootedFilePath, boolean overwriteOK, CallingContext cc) throws ODKDatastoreException;

}
