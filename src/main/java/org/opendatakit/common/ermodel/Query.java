/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.common.ermodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.opendatakit.common.ermodel.Relation.RelationImpl;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * Query is a query over a Relation.
 *
 * @author the.dylan.price@gmail.com
 */
public class Query {

  public static class WebsafeQueryResult {
    public final List<Entity> entities;
    public final String websafeRefetchCursor;
    public final String websafeBackwardCursor;
    public final String websafeResumeCursor;
    public final boolean hasMore;
    public final boolean hasPrior;

    public WebsafeQueryResult(List<Entity> entities,
        String websafeRefetchCursor, String websafeBackwardCursor, String websafeResumeCursor,
        boolean hasMore, boolean hasPrior) {
      this.entities = entities;
      this.websafeRefetchCursor = websafeRefetchCursor;
      this.websafeBackwardCursor = websafeBackwardCursor;
      this.websafeResumeCursor = websafeResumeCursor;
      this.hasMore = hasMore;
      this.hasPrior = hasPrior;
    }
  }

  /**
   * The Relation to query.
   */
  private Relation relation;

  /**
   * The underlying datastore query.
   */
  private org.opendatakit.common.persistence.Query query;

  protected Query(Relation relation, org.opendatakit.common.persistence.Query query) {
    Validate.notNull(relation);

    this.relation = relation;
    this.query = query;
  }

  /**
   * Add an equal filter to the query.
   *
   * @param field
   *          the field in the Relation.
   * @param value
   *          the value the given field should be equal to. This must be of the
   *          correct type for the corresponding field.
   * @return this Query, with the equal filter added.
   */
  public Query equal(DataField field, Object value) {
    return addFilter(field, FilterOperation.EQUAL, value);
  }

  /**
   * Add an equal filter to the query.
   *
   * @param fieldName
   *          the name of an field in the Relation.
   * @param value
   *          the value the given field should be equal to. This must be of the
   *          correct type for the corresponding field.
   * @return this Query, with the equal filter added.
   */
  public Query equal(String fieldName, Object value) {
    return addFilter(fieldName, FilterOperation.EQUAL, value);
  }

  /**
   * Add a not equal filter to the query.
   *
   * @param field
   *          the field in the Relation.
   * @param value
   *          the value the given field should be not equal to. This must be of
   *          the correct type for the corresponding field.
   * @return this Query, with the not equal filter added.
   */
  public Query notEqual(DataField field, Object value) {
    return addFilter(field, FilterOperation.NOT_EQUAL, value);
  }

  /**
   * Add a not equal filter to the query.
   *
   * @param fieldName
   *          the name of an field in the Relation.
   * @param value
   *          the value the given field should be not equal to. This must be of
   *          the correct type for the corresponding field.
   * @return this Query, with the not equal filter added.
   */
  public Query notEqual(String fieldName, Object value) {
    return addFilter(fieldName, FilterOperation.NOT_EQUAL, value);
  }

  /**
   * Add a greater than filter to the query.
   *
   * @param field
   *          the field in the Relation.
   * @param value
   *          the value the given field should greater than. This must be of the
   *          correct type for the corresponding field.
   * @return this Query, with the greater than filter added.
   */
  public Query greaterThan(DataField field, Object value) {
    return addFilter(field, FilterOperation.GREATER_THAN, value);
  }

  /**
   * Add a greater than filter to the query.
   *
   * @param fieldName
   *          the name of an field in the Relation.
   * @param value
   *          the value the given field should greater than. This must be of the
   *          correct type for the corresponding field.
   * @return this Query, with the greater than filter added.
   */
  public Query greaterThan(String fieldName, Object value) {
    return addFilter(fieldName, FilterOperation.GREATER_THAN, value);
  }

  /**
   * Add a greater than or equal filter to the query.
   *
   * @param field
   *          the field in the Relation.
   * @param value
   *          the value the given field should greater than or equal to. This
   *          must be of the correct type for the corresponding field.
   * @return this Query, with the greater than or equal filter added.
   */
  public Query greaterThanOrEqual(DataField field, Object value) {
    return addFilter(field, FilterOperation.GREATER_THAN_OR_EQUAL, value);
  }

  /**
   * Add a greater than or equal filter to the query.
   *
   * @param fieldName
   *          the name of an field in the Relation.
   * @param value
   *          the value the given field should greater than or equal to. This
   *          must be of the correct type for the corresponding field.
   * @return this Query, with the greater than or equal filter added.
   */
  public Query greaterThanOrEqual(String fieldName, Object value) {
    return addFilter(fieldName, FilterOperation.GREATER_THAN_OR_EQUAL, value);
  }

  /**
   * Add a less than filter to the query.
   *
   * @param field
   *          the field in the Relation.
   * @param value
   *          the value the given field should less than. This must be of the
   *          correct type for the corresponding field.
   * @return this Query, with the less than filter added.
   */
  public Query lessThan(DataField field, Object value) {
    return addFilter(field, FilterOperation.LESS_THAN, value);
  }

