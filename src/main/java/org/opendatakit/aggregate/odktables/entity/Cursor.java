package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Cursors;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Cursor is a (userUUID, tableUUID, tableID) tuple, where
 * <ul>
 * <li>userUUID: the globally unique identifier of a user who is using the table
 * with tableUUID</li>
 * <li>tableUUID: the globally unique identifier of a table</li>
 * <li>tableID: the identifier which the user uses to identify the table. This
 * is unique only to that user.</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class Cursor extends TypedEntity
{

    public Cursor(String userUUID, String tableUUID, String tableID,
            CallingContext cc) throws ODKDatastoreException
    {
        super(Cursors.getInstance(cc));
        setUser(userUUID);
        setTableUUID(tableUUID);
        setTableID(tableID);
    }

    public Cursor(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Cursors.getInstance(cc), entity);
    }

    public String getUser()
    {
        return super.getEntity().getField(Cursors.USER_UUID);
    }

    public void setUser(String userUUID)
    {
        super.getEntity().setField(Cursors.USER_UUID, userUUID);
    }

    public String getTableUUID()
    {
        return super.getEntity().getField(Cursors.TABLE_UUID);
    }

    public void setTableUUID(String tableUUID)
    {
        super.getEntity().setField(Cursors.TABLE_UUID, tableUUID);
    }

    public String getTableID()
    {
        return super.getEntity().getField(Cursors.TABLE_ID);
    }

    public void setTableID(String tableID)
    {
        super.getEntity().setField(Cursors.TABLE_ID, tableID);
    }
}
