package org.opendatakit.common.ermodel.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opendatakit.common.ermodel.ExtendedAbstractRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.Check;

/**
 * Query is a query over a Relation.
 * 
 * @author the.dylan.price@gmail.com
 */
public class Query
{
    /**
     * The Relation to query.
     */
    private ExtendedAbstractRelation Relation;

    /**
     * The underlying datastore query.
     */
    private org.opendatakit.common.persistence.Query query;

    protected Query(ExtendedAbstractRelation Relation, String loggingContextTag )
    {
        Check.notNull(Relation, "Relation");
        this.Relation = Relation;
        this.query = Relation.createQuery(loggingContextTag);
    }

    /**
     * Add an equal filter to the query.
     * 
     * @param attributeName
     *            the name of an Attribute in the Relation.
     * @param value
     *            the value the given Attribute should be equal to. This must be
     *            of the correct type for the corresponding Attribute.
     * @return this Query, with the equal filter added.
     */
    public Query equal(String attributeName, Object value)
    {
        return addFilter(attributeName, FilterOperation.EQUAL, value);
    }

    /**
     * Add a not equal filter to the query.
     * 
     * @param attributeName
     *            the name of an Attribute in the Relation.
     * @param value
     *            the value the given Attribute should be not equal to. This must be
     *            of the correct type for the corresponding Attribute.
     * @return this Query, with the not equal filter added.
     */
    public Query notEqual(String attributeName, Object value)
    {
        return addFilter(attributeName, FilterOperation.NOT_EQUAL, value);
    }

    /**
     * Add a greater than filter to the query.
     * 
     * @param attributeName
     *            the name of an Attribute in the Relation.
     * @param value
     *            the value the given Attribute should greater than. This must
     *            be of the correct type for the corresponding Attribute.
     * @return this Query, with the greater than filter added.
     */
    public Query greaterThan(String attributeName, Object value)
    {
        return addFilter(attributeName, FilterOperation.GREATER_THAN, value);
    }

    /**
     * Add a greater than or equal filter to the query.
     * 
     * @param attributeName
     *            the name of an Attribute in the Relation.
     * @param value
     *            the value the given Attribute should greater than or equal to.
     *            This must be of the correct type for the corresponding
     *            Attribute.
     * @return this Query, with the greater than or equal filter added.
     */
    public Query greaterThanOrEqual(String attributeName, Object value)
    {
        return addFilter(attributeName, FilterOperation.GREATER_THAN_OR_EQUAL,
                value);
    }

    /**
     * Add a less than filter to the query.
     * 
     * @param attributeName
     *            the name of an Attribute in the Relation.
     * @param value
     *            the value the given Attribute should less than. This must be
     *            of the correct type for the corresponding Attribute.
     * @return this Query, with the less than filter added.
     */
    public Query lessThan(String attributeName, Object value)
    {
        return addFilter(attributeName, FilterOperation.LESS_THAN, value);
    }

    /**
     * Add a less than or equal filter to the query.
     * 
     * @param attributeName
     *            the name of an Attribute in the Relation.
     * @param value
     *            the value the given Attribute should less than or equal to.
     *            This must be of the correct type for the corresponding
     *            Attribute.
     * @return this Query, with the less than or equal filter added.
     */
    public Query lessThanOrEqual(String attributeName, Object value)
    {
        return addFilter(attributeName, FilterOperation.LESS_THAN_OR_EQUAL,
                value);
    }

    /**
     * Adds a filter to the query.
     * 
     * @param attributeName
     *            the name of an Attribute in the Relation.
     * @param op
     *            the operation to filter with.
     * @param value
     *            the value to filter with. This must be of the correct type for
     *            the corresponding Attribute.
     * @return this Query, with the given filter added.
     */
    private Query addFilter(String attributeName, FilterOperation op,
            Object value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        Check.notNull(op, "op");
        DataField Attribute = Relation.getDataField(attributeName);
        query.addFilter(Attribute, op, value);
        return this;
    }

    /**
     * Adds an ascending sort to the query.
     * 
     * @param attributeName
     *            the name of the Attribute to sort by. This Attribute must be
     *            an Attribute in the Relation of this query.
     * @return this Query, with the ascending sort added.
     */
    public Query sortAscending(String attributeName)
    {
        return addSort(attributeName, Direction.ASCENDING);
    }

    /**
     * Adds a descending sort to the query.
     * 
     * @param attributeName
     *            the name of the Attribute to sort by. This Attribute must be
     *            an Attribute in the Relation of this query.
     * @return this Query, with the descending sort added.
     */
    public Query sortDescending(String attributeName)
    {
        return addSort(attributeName, Direction.DESCENDING);
    }

    /**
     * Adds a sort to the query.
     * 
     * @param attributeName
     *            the name of the Attribute to sort by. This Attribute must be
     *            an Attribute in the Relation of this query.
     * @param direction
     *            the direction to sort by.
     * @return this Query, with the given sort added.
     */
    private Query addSort(String attributeName, Direction direction)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        Check.notNull(direction, "direction");
        DataField Attribute = Relation.getDataField(attributeName);
        query.addSort(Attribute, direction);
        return this;
    }

    /**
     * Narrows the scope of the query to only include entities whose value for
     * the given attributeName is in values.
     * 
     * @param attributeName
     *            the name of the Attribute to filter with. This must be an
     *            Attribute in the Relation of this query.
     * @param values
     *            the values to filter by. This collection must be of the
     *            correct type for the Attribute identified by attributeName.
     *            Must not be null or empty.
     * @return this Query, with the include filter added. All entities with
     *         values not in values will be excluded from the query.
     */
    public Query include(String attributeName, Collection<?> values)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        Check.notNullOrEmpty(values, "values");
        DataField Attribute = Relation.getDataField(attributeName);
        query.addValueSetFilter(Attribute, values);
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
    public Entity get() throws ODKDatastoreException
    {
        List<Entity> results = execute();
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
        List<Entity> results = execute();
        if (results.isEmpty())
        {
            return false;
        } else
        {
            return true;
        }
    }

    /**
     * Execute the query and return a list of all results.
     * 
     * @return a list of all the entities which matched the query.
     * @throws ODKDatastoreException
     */
    public List<Entity> execute() throws ODKDatastoreException
    {
        List<org.opendatakit.common.ermodel.Entity> genericEntities = Relation
                .executeQuery(this.query);
        List<Entity> RelationEntities = new ArrayList<Entity>();
        for (org.opendatakit.common.ermodel.Entity genericEntity : genericEntities)
        {
            Entity RelationEntity = Entity.fromEntity(Relation, genericEntity);
            RelationEntities.add(RelationEntity);
        }
        return RelationEntities;
    }

    /**
     * Retrieves all distinct values for the given Attribute, with any sort and
     * filter criteria.
     * 
     * @param attributeName
     *            the name of the Attribute to retrieve distinct values for.
     * @return a list of distinct values for the given Attribute, narrowed by
     *         any existing filter and sort criteria.
     */
    public List<?> getDistinct(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField dataField = Relation.getDataField(attributeName);
        try
        {
            return query.executeDistinctValueForDataField(dataField);
        } catch (ODKDatastoreException e)
        {
            return Collections.emptyList();
        }
    }
}
