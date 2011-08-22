package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * An InternalUserTableMapping is a (aggregateUserIdentifier,
 * aggregateTableIdentifier, tableID) tuple, where
 * <ul>
 * <li>aggregateUserIdentifier: the globally unique identifier of a user who is
 * using the table with aggregateTableIdentifier</li>
 * <li>aggregateTableIdentifier: the globally unique identifier of a table</li>
 * <li>tableID: the identifier which the user uses to identify the table. This
 * is unique only to that user.</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalUserTableMapping extends TypedEntity
{

    public InternalUserTableMapping(String aggregateUserIdentifier,
            String aggregateTableIdentifier, String tableID, CallingContext cc)
            throws ODKDatastoreException
    {
        super(UserTableMappings.getInstance(cc).newEntity());
        setUser(aggregateUserIdentifier);
        setAggregateTableIdentifier(aggregateTableIdentifier);
        setTableID(tableID);
    }

    public InternalUserTableMapping(Entity entity) throws ODKDatastoreException
    {
        super(entity);
    }

    public String getUser()
    {
        return entity.getString(UserTableMappings.AGGREGATE_USER_IDENTIFIER);
    }

    public void setUser(String value)
    {
        entity.set(UserTableMappings.AGGREGATE_USER_IDENTIFIER, value);
    }

    public String getAggregateTableIdentifier()
    {
        return entity.getString(UserTableMappings.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String value)
    {
        entity.set(UserTableMappings.AGGREGATE_TABLE_IDENTIFIER, value);
    }

    public String getTableID()
    {
        return entity.getString(UserTableMappings.TABLE_ID);
    }

    public void setTableID(String value)
    {
        entity.set(UserTableMappings.TABLE_ID, value);
    }

    @Override
    public String toString()
    {
        return String
                .format("InternalUserTableMapping[aggregateUserIdentifier=%s, aggregateTableIdentifier=%s, tableID=%s",
                        getUser(), getAggregateTableIdentifier(), getTableID());
    }

    public static InternalUserTableMapping fromEntity(Entity entity)
            throws ODKDatastoreException
    {
        return new InternalUserTableMapping(entity);
    }
}
