package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.common.ermodel.simple.Attribute;
import org.opendatakit.common.ermodel.simple.AttributeType;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Column is a (columnAggregate Identifier, aggregateTableIdentifier,
 * columnName, columnType, nullable) tuple, where
 * <ul>
 * <li>columnAggregate Identifier: the globally unique identifier of the column</li>
 * <li>aggregateTableIdentifier: the globally unique identifier of the table
 * this column belongs to</li>
 * <li>columnName: the name of the column. This must consist if upper case
 * letters, numbers, and underscores, and must start with an uppercase letter.</li>
 * <li>columnType: the type of the column. This is a DataField.DataType.</li>
 * <li>nullable: whether the column is allowed to contain a null value.</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalColumn extends TypedEntity
{

    public InternalColumn(String aggregateTableIdentifier, String columnName,
            AttributeType columnType, boolean nullable, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Columns.getInstance(cc).newEntity());
        setAggregateTableIdentifier(aggregateTableIdentifier);
        setName(columnName);
        setType(columnType);
        setNullable(nullable);
    }

    private InternalColumn(Entity entity) throws ODKDatastoreException
    {
        super(entity);
    }

    public String getAggregateTableIdentifier()
    {
        return entity.getString(Columns.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String value)
    {
        entity.set(Columns.AGGREGATE_TABLE_IDENTIFIER, value);
    }

    public String getName()
    {
        return entity.getString(Columns.COLUMN_NAME);
    }

    public void setName(String value)
    {
        entity.set(Columns.COLUMN_NAME, value);
    }

    public AttributeType getType()
    {
        return AttributeType.valueOf(entity.getString(Columns.COLUMN_TYPE));
    }

    public void setType(AttributeType value)
    {
        entity.set(Columns.COLUMN_TYPE, value.toString());
    }

    public boolean getNullable()
    {
        return entity.getBoolean(Columns.NULLABLE);
    }

    public void setNullable(boolean value)
    {
        entity.set(Columns.NULLABLE, value);
    }

    @Override
    public String toString()
    {
        return String.format("InternalColumn[name=%s, type=%s, nullable=%s",
                getName(), getType(), getNullable());
    }

    public static InternalColumn fromEntity(Entity entity)
            throws ODKDatastoreException
    {
        return new InternalColumn(entity);
    }
}
