package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Filters;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * *
 * <p>
 * An InternalFilter is a (aggregateUserIdentifier, aggregateTableIdentifier,
 * columnName, filterOperation, value) tuple, where
 * <ul>
 * <li>aggregateUserIdentifier: the globally unique identifier of the user this
 * filter is for.</li>
 * <li>aggregateTableIdentifier: the globally unique identifier of the table
 * this filter applies to</li>
 * <li>columnName: the name of the column for the filter.</li>
 * <li>filterOperation: the operation for the filter (e.g. GREATER_THAN, EQUAL,
 * etc.)</li>
 * <li>value: the value for the filter.</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalFilter extends TypedEntity
{

    public InternalFilter(String aggregateUserIdentifier,
            String aggregateTableIdentifier, String columnName,
            FilterOperation filterOperation, String value, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Filters.getInstance(cc).newEntity());
        setAggregateUserIdentifier(aggregateUserIdentifier);
        setAggregateTableIdentifier(aggregateTableIdentifier);
        setColumnName(columnName);
        setFilterOperation(filterOperation);
        setValue(value);
    }

    private InternalFilter(Entity entity) throws ODKDatastoreException
    {
        super(entity);
    }

    public String getAggregateUserIdentifier()
    {
        return entity.getString(Filters.AGGREGATE_USER_IDENTIFIER);
    }

    public void setAggregateUserIdentifier(String value)
    {
        entity.set(Filters.AGGREGATE_USER_IDENTIFIER, value);
    }

    public String getAggregateTableIdentifier()
    {
        return entity.getString(Filters.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String value)
    {
        entity.set(Filters.AGGREGATE_TABLE_IDENTIFIER, value);
    }

    public String getColumnName()
    {
        return entity.getString(Filters.COLUMN_NAME);
    }

    public void setColumnName(String value)
    {
        entity.set(Filters.COLUMN_NAME, value);
    }

    public FilterOperation getFilterOperation()
    {
        return FilterOperation.valueOf(entity
                .getString(Filters.FILTER_OPERATION));
    }

    public void setFilterOperation(FilterOperation value)
    {
        entity.set(Filters.FILTER_OPERATION, value.name());
    }

    public String getValue()
    {
        return entity.getString(Filters.VALUE);
    }

    public void setValue(String value)
    {
        entity.set(Filters.VALUE, value);
    }

    @Override
    public String toString()
    {
        return String.format(
                "InternalFilter[columnName=%s, filterOperation=%s, value=%s",
                getColumnName(), getFilterOperation(), getValue());
    }

    public static InternalFilter fromEntity(Entity entity)
            throws ODKDatastoreException
    {
        return new InternalFilter(entity);
    }
}
