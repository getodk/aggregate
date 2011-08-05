package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * An InternalPermission is a (aggregateTableIdentifier,
 * aggregateUserIdentifier, read, write, delete) tuple, where
 * <ul>
 * <li>aggregateTableIdentifier: the globally unique identifer of a table</li>
 * <li>aggregateUserIdentifier: the globally unique identifier of a user who
 * should have access to the table</li>
 * <li>read: true if the user with userAggregate Identifier should be able to
 * read the table with tableAggregate Identifier</li>
 * <li>write: true if the user with userAggregate Identifier should be able to
 * write the table with tableAggregate Identifier</li>
 * <li>delete: true if the user with userAggregate Identifier should be able to
 * delete the table with tableAggregate Identifier</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalPermission extends TypedEntity
{

    public InternalPermission(String aggregateTableIdentifier,
            String aggregateUserIdentifier, boolean read, boolean write,
            boolean delete, CallingContext cc) throws ODKDatastoreException
    {
        super(Permissions.getInstance(cc).newEntity());
        setAggregateTableIdentifier(aggregateTableIdentifier);
        setAggregateUserIdentifier(aggregateUserIdentifier);
        setRead(read);
        setWrite(write);
        setDelete(delete);
    }

    public InternalPermission(Entity entity) throws ODKDatastoreException
    {
        super(entity);
    }

    public String getAggregateTableIdentifier()
    {
        return entity.getString(Permissions.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String value)
    {
        entity.set(Permissions.AGGREGATE_TABLE_IDENTIFIER, value);
    }

    public String getAggregateUserIdentifier()
    {
        return entity.getString(Permissions.AGGREGATE_USER_IDENTIFIER);
    }

    public void setAggregateUserIdentifier(String value)
    {
        entity.set(Permissions.AGGREGATE_USER_IDENTIFIER, value);
    }

    public boolean getRead()
    {
        return entity.getBoolean(Permissions.READ);
    }

    public void setRead(boolean value)
    {
        entity.set(Permissions.READ, value);
    }

    public boolean getWrite()
    {
        return entity.getBoolean(Permissions.WRITE);
    }

    public void setWrite(boolean value)
    {
        entity.set(Permissions.WRITE, value);
    }

    public boolean getDelete()
    {
        return entity.getBoolean(Permissions.DELETE);
    }

    public void setDelete(boolean value)
    {
        entity.set(Permissions.DELETE, value);
    }

    @Override
    public String toString()
    {
        return String
                .format("InternalPermission[aggregateTableIdentifier=%s, aggregateUserIdentifier=%s, read=%s, write=%s, delete=%s",
                        getAggregateTableIdentifier(),
                        getAggregateUserIdentifier(), getRead(), getWrite(),
                        getDelete());
    }

    public static InternalPermission fromEntity(Entity entity)
            throws ODKDatastoreException
    {
        return new InternalPermission(entity);
    }
}