  /**
   * Add a less than filter to the query.
   *
   * @param fieldName
   *          the name of an field in the Relation.
   * @param value
   *          the value the given field should less than. This must be of the
   *          correct type for the corresponding field.
   * @return this Query, with the less than filter added.
   */
  public Query lessThan(String fieldName, Object value) {
    return addFilter(fieldName, FilterOperation.LESS_THAN, value);
  }

  /**
   * Add a less than or equal filter to the query.
   *
   * @param field
   *          the field in the Relation.
   * @param value
   *          the value the given field should less than or equal to. This must
   *          be of the correct type for the corresponding field.
   * @return this Query, with the less than or equal filter added.
   */
  public Query lessThanOrEqual(DataField field, Object value) {
    return addFilter(field, FilterOperation.LESS_THAN_OR_EQUAL, value);
  }

  /**
   * Add a less than or equal filter to the query.
   *
   * @param fieldName
   *          the name of an field in the Relation.
   * @param value
   *          the value the given field should less than or equal to. This must
   *          be of the correct type for the corresponding field.
   * @return this Query, with the less than or equal filter added.
   */
  public Query lessThanOrEqual(String fieldName, Object value) {
    return addFilter(fieldName, FilterOperation.LESS_THAN_OR_EQUAL, value);
  }

  /**
   * Adds a filter to the query. Alternative API to {@link #equal},
   * {@link #greaterThan}, etc.
   *
   * @param fieldName
   *          the name of an field in the Relation.
   * @param op
   *          the operation to filter with.
   * @param value
   *          the value to filter with. This must be of the correct type for the
   *          corresponding field.
   * @return this Query, with the given filter added.
   */
  public Query addFilter(String fieldName, FilterOperation op, Object value) {
    Validate.notEmpty(fieldName);
    Validate.notNull(op);
    DataField field = relation.getDataField(fieldName);
    query.addFilter(field, op, value);
    return this;
  }

  /**
   * Adds a filter to the query. Alternative API to {@link #equal},
   * {@link #greaterThan}, etc.
   *
   * @param fieldName
   *          the name of an field in the Relation.
   * @param op
   *          the operation to filter with.
   * @param value
   *          the value to filter with. This must be of the correct type for the
   *          corresponding field.
   * @return this Query, with the given filter added.
   */
  public Query addFilter(DataField field, FilterOperation op, Object value) {
    Validate.notNull(field);
    Validate.notNull(op);

    query.addFilter(relation.verify(field), op, value);
    return this;
  }

  /**
   * Adds an ascending sort to the query.
   *
   * @param field
   *          the field to sort by. This field must be an field in
   *          the Relation of this query.
   * @return this Query, with the ascending sort added.
   */
  public Query sortAscending(DataField field) {
    return addSort(field, Direction.ASCENDING);
  }

  /**
   * Adds an ascending sort to the query.
   *
   * @param fieldName
   *          the name of the field to sort by. This field must be an field in
   *          the Relation of this query.
   * @return this Query, with the ascending sort added.
   */
  public Query sortAscending(String fieldName) {
    return addSort(fieldName, Direction.ASCENDING);
  }

  /**
   * Adds a descending sort to the query.
   *
   * @param field
   *          the field to sort by. This field must be an field in
   *          the Relation of this query.
   * @return this Query, with the descending sort added.
   */
  public Query sortDescending(DataField field) {
    return addSort(field, Direction.DESCENDING);
  }

  /**
   * Adds a descending sort to the query.
   *
   * @param fieldName
   *          the name of the field to sort by. This field must be an field in
   *          the Relation of this query.
   * @return this Query, with the descending sort added.
   */
  public Query sortDescending(String fieldName) {
    return addSort(fieldName, Direction.DESCENDING);
  }

  /**
   * Adds a sort to the query. Alternative API to {@link #sortAscending} and
   * {@link #sortDescending}.
   *
   * @param fieldName
   *          the name of the field to sort by. This field must be an field in
   *          the Relation of this query.
   * @param direction
   *          the direction to sort by.
   * @return this Query, with the given sort added.
   */
  public Query addSort(DataField field, Direction direction) {
    Validate.notNull(field);
    Validate.notNull(direction);
    query.addSort(relation.verify(field), direction);
    return this;
  }

  /**
   * Adds a sort to the query. Alternative API to {@link #sortAscending} and
   * {@link #sortDescending}.
   *
   * @param fieldName
   *          the name of the field to sort by. This field must be an field in
   *          the Relation of this query.
   * @param direction
   *          the direction to sort by.
   * @return this Query, with the given sort added.
   */
  public Query addSort(String fieldName, Direction direction) {
    Validate.notEmpty(fieldName);
    Validate.notNull(direction);
    DataField field = relation.getDataField(fieldName);
    query.addSort(field, direction);
    return this;
  }

