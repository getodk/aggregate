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
import java.util.Set;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;

/**
 * The Query interface defines how persistence implementations should create query functionality.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public interface Query {
  
	/**
	 * This enum defines the directions which the Query can be sorted on.
	 *
	 */
  public enum Direction {
    ASCENDING,
    DESCENDING;
  };
  
  /**
   * This enum defines the different filter operations which the Query can have.
   *
   */
  public enum FilterOperation {
   EQUAL,
   NOT_EQUAL,
   GREATER_THAN,
   GREATER_THAN_OR_EQUAL,
   LESS_THAN,
   LESS_THAN_OR_EQUAL;
  };
  
  /**
   * Adds a sort to the query using the given attribute name and sort direction.
   * 
   * @param attributeName the name of the attribute to sort on
   * @param direction a Query.Direction which defines the direction to sort
   */
  public void addSort(DataField attributeName, Direction direction);
  
  /**
   * Adds a filter to the query using the given attribute name, filter operation, and value.
   * 
   * @param attributeName the name of the attribute to filter
   * @param op the Query.FilterOperation to use for filtering
   * @param value the value to filter with
   */
  public void addFilter(DataField attributeName, FilterOperation op, Object value);
  
  /**
   * Adds a filter to the query using the given attribute name, filtered by the values
   * IN the value set.
   * 
   * @param attributeName
   * @param valueSet
   */
  public void addValueSetFilter(DataField attributeName, Collection<?> valueSet );
  
  /**
   * Returns a list of all the entities which are the results of executing the query.
   * 
   * @return a List<Entity> which contains the Entity objects from the results of the Query
   * @throws ODKDatastoreException if there was a  problem executing the Query
   * @throws ODKOverQuotaException if the usage quota was exceeded
   */
  public List<? extends CommonFieldsBase> executeQuery() throws ODKDatastoreException;

  /**
   * Returns a list of entities which are the results of executing the query.
   * 
   * @param startCursor -- the cursor at which to start (null if new query)
   * @param fetchLimit -- number of records to fetch
   * @return
   * @throws ODKDatastoreException
   * @throws ODKOverQuotaException if the usage quota was exceeded
   */
  public QueryResult executeQuery(QueryResumePoint startCursor, int fetchLimit) throws ODKDatastoreException, ODKOverQuotaException;
  
  /**
   * Returns a list of distinct EntityKeys of the topLevelAuri for the set of records
   * returned by the query.  This should always be an empty or singleton set.
   * 
   * @param topLevelTable - the relation that the topLevelAuri corresponds to.
   * @param foreignKeyField - the topLevelAuri DataField in the table being queried.
   * @return
   * @throws ODKDatastoreException
   * @throws ODKOverQuotaException if the usage quota was exceeded
   */
  public Set<EntityKey> executeForeignKeyQuery(CommonFieldsBase topLevelTable, DataField foreignKeyField )
  							throws ODKDatastoreException, ODKOverQuotaException;


  /**
   * Returns the list of all distinct values for a given field with any given filter
   * and sort criteria.
   * 
   * @param dataField
   * @return
   * @throws ODKDatastoreException 
   * @throws ODKOverQuotaException if the usage quota was exceeded
   */
  public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException, ODKOverQuotaException;
}
