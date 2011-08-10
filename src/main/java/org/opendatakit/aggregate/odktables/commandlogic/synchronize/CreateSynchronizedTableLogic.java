package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.Collections;

import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.command.synchronize.CreateSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.CreateSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;

/**
 * CreateSynchronizedTableLogic encapsulates the logic necessary to validate and
 * execute a CreateSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CreateSynchronizedTableLogic extends
        CommandLogic<CreateSynchronizedTable>
{

    private final CreateSynchronizedTable createSynchronizedTable;

    public CreateSynchronizedTableLogic(
            CreateSynchronizedTable createSynchronizedTable)
    {
        this.createSynchronizedTable = createSynchronizedTable;
    }

    @Override
    public CreateSynchronizedTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        // get relation instances
        Users users = Users.getInstance(cc);
        UserTableMappings mappings = UserTableMappings.getInstance(cc);

        // get request data
        String tableID = createSynchronizedTable.getTableID();
        String requestingUserID = createSynchronizedTable.getRequestingUserID();

        // retrieve request user
        InternalUser requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        String aggregateUserIdentifier = requestingUser
                .getAggregateIdentifier();

        // Check if user already has a mapping using the tableID
        // If table exists, return failure
        if (mappings
                .query()
                .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                        aggregateUserIdentifier)
                .equal(UserTableMappings.TABLE_ID, tableID).exists())
        {
            return CreateSynchronizedTableResult.failure(tableID,
                    FailureReason.TABLE_ALREADY_EXISTS);
        }
        // Create the table
        try
        {
            // create the table entry
            InternalTableEntry entry = new InternalTableEntry(aggregateUserIdentifier,
                    createSynchronizedTable.getTableName(), true, cc);
            entry.save();
            // create the columns
            for (org.opendatakit.aggregate.odktables.client.entity.Column clientColumn : createSynchronizedTable
                    .getColumns())
            {
                InternalColumn column = new InternalColumn(entry.getAggregateIdentifier(),
                        clientColumn.getName(), clientColumn.getType(),
                        clientColumn.isNullable(), cc);
                column.save();
            }
            // create the mapping from aggregateTableIdentifier to user's tableID
            InternalUserTableMapping mapping = new InternalUserTableMapping(
                    aggregateUserIdentifier, entry.getAggregateIdentifier(),
                    tableID, cc);
            mapping.save();
            
            // make sure the user has full permissions on their own table
            InternalPermission perm = new InternalPermission(
                    entry.getAggregateIdentifier(), aggregateUserIdentifier,
                    true, true, true, cc);
            perm.save();
        } catch (ODKEntityPersistException e)
        {
            // TODO: query to see what got created and delete it?
        }
        // create initial empty modification
        org.opendatakit.aggregate.odktables.client.entity.Modification clientModification = new org.opendatakit.aggregate.odktables.client.entity.Modification(
                0, Collections.<SynchronizedRow> emptyList());
        
        return CreateSynchronizedTableResult.success(clientModification);
    }
}
