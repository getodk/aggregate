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


import java.util.List;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Interface for submission value that can be used to store a submission value
 * in the datastore
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface SubmissionValue extends SubmissionElement {

  void getValueFromEntity(CallingContext cc) throws ODKDatastoreException;

  void recursivelyAddEntityKeysForDeletion(List<EntityKey> keyList, CallingContext cc) throws ODKDatastoreException;

  void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException;

  void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue, CallingContext cc) throws ODKDatastoreException;
}
