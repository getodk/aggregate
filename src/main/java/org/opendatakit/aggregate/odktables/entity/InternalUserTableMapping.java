package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Cursor is a (aggregateUserIdentifier, aggregateTableIdentifier, tableID)
 * tuple, where
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
        super(UserTableMappings.getInstance(cc));
        setUser(aggregateUserIdentifier);
        setAggregateTableIdentifier(aggregateTableIdentifier);
        setTableID(tableID);
    }

    public InternalUserTableMapping(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(UserTableMappings.getInstance(cc), entity);
    }

    public String getUser()
    {
        return super.getEntity().getField(
                UserTableMappings.AGGREGATE_USER_IDENTIFIER);
    }

    public void setUser(String aggregateUserIdentifier)
    {
        super.getEntity().setField(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                aggregateUserIdentifier);
    }

    public String getAggregateTableIdentifier()
    {
        return super.getEntity().getField(
                UserTableMappings.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String aggregateTableIdentifier)
    {
        super.getEntity().setField(
                UserTableMappings.AGGREGATE_TABLE_IDENTIFIER,
                aggregateTableIdentifier);
    }

    public String getTableID()
    {
        return super.getEntity().getField(UserTableMappings.TABLE_ID);
    }

    public void setTableID(String tableID)
    {
        super.getEntity().setField(UserTableMappings.TABLE_ID, tableID);
    }

    @Override
    public String toString()
    {
        return String
                .format("InternalUserTableMapping[aggregateUserIdentifier=%s, aggregateTableIdentifier=%s, tableID=%s",
                        getUser(), getAggregateTableIdentifier(), getTableID());
    }
}
