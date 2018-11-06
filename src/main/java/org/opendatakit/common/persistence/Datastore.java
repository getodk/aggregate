/*
  Copyright (C) 2010 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.common.persistence;

import java.util.Collection;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;

/**
 * The Datastore interface defines how to store, retrieve, and query data in ODK
 * Aggregate. It is designed to be the sole point of interaction for code above
 * the org.opendatakit.common.persistence layer.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface Datastore {

  String getDefaultSchemaName();

  int getMaxLenTableName();

  int getMaxLenColumnName();

  void assertRelation(CommonFieldsBase relation, User user) throws ODKDatastoreException;

  void dropRelation(CommonFieldsBase relation, User user) throws ODKDatastoreException;

  boolean hasRelation(String schema, String tableName, User user) throws ODKDatastoreException;

  <T extends CommonFieldsBase> T createEntityUsingRelation(T relation, User user);

  <T extends CommonFieldsBase> T getEntity(T relation, String uri, User user) throws ODKDatastoreException;

  Query createQuery(CommonFieldsBase table, String loggingContextTag, User user);

  void putEntity(CommonFieldsBase entity, User user) throws ODKEntityPersistException, ODKOverQuotaException;

  void putEntities(Collection<? extends CommonFieldsBase> entities, User user) throws ODKEntityPersistException, ODKOverQuotaException;

  void deleteEntity(EntityKey key, User user) throws ODKDatastoreException;

  void deleteEntities(Collection<EntityKey> keys, User user) throws ODKDatastoreException;

  TaskLock createTaskLock(User user);
}
