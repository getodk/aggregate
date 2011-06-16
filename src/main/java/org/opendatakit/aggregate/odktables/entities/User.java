package org.opendatakit.aggregate.odktables.entities;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class User extends TypedEntity
{

    public User(String userId, String userName, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Users.getInstance(cc));
        setId(userId);
        setName(userName);
    }

    public User(Entity entity, CallingContext cc) throws ODKDatastoreException
    {
        super(Users.getInstance(cc), entity);
    }

    public String getId()
    {
        DataField idField = getDataField(Users.USER_ID);
        return super.getEntity().getString(idField);
    }

    public void setId(String id)
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

    public boolean hasPerm(String tableUri, String permission)
            throws ODKDatastoreException
    {
        return !Permissions.getInstance(getCC()).query()
                .equal(Permissions.TABLE_URI, tableUri)
                .equal(permission, true)
                .execute().isEmpty();
    }

    public void setPerm(String tableUri, String permission, boolean value)
    {
        throw new NotImplementedException();
    }

    public List<Table> getOwnedTables()
    {
        throw new NotImplementedException();
    }

    public List<Table> getSynchedTables()
    {
        throw new NotImplementedException();
    }
}
