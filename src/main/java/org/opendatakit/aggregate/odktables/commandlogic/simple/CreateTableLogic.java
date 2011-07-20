package org.opendatakit.aggregate.odktables.commandlogic.simple;

import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.CreateTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;

/**
 * CreateTableLogic encapsulates the logic necessary to validate and execute a
 * CreateTable Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateTableLogic extends CommandLogic<CreateTable>
{

    private final CreateTable createTable;

    public CreateTableLogic(CreateTable createTable)
    {
        this.createTable = createTable;
    }

    @Override
    public CreateTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        UserTableMappings cursors = UserTableMappings.getInstance(cc);

        String tableID = createTable.getTableID();
        String requestingUserID = createTable.getRequestingUserID();

        InternalUser requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        String aggregateUserIdentifier = requestingUser
                .getAggregateIdentifier();

        // Check if table exists in Cursor
        // If table exists, return failure
        if (cursors
                .query()
                .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                        aggregateUserIdentifier)
                .equal(UserTableMappings.TABLE_ID, tableID).exists())
        {
            return CreateTableResult.failure(tableID,
                    FailureReason.TABLE_ALREADY_EXISTS);
        }
        // Create table in Tables, Columns, and Cursors.
        try
        {
            InternalTableEntry entry = new InternalTableEntry(aggregateUserIdentifier,
                    createTable.getTableName(), cc);
            entry.save();
            for (org.opendatakit.aggregate.odktables.client.entity.Column clientColumn : createTable
                    .getColumns())
            {
                InternalColumn column = new InternalColumn(entry.getAggregateIdentifier(),
                        clientColumn.getName(), clientColumn.getType(),
                        clientColumn.isNullable(), cc);
                column.save();
            }
            InternalUserTableMapping cursor = new InternalUserTableMapping(
                    aggregateUserIdentifier, entry.getAggregateIdentifier(),
                    tableID, cc);
            cursor.save();
        } catch (ODKEntityPersistException e)
        {
            // TODO: query to see what got created and delete it
            // TODO: add an internal error failure reason
        }
        return CreateTableResult.success(tableID);
    }
}
