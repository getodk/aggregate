package org.opendatakit.aggregate.odktables.entities;

import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class Permission extends TypedEntity
{

    public Permission(String tableUri, String userUri, boolean read,
            boolean write, boolean delete, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Permissions.getInstance(cc));
        setTableUri(tableUri);
        setUserUri(userUri);
        setRead(read);
        setWrite(write);
        setDelete(delete);
    }

    public Permission(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Permissions.getInstance(cc), entity);
    }

    public String getTableUri()
    {
        return super.getEntity().getField(Permissions.TABLE_URI);
    }

    public void setTableUri(String tableUri)
    {
        super.getEntity().setField(Permissions.TABLE_URI, tableUri);
    }

    public String getUserUri()
    {
        return super.getEntity().getField(Permissions.USER_URI);
    }

    public void setUserUri(String userUri)
    {
        super.getEntity().setField(Permissions.USER_URI, userUri);
    }

    public boolean getRead()
    {
        DataField readField = getDataField(Permissions.READ);
        return super.getEntity().getBoolean(readField);
    }

    public void setRead(boolean read)
    {
        DataField readField = getDataField(Permissions.READ);
        super.getEntity().setBoolean(readField, read);
    }

    public boolean getWrite()
    {
        DataField writeField = getDataField(Permissions.WRITE);
        return super.getEntity().getBoolean(writeField);
    }

    public void setWrite(boolean write)
    {
        DataField writeField = getDataField(Permissions.WRITE);
        super.getEntity().setBoolean(writeField, write);
    }

    public boolean getDelete()
    {
        DataField deleteField = getDataField(Permissions.DELETE);
        return super.getEntity().getBoolean(deleteField);
    }

    public void setDelete(boolean delete)
    {
        DataField deleteField = getDataField(Permissions.DELETE);
        super.getEntity().setBoolean(deleteField, delete);
    }
}
