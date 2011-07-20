package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A User is a (aggregateUserIdentifier, userID, userName) tuple, where
 * <ul>
 * <li>aggregateUserIdentifier = the public unique identifier of a user</li>
 * <li>userID = the private unique identifier of the user, known only to the
 * user</li>
 * <li>userName = the human readable name of the user</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalUser extends TypedEntity
{

    public InternalUser(String userID, String userName, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Users.getInstance(cc));
        setID(userID);
        setName(userName);
    }

    public InternalUser(Entity entity, CallingContext cc)
            throws ODKDatastoreException
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

    public boolean hasPerm(String aggregateTableIdentifier, String permission)
            throws ODKDatastoreException
    {
        return Permissions
                .getInstance(getCC())
                .query()
                .equal(Permissions.AGGREGATE_TABLE_IDENTIFIER,
                        aggregateTableIdentifier).equal(permission, true)
                .exists();
    }

    @Override
    public String toString()
    {
        return String.format(
                "InternalUser[aggregateIdentifier=%s, userID=%s, name=%s",
                getAggregateIdentifier(), getID(), getName());
    }
}
