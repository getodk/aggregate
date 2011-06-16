package org.opendatakit.aggregate.odktables.entities;

import java.util.Collection;
import java.util.List;

import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Tables;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class Table extends TypedEntity
{

    private Collection<Column> columns;
    
    public Table(String ownerUri, String tableName, Collection<Column> columns,
            CallingContext cc) throws ODKDatastoreException
    {
        super(Tables.getInstance(cc));
        setOwnerUri(ownerUri);
        setName(tableName);
        this.columns = columns;
    }

    public Table(Entity entity, CallingContext cc) throws ODKDatastoreException
    {
        super(Tables.getInstance(cc), entity);
    }

    public String getOwnerUri()
    {
        return super.getEntity().getField(Tables.OWNER_URI);
    }

    public void setOwnerUri(String ownerUri)
    {
        super.getEntity().setField(Tables.OWNER_URI, ownerUri);
    }

    public String getName()
    {
        return super.getEntity().getField(Tables.TABLE_NAME);
    }

    public void setName(String name)
    {
        super.getEntity().setField(Tables.TABLE_NAME, name);
    }

    public int getModificationNumber()
    {
        DataField modificationNumberField = getDataField(Tables.MODIFICATION_NUMBER);
        return super.getEntity().getInteger(modificationNumberField);
    }

    public void incrementModificationNumber()
    {
        int modificationNumber = getModificationNumber();
        modificationNumber++;
        DataField modificationNumberField = getDataField(Tables.MODIFICATION_NUMBER);
        super.getEntity().setInteger(modificationNumberField,
                modificationNumber);
    }

    public Collection<Column> getColumns() throws ODKDatastoreException
    {
        if (this.columns == null)
        {
            String tableUri = super.getEntity().getUri();
            List<Column> columns = Columns.getInstance(getCC()).query()
                    .equal(Columns.TABLE_URI, tableUri).execute();
            this.columns = columns;
            return columns;
        }
        else
        {
            return this.columns;
        }
    }
}
