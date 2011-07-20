package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Permission is a (tableAggregate Identifier, userAggregate Identifier, read,
 * write, delete) tuple, where
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
        super(Permissions.getInstance(cc));
        setAggregateTableIdentifier(aggregateTableIdentifier);
        setAggregateUserIdentifier(aggregateUserIdentifier);
        setRead(read);
        setWrite(write);
        setDelete(delete);
    }

    public InternalPermission(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Permissions.getInstance(cc), entity);
    }

    public String getAggregateTableIdentifier()
    {
        return super.getEntity().getField(
                Permissions.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String aggregateTableIdentifier)
    {
        super.getEntity().setField(Permissions.AGGREGATE_TABLE_IDENTIFIER,
                aggregateTableIdentifier);
    }

    public String getAggregateUserIdentifier()
    {
        return super.getEntity()
                .getField(Permissions.AGGREGATE_USER_IDENTIFIER);
    }

    public void setAggregateUserIdentifier(String aggregateUserIdentifier)
    {
        super.getEntity().setField(Permissions.AGGREGATE_USER_IDENTIFIER,
                aggregateUserIdentifier);
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
