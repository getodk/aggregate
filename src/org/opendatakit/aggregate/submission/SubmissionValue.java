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

import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.Row;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Interface for submission value that can be used to store a submission value
 * in the datastore
 * 
 * @author wbrunette@gmail.com
 */
public interface SubmissionValue {

  /**
   * Get Property Name
   * 
   * @return property name
   */
  public String getPropertyName();

  /**
   * Get submission field value from database entity
   * 
   * @param dbEntity
   *          entity to obtain value
   * @param form
   *          the form definition object
   * @param datastore
   *          TODO
 * @throws ODKDatastoreException 
   */
  public void getValueFromEntity(CommonFieldsBase dbEntity, String uriAssociatedRow,
			EntityKey persistenceColocationKey, 
			Datastore datastore, User user, boolean fetchElement)
  		throws ODKDatastoreException;

  /**
   * Gather the entity keys for this and all subordinate elements.
   * Used when assembling the deletion list when deleting a submission.
   * 
   * @param keyList
 * @throws ODKDatastoreException 
   */
  public void recursivelyAddEntityKeys(List<EntityKey> keyList) throws ODKDatastoreException;
  
  /**
   * Recursively persist this submission to the datastore.
   * 
   * @param datastore
   * @param uriUser
 * @throws ODKEntityPersistException 
   */
  public void persist(Datastore datastore, User user ) throws ODKEntityPersistException;
  
  /**
   * Format value for output
   * 
   * @param elemFormatter
   *          the element formatter that will convert the value to the proper
   *          format for output
   * @param row TODO
   * @throws ODKDatastoreException TODO
   */
  public void formatValue(ElementFormatter elemFormatter, Row row) throws ODKDatastoreException;
}
