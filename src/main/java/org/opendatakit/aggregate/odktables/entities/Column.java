package org.opendatakit.aggregate.odktables.entities;

import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class Column extends TypedEntity
{

    public Column(String tableUri, String columnName, DataType columnType,
            boolean nullable, CallingContext cc) throws ODKDatastoreException
    {
        super(Columns.getInstance(cc));
        setTableUri(tableUri);
        setName(columnName);
        setType(columnType);
        setNullable(nullable);
    }

    public Column(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Columns.getInstance(cc), entity);
    }

    public String getTableUri()
    {
        return super.getEntity().getField(Columns.TABLE_URI);
    }

    public void setTableUri(String tableUri)
    {
        super.getEntity().setField(Columns.TABLE_URI, tableUri);
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
