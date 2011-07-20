package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
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
            DataType columnType, boolean nullable, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Columns.getInstance(cc));
        setAggregateTableIdentifier(aggregateTableIdentifier);
        setName(columnName);
        setType(columnType);
        setNullable(nullable);
    }

    public InternalColumn(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Columns.getInstance(cc), entity);
    }

    public String getAggregateTableIdentifier()
    {
        return super.getEntity().getField(Columns.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String aggregateTableIdentifier)
    {
        super.getEntity().setField(Columns.AGGREGATE_TABLE_IDENTIFIER,
                aggregateTableIdentifier);
    }

    public String getName()
    {
        return super.getEntity().getField(Columns.COLUMN_NAME);
    }

    public void setName(String name)
    {
        super.getEntity().setField(Columns.COLUMN_NAME, name);
    }

    public DataType getType()
    {
        return DataType
                .valueOf(super.getEntity().getField(Columns.COLUMN_TYPE));
    }

    public void setType(DataType type)
    {
        super.getEntity().setField(Columns.COLUMN_TYPE, type.name());
    }

    public boolean getNullable()
    {
        DataField nullableField = getDataField(Columns.NULLABLE);
        return super.getEntity().getBoolean(nullableField);
    }

    public void setNullable(boolean nullable)
    {
        DataField nullableField = getDataField(Columns.NULLABLE);
        super.getEntity().setBoolean(nullableField, nullable);
    }

    public DataField toDataField()
    {
        return new DataField(Table.convertIdentifier(getAggregateIdentifier()),
                getType(), getNullable());
    }

    @Override
    public String toString()
    {
        return String.format("InternalColumn[name=%s, type=%s, nullable=%s",
                getName(), getType(), getNullable());
    }
}
