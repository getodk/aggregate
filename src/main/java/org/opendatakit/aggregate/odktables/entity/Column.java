package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Column is a (columnUUID, tableUUID, columnName, columnType, nullable) tuple,
 * where
 * <ul>
 * <li>columnUUID: the globally unique identifier of the column</li>
 * <li>tableUUID: the globally unique identifier of the table this column belongs
 * to</li>
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
public class Column extends TypedEntity
{

    public Column(String tableUUID, String columnName, DataType columnType,
            boolean nullable, CallingContext cc) throws ODKDatastoreException
    {
        super(Columns.getInstance(cc));
        setTableUUID(tableUUID);
        setName(columnName);
        setType(columnType);
        setNullable(nullable);
    }

    public Column(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Columns.getInstance(cc), entity);
    }

    public String getTableUUID()
    {
        return super.getEntity().getField(Columns.TABLE_UUID);
    }

    public void setTableUUID(String tableUUID)
    {
        super.getEntity().setField(Columns.TABLE_UUID, tableUUID);
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
        return new DataField(getName(), getType(), getNullable());
    }
}
