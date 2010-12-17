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

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
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
	 */
	public boolean hasRelation(String schema, String tableName, User user );

	/**
	 * Returns an empty entity able to be stored in the given relation. The
	 * type of the object returned is the type of the relation passed in.
	 * This is a "not-yet-stored-in-the-persistence-layer" entity.  It has 
	 * its uri (getUri()) defined.
	 * 
	 * @param relation
	 *            the relation for which and empty entity should be created.
	 * @param topLevelAuriKey
	 * 			  the uri of the top-level entity to which the created 
	 *            entity will have affinity (e.g., for Google joins).
	 *            If the table is dynamic, the Uri of this key is also 
	 *            stored in the topLevelAuri field.  Specify null if 
	 *            there is no appropriate affinity for is relation.  
	 *            This is for bigTable and other distributed systems 
	 *            that have big performance gains if the data that are 
	 *            commonly joined are co-resident in the cloud.
	 * @param user non-null user responsible for this request.
	 *            
	 * @return an empty Entity set up for storage in the given relation
	 */
	public <T extends CommonFieldsBase> T createEntityUsingRelation(T relation, EntityKey topLevelAuriKey, User user);

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
	 */
	public <T extends CommonFieldsBase> T getEntity(T relation, String uri, User user) throws ODKEntityNotFoundException;

	/**
	 * Returns a new Query -- possibly for a record with a specific primary key.
	 * 
	 * @param relation the prototype relation to be fetched.
	 * @param user non-null user responsible for this request.
	 * @return a Query object
	 */
	public Query createQuery(CommonFieldsBase table, User user);
	
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
	 */
	public void putEntity(CommonFieldsBase entity, User user) throws ODKEntityPersistException;

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
	 */
	public void putEntities(Collection<? extends CommonFieldsBase> entities, User user)
			throws ODKEntityPersistException;

	/**
	 * Deletes the entity corresponding to the given EntityKey.
	 * 
	 * @param key the key corresponding to the entity to delete
	 * @param user non-null user responsible for this request.
	 * @throws ODKDatastoreException
	 *             if there was an error deleting the Entity
	 */
	public void deleteEntity(EntityKey key, User user) throws ODKDatastoreException;

	/**
	 * Deletes all of the entities which correspond to the given EntityKeys.
	 * 
	 * @param keys collection of the keys corresponding to the entities to delete
	 * @param user non-null user responsible for this request.
	 * @throws ODKDatastoreException
	 *             if there was an error deleting the Entities
	 */
	public void deleteEntities(Collection<EntityKey> keys, User user)
			throws ODKDatastoreException;
	
	/**
	 * Create a task lock object.  A database-mediated global mutex.
	 * 
	 * @param user
	 * @return
	 */
	public TaskLock createTaskLock(User user);
}
