package org.opendatakit.common.ermodel.simple.typedentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * <p>
 * Query capabilities for {@link TypedEntityRelation}. Allows the results of
 * queries to be arbitrary objects instead of generic Entity objects. You
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
public class TypedEntityQuery<T>
{

    /**
     * The relation to query.
     */
    private TypedEntityRelation<T> relation;

    /**
     * The underlying query.
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
    public TypedEntityQuery(TypedEntityRelation<T> relation, Query query)
    {
        this.relation = relation;
        this.query = query;
    }

    /**
     * Add an equal filter to the query.
     * 
     * @param attributeName
     *            the name of an attribute in the relation.
     * @param value
     *            the value the given attribute should be equal to. This must be
     *            of the correct type for the corresponding attribute.
     * @return this TypedEntityQuery, with the equal filter added.
     */
    public TypedEntityQuery<T> equal(String attributeName, Object value)
    {
        query.equal(attributeName, value);
        return this;
    }

    /**
     * Add a greater than filter to the query.
     * 
     * @param attributeName
     *            the name of an attribute in the relation.
     * @param value
     *            the value the given attribute should greater than. This must
     *            be of the correct type for the corresponding attribute.
     * @return this TypedEntityQuery, with the greater than filter added.
     */
    public TypedEntityQuery<T> greaterThan(String attributeName, Object value)
    {
        query.greaterThan(attributeName, value);
        return this;
    }

    /**
     * Add a greater than or equal filter to the query.
     * 
     * @param attributeName
     *            the name of an attribute in the relation.
     * @param value
     *            the value the given attribute should greater than or equal to.
     *            This must be of the correct type for the corresponding
     *            attribute.
     * @return this TypedEntityQuery, with the greater than or equal filter
     *         added.
     */
    public TypedEntityQuery<T> greaterThanOrEqual(String attributeName,
            Object value)
    {
        query.greaterThanOrEqual(attributeName, value);
        return this;
    }

    /**
     * Add a less than filter to the query.
     * 
     * @param attributeName
     *            the name of an attribute in the relation.
     * @param value
     *            the value the given attribute should less than. This must be
     *            of the correct type for the corresponding attribute.
     * @return this TypedEntityQuery, with the less than filter added.
     */
    public TypedEntityQuery<T> lessThan(String attributeName, Object value)
    {
        query.lessThan(attributeName, value);
        return this;
    }

    /**
     * Add a less than or equal filter to the query.
     * 
     * @param attributeName
     *            the name of an attribute in the relation.
     * @param value
     *            the value the given attribute should less than or equal to.
     *            This must be of the correct type for the corresponding
     *            attribute.
     * @return this TypedEntityQuery, with the less than or equal filter added.
     */
    public TypedEntityQuery<T> lessThanOrEqual(String attributeName,
            Object value)
    {
        query.lessThanOrEqual(attributeName, value);
        return this;
    }

    /**
     * Adds an ascending sort to the query.
     * 
     * @param attributeName
     *            the name of the attribute to sort by. This attribute must be
     *            an attribute in the relation of this query.
     * @return this TypedEntityQuery, with the ascending sort added.
     */
    public TypedEntityQuery<T> sortAscending(String attributeName)
    {
        query.sortAscending(attributeName);
        return this;
    }

    /**
     * Adds an descending sort to the query.
     * 
     * @param attributeName
     *            the name of the attribute to sort by. This attribute must be
     *            an attribute in the relation of this query.
     * @return this TypedEntityQuery, with the descending sort added.
     */
    public TypedEntityQuery<T> sortDescending(String attributeName)
    {
        query.sortDescending(attributeName);
        return this;
    }

    /**
     * Narrows the scope of the query to only include entities whose value for
     * the given attributeName is in values.
     * 
     * @param attributeName
     *            the name of the attribute to filter with. This must be an
     *            attribute in the relation of this query.
     * @param values
     *            the values to filter by. This collection must be of the
     *            correct type for the attribute identified by attributeName.
     * @return this TypedEntityQuery, with the include filter added. All
     *         entities with values not in values will be excluded from the
     *         query.
     */
    public TypedEntityQuery<T> include(String attributeName,
            Collection<?> values)
    {
        query.include(attributeName, values);
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
     * @return true if the results of executing this query are not empty
     * @throws ODKDatastoreException
     */
    public boolean exists() throws ODKDatastoreException
    {
        List<T> results = execute();
        if (results.isEmpty())
        {
            return false;
        } else
        {
            return true;
        }
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
        List<Entity> genericEntities = query.execute(limit);
        List<T> relationEntities = new ArrayList<T>();
        for (Entity genericEntity : genericEntities)
        {
            T relationEntity = relation.initialize(genericEntity);
            relationEntities.add(relationEntity);
        }
        return relationEntities;
    }

    /**
     * Retrieves all distinct values for the given attribute, with any sort and
     * filter criteria.
     * 
     * @param attributeName
     *            the name of the attribute to retrieve distinct values for.
     * @return a list of distinct values for the given attribute, narrowed by
     *         any existing filter and sort criteria.
     */
    public List<?> getDistinct(String attributeName)
    {
        return query.getDistinct(attributeName);
    }
}
