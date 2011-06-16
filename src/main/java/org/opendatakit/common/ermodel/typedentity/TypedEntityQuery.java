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
 * A RelationQuery defines a query over a specific Relation. TODO: document
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
    private Query query;

    public TypedEntityQuery(TypedEntityRelation<T> relation)
    {
        this.relation = relation;
        this.query = relation.createQuery();
    }

    public TypedEntityQuery<T> equal(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.EQUAL, value);
    }

    public TypedEntityQuery<T> greaterThan(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.GREATER_THAN, value);
    }

    public TypedEntityQuery<T> greaterThanOrEqual(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.GREATER_THAN_OR_EQUAL,
                value);
    }

    public TypedEntityQuery<T> lessThan(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.LESS_THAN, value);
    }

    public TypedEntityQuery<T> lessThanOrEqual(String fieldName, Object value)
    {
        return addFilter(fieldName, FilterOperation.LESS_THAN_OR_EQUAL, value);
    }

    private TypedEntityQuery<T> addFilter(String fieldName, FilterOperation op,
            Object value)
    {
        DataField field = relation.getDataField(fieldName);
        query.addFilter(field, op, value);
        return this;
    }

    public TypedEntityQuery<T> sortAscending(String fieldName)
    {
        return addSort(fieldName, Direction.ASCENDING);
    }

    public TypedEntityQuery<T> sortDescending(String fieldName)
    {
        return addSort(fieldName, Direction.DESCENDING);
    }

    private TypedEntityQuery<T> addSort(String fieldName, Direction direction)
    {
        DataField field = relation.getDataField(fieldName);
        query.addSort(field, direction);
        return this;
    }

    public TypedEntityQuery<T> include(String fieldName, Collection<?> values)
    {
        DataField field = relation.getDataField(fieldName);
        query.addValueSetFilter(field, values);
        return this;
    }

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

    public List<T> execute() throws ODKDatastoreException
    {
        return execute(0);
    }

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
