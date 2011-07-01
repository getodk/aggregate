package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Permission is a (tableUUID, userUUID, read, write, delete) tuple, where
 * <ul>
 * <li>tableUUID: the globally unique identifer of a table</li>
 * <li>userUUID: the globally unique identifier of a user who should have access
 * to the table</li>
 * <li>read: true if the user with userUUID should be able to read the table with
 * tableUUID</li>
 * <li>write: true if the user with userUUID should be able to write the table
 * with tableUUID</li>
 * <li>delete: true if the user with userUUID should be able to delete the table
 * with tableUUID</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class Permission extends TypedEntity
{

    public Permission(String tableUUID, String userUUID, boolean read,
            boolean write, boolean delete, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Permissions.getInstance(cc));
        setTableUUID(tableUUID);
        setUserUUID(userUUID);
        setRead(read);
        setWrite(write);
        setDelete(delete);
    }

    public Permission(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Permissions.getInstance(cc), entity);
    }

    public String getTableUUID()
    {
        return super.getEntity().getField(Permissions.TABLE_UUID);
    }

    public void setTableUUID(String tableUUID)
    {
        super.getEntity().setField(Permissions.TABLE_UUID, tableUUID);
    }

    public String getUserUUID()
    {
        return super.getEntity().getField(Permissions.USER_UUID);
    }

    public void setUserUUID(String userUUID)
    {
        super.getEntity().setField(Permissions.USER_UUID, userUUID);
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