  /**
   * Narrows the scope of the query to only include entities whose value for the
   * given fieldName is in values.
   *
   * @param field
   *          the field to filter with. This must be an field in the
   *          Relation of this query.
   * @param values
   *          the values to filter by. This collection must be of the correct
   *          type for the field identified by fieldName. Must not be null or
   *          empty.
   * @return this Query, with the include filter added. All entities with values
   *         not in values will be excluded from the query.
   */
  public Query include(DataField field, Collection<?> values) {
    Validate.notNull(field);
    Validate.noNullElements(values);
    query.addValueSetFilter(relation.verify(field), values);
    return this;
  }

  /**
   * Narrows the scope of the query to only include entities whose value for the
   * given fieldName is in values.
   *
   * @param fieldName
   *          the name of the field to filter with. This must be an field in the
   *          Relation of this query.
   * @param values
   *          the values to filter by. This collection must be of the correct
   *          type for the field identified by fieldName. Must not be null or
   *          empty.
   * @return this Query, with the include filter added. All entities with values
   *         not in values will be excluded from the query.
   */
  public Query include(String fieldName, Collection<?> values) {
    Validate.notEmpty(fieldName);
    Validate.noNullElements(values);
    DataField field = relation.getDataField(fieldName);
    query.addValueSetFilter(field, values);
    return this;
  }

  /**
   * Get the single entity result of the query.
   *
   * @return the entity which is the sole result of executing this query, or
   *         null if the query returned no results
   * @throws ODKDatastoreException
   *           if the results contained more than one entity or if there is a
   *           problem communicating with the datastore.
   */
  public Entity get() throws ODKDatastoreException {
    List<Entity> results = execute();
    if (results.isEmpty()) {
      return null;
    }
    if (results.size() > 1) {
      throw new ODKDatastoreException(
          "called get() and query results contained more than one result");
    }
    return results.get(0);
  }

  /**
   * @return true if the results of executing this query are not empty
   * @throws ODKDatastoreException
   */
  public boolean exists() throws ODKDatastoreException {
    List<Entity> results = execute();
    if (results.isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Execute the query and return a list of all results.
   *
   * @return a list of all the entities which matched the query.
   * @throws ODKDatastoreException
   */
  public List<Entity> execute() throws ODKDatastoreException {
    try {
      List<? extends CommonFieldsBase> list = query.executeQuery();
      List<Entity> entities = new ArrayList<Entity>();
      for (CommonFieldsBase b : list) {
        entities.add(relation.new EntityImpl((RelationImpl) b));
      }
      return entities;
    } catch (ODKDatastoreException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Execute the query and return a list of all results.
   *
   * @return a list of all the entities which matched the query.
   * @throws ODKDatastoreException
   */
  public WebsafeQueryResult execute(QueryResumePoint startCursor, int fetchLimit) throws ODKDatastoreException {
    try {
      QueryResult result = query.executeQuery(startCursor, fetchLimit);

      List<? extends CommonFieldsBase> list = result.getResultList();
      List<Entity> entities = new ArrayList<Entity>();
      for (CommonFieldsBase b : list) {
        entities.add(relation.new EntityImpl((RelationImpl) b));
      }
      String safeStartCursor = result.getStartCursor() == null ? null : result.getStartCursor().asWebsafeCursor();
      String safeBackwardCursor = result.getBackwardCursor() == null ? null : result.getBackwardCursor().asWebsafeCursor();
      String safeResumeCursor = result.getResumeCursor() == null ? null : result.getResumeCursor().asWebsafeCursor();

      return new WebsafeQueryResult( entities,
          safeStartCursor,
          safeBackwardCursor,
          safeResumeCursor,
          result.hasMoreResults(), result.hasPriorResults() );
    } catch (ODKDatastoreException e) {
      return new WebsafeQueryResult( new ArrayList<Entity>(),
          null, null, null,
          false, false );
    }
  }

  /**
   * Retrieves all distinct values for the given field, with any sort and filter
   * criteria.
   *
   * @param field
   *          the name of the field to retrieve distinct values for.
   * @return a list of distinct values for the given field, narrowed by any
   *         existing filter and sort criteria.
   */
  public List<?> getDistinct(DataField field) {
    Validate.notNull(field);
    try {
      return query.executeDistinctValueForDataField(relation.verify(field));
    } catch (ODKDatastoreException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Retrieves all distinct values for the given field, with any sort and filter
   * criteria.
   *
   * @param fieldName
   *          the name of the field to retrieve distinct values for.
   * @return a list of distinct values for the given field, narrowed by any
   *         existing filter and sort criteria.
   */
  public List<?> getDistinct(String fieldName) {
    Validate.notEmpty(fieldName);
    DataField dataField = relation.getDataField(fieldName);
    try {
      return query.executeDistinctValueForDataField(dataField);
    } catch (ODKDatastoreException e) {
      return Collections.emptyList();
    }
  }
}
