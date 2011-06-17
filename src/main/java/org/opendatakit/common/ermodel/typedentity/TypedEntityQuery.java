package org.opendatakit.common.ermodel.typedentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * <p>
 * Query capabilities for {@link TypedEntityRelation}. Allows the results of
 * queries to be TypedEntity objects instead of generic Entity objects. You
 * usually obtain a TypedEntityQuery through a {@link TypedEntityRelation}'s
 * {@link TypedEntityRelation#query() query()} method.
 * </p>
 * 
 * <p>
 * TypedEntityQuery is mutable and not threadsafe.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class TypedEntityQuery<T extends TypedEntity>
{

    /**
     * The relation to query.
     */
    private TypedEntityRelation<T> relation;
    /**
     * The underlying datastore query.
     */
    private Query query;

    /**
     * Create a new TypedEntityQuery. Typically you should create a
     * TypedEntityQuery through a {@link TypedEntityRelation}'s
     * {@link TypedEntityRelation#query() query()} method.
     * 
     * @param relation
     *            an instance of the relation to query.
     */
    public TypedEntityQuery(TypedEntityRelation<T> relation)
    {
        this.relation = relation;
        this.query = relation.createQuery();
    }

    /**
     * Add an equal filter to the query.
     * 
     * @param fieldName
     *            the name of a field in the relation.
     * @param value
     *            the value the given field should be equal to. This must be of
     *            the correct type for the corresponding field.
     * @return this TypedEntityQuery, with the equal filter added.
     */
    public TypedEntityQuery<T> equal(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.EQUAL, value);
    }

    /**
     * Add a greater than filter to the query.
     * 
     * @param fieldName
     *            the name of a field in the relation.
     * @param value
     *            the value the given field should greater than. This must be of
     *            the correct type for the corresponding field.
     * @return this TypedEntityQuery, with the greater than filter added.
     */
    public TypedEntityQuery<T> greaterThan(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.GREATER_THAN, value);
    }

    /**
     * Add a greater than or equal filter to the query.
     * 
     * @param fieldName
     *            the name of a field in the relation.
     * @param value
     *            the value the given field should greater than or equal to.
     *            This must be of the correct type for the corresponding field.
     * @return this TypedEntityQuery, with the greater than or equal filter
     *         added.
     */
    public TypedEntityQuery<T> greaterThanOrEqual(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.GREATER_THAN_OR_EQUAL,
                value);
    }

    /**
     * Add a less than filter to the query.
     * 
     * @param fieldName
     *            the name of a field in the relation.
     * @param value
     *            the value the given field should less than. This must be of
     *            the correct type for the corresponding field.
     * @return this TypedEntityQuery, with the less than filter added.
     */
    public TypedEntityQuery<T> lessThan(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.LESS_THAN, value);
    }

    /**
     * Add a less than or equal filter to the query.
     * 
     * @param fieldName
     *            the name of a field in the relation.
     * @param value
     *            the value the given field should less than or equal to. This
     *            must be of the correct type for the corresponding field.
     * @return this TypedEntityQuery, with the less than or equal filter added.
     */
    public TypedEntityQuery<T> lessThanOrEqual(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.LESS_THAN_OR_EQUAL, value);
    }

    /**
     * Adds a filter to the query.
     * 
     * @param fieldName
     *            the name of a field in the relation.
     * @param op
     *            the operation to filter with.
     * @param value
     *            the value to filter with. This must be of the correct type for
     *            the corresponding field.
     * @return this TypedEntityQuery, with the given filter added.
     */
    private TypedEntityQuery<T> addFilter(String fieldName, FilterOperation op,
            Object value)
    {
        DataField field = relation.getDataField(fieldName);
        query.addFilter(field, op, value);
        return this;
    }

    /**
     * Adds an ascending sort to the query.
     * 
     * @param fieldName
     *            the name of the field to sort by. This field must be a field
     *            in the relation of this query.
     * @return this TypedEntityQuery, with the ascending sort added.
     */
    public TypedEntityQuery<T> sortAscending(String fieldName)
    {
        return addSort(fieldName, Direction.ASCENDING);
    }

    /**
     * Adds an descending sort to the query.
     * 
     * @param fieldName
     *            the name of the field to sort by. This field must be a field
     *            in the relation of this query.
     * @return this TypedEntityQuery, with the descending sort added.
     */
    public TypedEntityQuery<T> sortDescending(String fieldName)
    {
        return addSort(fieldName, Direction.DESCENDING);
    }

    /**
     * Adds a sort to the query.
     * 
     * @param fieldName
     *            the name of the field to sort by. This field must be a field
     *            in the relation of this query.
     * @param direction
     *            the direction to sort by.
     * @return this TypedEntityQuery, with the given sort added.
     */
    private TypedEntityQuery<T> addSort(String fieldName, Direction direction)
    {
        DataField field = relation.getDataField(fieldName);
        query.addSort(field, direction);
        return this;
    }

    /**
     * Narrows the scope of the query to only include entities whose value for
     * the given fieldName is in values.
     * 
     * @param fieldName
     *            the name of the field to filter with. This must be a field in
     *            the relation of this query.
     * @param values
     *            the values to filter by. This collection must be of the
     *            correct type for the field identified by fieldName.
     * @return this TypedEntityQuery, with the include filter added. All
     *         entities with values not in values will be excluded from the
     *         query.
     */
    public TypedEntityQuery<T> include(String fieldName, Collection<?> values)
    {
        DataField field = relation.getDataField(fieldName);
        query.addValueSetFilter(field, values);
        return this;
    }

    /**
     * Get the single entity result of the query.
     * 
     * @return the entity which is the sole result of executing this query.
     * @throws ODKDatastoreException
     *             if the query contains no entities or greater than one entity,
     *             or if there is a problem communicating with the datastore.
     */
    public T get() throws ODKDatastoreException
    {
        List<T> results = execute(1);
        if (results.isEmpty())
        {
            throw new ODKDatastoreException(
                    "called get() and query results contained no results");
        }
        if (results.size() > 1)
        {
            throw new ODKDatastoreException(
                    "called get() and query results contained more than one result");
        }
        return results.get(0);
    }

    /**
     * Execute the query and return a list of all results. Equivalent to calling
     * {@link #execute(int) execute(0)}.
     * 
     * @return a list of all the entities which matched the query.
     * @throws ODKDatastoreException
     */
    public List<T> execute() throws ODKDatastoreException
    {
        return execute(0);
    }

    /**
     * Execute the query and return a list of results.
     * 
     * @param limit
     *            the maximum number of entities to retrieve, or 0 for no limit.
     * @return a list of the entities which matched the query. The size of this
     *         list will be no greater than limit.
     * @throws ODKDatastoreException
     */
    public List<T> execute(int limit) throws ODKDatastoreException
    {
        List<Entity> genericEntities = relation.executeQuery(this.query, limit);
        List<T> relationEntities = new ArrayList<T>();
        for (Entity genericEntity : genericEntities)
        {
            T relationEntity = relation.initialize(genericEntity);
            relationEntities.add(relationEntity);
        }
        return relationEntities;
    }

    /**
     * Retrieves all distinct values for the given field, with any sort and
     * filter criteria.
     * 
     * @param fieldName
     *            the name of the field to retrieve distinct values for.
     * @return a list of distinct values for the given field, narrowed by any
     *         existing filter and sort criteria.
     */
    public List<?> getDistinct(String fieldName)
    {
        DataField dataField = relation.getDataField(fieldName);
        try
        {
            return query.executeDistinctValueForDataField(dataField);
        } catch (ODKDatastoreException e)
        {
            return Collections.emptyList();
        }
    }
}
