package org.opendatakit.aggregate.odktables.entities;

import org.opendatakit.aggregate.odktables.relation.Cursors;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class Cursor extends TypedEntity
{

    public Cursor(String userUri, String tableUri, String tableId,
            CallingContext cc) throws ODKDatastoreException
    {
        super(Cursors.getInstance(cc));
        setUser(userUri);
        setTable(tableUri);
        setTableId(tableId);
    }

    public Cursor(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Cursors.getInstance(cc), entity);
    }

    public String getUser()
    {
        return super.getEntity().getField(Cursors.USER_URI);
    }

    public void setUser(String userUri)
    {
        super.getEntity().setField(Cursors.USER_URI, userUri);
    }

    public String getTable()
    {
        return super.getEntity().getField(Cursors.TABLE_URI);
    }

    public void setTable(String tableUri)
    {
        super.getEntity().setField(Cursors.TABLE_URI, tableUri);
    }

    public String getTableId()
    {
        return super.getEntity().getField(Cursors.TABLE_ID);
    }

    public void setTableId(String tableId)
    {
        super.getEntity().setField(Cursors.TABLE_ID, tableId);
    }
}
