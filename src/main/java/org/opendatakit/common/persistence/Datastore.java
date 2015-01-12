/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence;

import java.util.Collection;
import java.util.List;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
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
 * 
 */
public interface Datastore {

	/**
	 * @return the schema name
	 */
	public String getDefaultSchemaName();
	
	/**
	 * @return the maximum length of a table name in this persistence layer implementation
	 */
	public int getMaxLenTableName();

	/**
	 * @return the maximum length of a column name in this persistence layer implementation
	 */
	public int getMaxLenColumnName();
	
	/**
	 * Asserts that the relation exists in the datastore.  The details of the 
	 * field storage capabilities (e.g., max binary size, max long string size) 
	 * of each data field is filled in before returning.  This means that the 
	 * database administrator can use "ALTER TABLE" commands to redefine column
	 * dimensions and those changes will be reflected in the operations of
	 * aggregate.
	 * 
	 * @param relation
	 * 		   The prototype of the relation is passed into this routine.  The
	 * 		   persistence layer asserts the relation in the datastore and updates
	 *         the prototype with the field storage capabilities of the relation as
	 *         it exists in the datastore.  After this call, the relation is a 
	 *         full-blown relation for use in the other persistence layer APIs. 
	 * @param user non-null user responsible for this request.
	 * @throws ODKDatastoreException
	 *             if there was an error creating the relation
    * @throws ODKOverQuotaException 
    *             if there was a quota limit violation
	 */
	public void assertRelation(CommonFieldsBase relation, User user ) throws ODKDatastoreException;

	/**
	 * Drops the given relation from the Datastore.
	 * The schema and table name are matched.  The structure of the relation 
	 * is not evaluated to determine a match. 
	 * 
	 * @param relation
	 *            the relation to drop
	 * @param user non-null user responsible for this request.
	 * @throws ODKDatastoreException
	 *             if there was an error deleting the relation
    * @throws ODKOverQuotaException 
    *             if there was a quota limit violation
	 */
	public void dropRelation(CommonFieldsBase relation, User user ) throws ODKDatastoreException;

	/**
	 * Quick API to test whether a relation with the given schema and tableName 
	 * already exists in the datastore.  This is used when determining the 
	 * persistence layer naming of the tables that will back a newly uploaded xform.
	 * 
	 * @param schema
	 * @param tableName
	 * @param user non-null user responsible for this request.
	 * @return true if the given relation exists.
	 * @throws ODKDatastoreException
    *                e.g., ODKOverQuotaException
    * @throws ODKOverQuotaException 
    *             if there was a quota limit violation
	 */
	public boolean hasRelation(String schema, String tableName, User user ) throws ODKDatastoreException;

	/**
	 * Returns an empty entity able to be stored in the given relation. The
	 * type of the object returned is the type of the relation passed in.
	 * This is a "not-yet-stored-in-the-persistence-layer" entity.  It has 
	 * its uri (getUri()) defined, but the caller can change that if desired.
	 * 
	 * @param relation
	 *            the relation for which and empty entity should be created.
	 * @param user 
	 * 			  the user responsible for this request.
	 *            
	 * @return an empty Entity set up for storage in the given relation
	 */
	public <T extends CommonFieldsBase> T createEntityUsingRelation(T relation, User user);

	/**
	 * Returns the entity corresponding to the given relation and uri.
	 * The object returned is of the same class as the relation (the 
	 * relation acts as a prototype).
	 * 
	 * @param relation -  the prototype relation to be fetched.
	 * @param uri the primary key corresponding to the desired Entity
	 * @param user non-null user responsible for this request.
	 * 
	 * @return the Entity associated with the given uri
	 * @throws ODKEntityNotFoundException
	 *             if the Entity could not be found in the Datastore
	 * @throws ODKDatastoreException 
	 *             if there is an unspecified error in the Datastore layer
	 * @throws ODKOverQuotaException
	 *             if there is a quota limit violation
	 */
	public <T extends CommonFieldsBase> T getEntity(T relation, String uri, User user)
	    throws ODKOverQuotaException, ODKEntityNotFoundException, ODKDatastoreException;

	/**
	 * Returns a new Query -- possibly for a record with a specific primary key.
	 * 
	 * @param relation -- the prototype relation to be fetched.
	 * @param loggingContextTag -- used when logging query statistics after execute()'s.
	 * @param user -- non-null user responsible for this request.
	 * @return a Query object
	 */
	public Query createQuery(CommonFieldsBase table, String loggingContextTag, User user);
	
	/**
	 * Puts the given entity into the Datastore.
	 * The data store figures out whether this is an insert (new entity) 
	 * or an update (existing entity).
	 * 
	 * @param entity
	 *            the entity to put into the Datastore
	 * @param user non-null user responsible for this request.
	 * @throws ODKEntityPersistException
	 *             if there was an error persisting the Entity
	 * @throws ODKOverQuotaException 
	 *             if there is a quota limit violation
	 */
	public void putEntity(CommonFieldsBase entity, User user) throws ODKEntityPersistException, ODKOverQuotaException;

	/**
	 * Puts multiple entities into the Datastore.
	 * The data store figures out whether each of these is an insert (new entity) 
	 * or an update (existing entity).
	 * 
	 * @param entities
	 *            the entities to put into the Datastore
	 * @param user non-null user responsible for this request.
	 * @throws ODKEntityPersistException
	 *             if there was an error persisting the Entity
	 * @throws ODKOverQuotaException 
	 *             if there was a quota limit violation
	 */
	public void putEntities(Collection<? extends CommonFieldsBase> entities, User user)
			throws ODKEntityPersistException, ODKOverQuotaException;

	/**
	 * Deletes the entity corresponding to the given EntityKey.
	 * 
	 * @param key the key corresponding to the entity to delete
	 * @param user non-null user responsible for this request.
	 * @throws ODKDatastoreException
	 *             if there was an error deleting the Entity
    * @throws ODKOverQuotaException 
    *             if there was a quota limit violation
	 */
	public void deleteEntity(EntityKey key, User user) throws ODKOverQuotaException, ODKDatastoreException;

	/**
	 * Deletes all of the entities which correspond to the given EntityKeys.
	 * 
	 * @param keys collection of the keys corresponding to the entities to delete
	 * @param user non-null user responsible for this request.
	 * @throws ODKDatastoreException
	 *             if there was an error deleting the Entities
    * @throws ODKOverQuotaException 
    *             if there was a quota limit violation
	 */
	public void deleteEntities(Collection<EntityKey> keys, User user)
			throws ODKOverQuotaException, ODKDatastoreException;

	/**
	 * Back-port from Mezuri -- functionality to do a bulk alteration of data.
	 * Everything in this list is either an update or an insert. DatastoreImpl
	 * discovers which it is and implements the appropriate update.
	 * 
	 * @param changes
	 */
	public void batchAlterData(List<? extends CommonFieldsBase> changes, User user)
	      throws ODKEntityPersistException, ODKOverQuotaException;
	
	/**
	 * Create a task lock object.  A database-mediated global mutex.
	 * 
	 * @param user
	 * @return
	 */
	public TaskLock createTaskLock(User user);
}
