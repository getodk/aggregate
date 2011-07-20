package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.Collections;

import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.command.synchronize.CreateSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.CreateSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
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
        Users users = Users.getInstance(cc);
        UserTableMappings mappings = UserTableMappings.getInstance(cc);

        String tableID = createSynchronizedTable.getTableID();
        String requestingUserID = createSynchronizedTable.getRequestingUserID();

        InternalUser requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        String aggregateUserIdentifier = requestingUser
                .getAggregateIdentifier();

        // Check if table exists in Cursor
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
        // Create table in Tables, Columns, and Cursors.
        try
        {
            InternalTableEntry entry = new InternalTableEntry(aggregateUserIdentifier,
                    createSynchronizedTable.getTableName(), cc);
            entry.save();
            for (org.opendatakit.aggregate.odktables.client.entity.Column clientColumn : createSynchronizedTable
                    .getColumns())
            {
                InternalColumn column = new InternalColumn(entry.getAggregateIdentifier(),
                        clientColumn.getName(), clientColumn.getType(),
                        clientColumn.isNullable(), cc);
                column.save();
            }
            InternalUserTableMapping mapping = new InternalUserTableMapping(
                    aggregateUserIdentifier, entry.getAggregateIdentifier(),
                    tableID, cc);
            mapping.save();
        } catch (ODKEntityPersistException e)
        {
            // TODO: query to see what got created and delete it
        }
        org.opendatakit.aggregate.odktables.client.entity.Modification clientModification = new org.opendatakit.aggregate.odktables.client.entity.Modification(
                0, Collections.<SynchronizedRow> emptyList());
        return CreateSynchronizedTableResult.success(clientModification);

    }
}
