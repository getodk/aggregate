package org.opendatakit.aggregate.odktables.entity;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A User is a (userUUID, userID, userName) tuple, where
 * <ul>
 * <li>userUUID = the public unique identifier of a user</li>
 * <li>userID = the private unique identifier of the user, known only to the
 * user</li>
 * <li>userName = the human readable name of the user</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class User extends TypedEntity
{

    public User(String userID, String userName, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Users.getInstance(cc));
        setID(userID);
        setName(userName);
    }

    public User(Entity entity, CallingContext cc) throws ODKDatastoreException
    {
        super(Users.getInstance(cc), entity);
    }

    public String getID()
    {
        DataField idField = getDataField(Users.USER_ID);
        return super.getEntity().getString(idField);
    }

    public void setID(String id)
    {
        super.getEntity().setField(Users.USER_ID, id);
    }

    public String getName()
    {
        DataField nameField = getDataField(Users.USER_NAME);
        return super.getEntity().getString(nameField);
    }

    public void setName(String name)
    {
        super.getEntity().setField(Users.USER_NAME, name);
    }

    public boolean hasPerm(String tableUUID, String permission)
            throws ODKDatastoreException
    {
        return Permissions.getInstance(getCC()).query()
                .equal(Permissions.TABLE_UUID, tableUUID).equal(permission, true)
                .exists();
    }

    public void setPerm(String tableUUID, String permission, boolean value)
    {
        throw new NotImplementedException();
    }

    public List<TableEntry> getOwnedTables()
    {
        throw new NotImplementedException();
    }

    public List<TableEntry> getSynchedTables()
    {
        throw new NotImplementedException();
    }
}
