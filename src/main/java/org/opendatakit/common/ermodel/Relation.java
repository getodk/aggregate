/**
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.common.ermodel;

import java.util.List;

import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;

/**
 * API for the manipulation of a Relation (e.g., database table) in the
 * datastore.  See {@link AbstractRelation} for the constructor choices.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public interface Relation {
	
	/** regex for legal UPPER_CASE_COL_NAME column and table names */
	static final String VALID_UPPER_CASE_NAME_REGEX = "[\\p{Upper}_][\\p{Upper}\\p{Digit}_]*";
	/** maximum length of a table or column name */
	static final int MAX_PERSISTENCE_NAME_LENGTH = 64;

	/**
	 * Create a new entity (row).  This entity does not exist in the database
	 * until you put() it there.
	 * 
	 * @param cc
	 * @return
	 */
	public Entity newEntity(CallingContext cc);
	
	/**
	 * Create a new entity (row).  This entity does not exist in the database
	 * until you put() it there.
	 * 
	 * @param uri  the primary key for this new entity.  The key must be 
	 *        a string less than 80 characters long.  It should be in a
	 *        URI-style format -- meaning that it has a namespace identifier
	 *        followed by a colon, followed by a string in that namespace.
	 *        The default is a uri in the UUID namespace.  You can construct
	 *        one of these UUID uris using CommonFieldsBase.newUri().
	 *        
	 *        Those are of the form:
	 *          "uuid:371adf05-3cea-4e11-b56c-3b3a1ec25761"
	 * @param cc
	 * @return
	 */
	public Entity newEntity(String uri, CallingContext cc);
	
	/**
	 * Fetch the entity with the given primary key (uri).
	 * 
	 * @param uri
	 * @param cc
	 * @return
	 * @throws ODKEntityNotFoundException
	 */
	public Entity getEntity(String uri, CallingContext cc) throws ODKEntityNotFoundException;

	/**
	 * Search for the entities having dataField values in the given relation to the specified value.
	 * 
	 * @param dataField
	 * @param op  e.g., EQUALS, LESS_THAN, etc.
	 * @param value
	 * @param cc
	 * @return
	 * @throws ODKDatastoreException
	 */
	public List<Entity> getEntities( DataField dataField, Query.FilterOperation op, Object value, CallingContext cc) throws ODKDatastoreException;

	/**
	 * Insert or update the datastore with the values from this entity.
	 * Equivalent to e.persist(cc).
	 * 
	 * @param e
	 * @param cc
	 * @throws ODKEntityPersistException
	 */
	public void putEntity(Entity e, CallingContext cc) throws ODKEntityPersistException;
	
	/**
	 * Delete the given entity from the datastore.
	 * Equivalent to e.remove(cc).
	 * 
	 * @param e
	 * @param cc
	 * @throws ODKDatastoreException if the deletion fails.
	 */
	public void deleteEntity(Entity e, CallingContext cc) throws ODKDatastoreException;
	
	/**
	 * This is just a convenience method.  It may fail midway through 
	 * saving the list of entities.
	 * 
	 * @param eList
	 * @param cc
	 * @throws ODKEntityPersistException
	 */
	public void putEntities(List<Entity> eList, CallingContext cc) throws ODKEntityPersistException;
	
	/**
	 * This is just a convenience function.  It can fail after
	 * having deleted only some of the entities.
	 * 
	 * @param eList
	 * @param cc
	 * @throws ODKDatastoreException
	 */
	public void deleteEntities(List<Entity> eList, CallingContext cc) throws ODKDatastoreException;
	
	/**
	 * This deletes all records in your table and drops it from the 
	 * datastore.  The deletion step is non-optimal for MySQL/Postgresql,
	 * but is required for Google BigTables, as that has no concept of 
	 * dropping a relation.  
	 * 
	 * @param cc
	 * @throws ODKDatastoreException
	 */
	public void dropRelation(CallingContext cc) throws ODKDatastoreException;
	
	/**
	 * Retrieve the DataField with the given field name.
	 * <p>Most implementations would declare fields as public static final DataField
	 * values in the class derived from {@link AbstractRelation).  Code could then directly
	 * reference these static final fields and would not incur the costs of a
	 * string match to find them.  
	 * <p>This method is primarily provided for use cases where the tables are being
	 * dynamically constructed and where the columns cannot be exposed as static values.  
	 * 
	 * @param fieldName
	 * @return
	 */
	public DataField getDataField(String fieldName);
}
