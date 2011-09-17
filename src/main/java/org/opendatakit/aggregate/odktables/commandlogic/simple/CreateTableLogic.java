package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.CreateTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
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
    public CreateTableResult execute(CallingContext cc) throws SnafuException,
            AggregateInternalErrorException
    {
        List<TypedEntity> entitiesToSave = new ArrayList<TypedEntity>();
        String tableID;
        try
        {
            Users users = Users.getInstance(cc);
            UserTableMappings cursors = UserTableMappings.getInstance(cc);

            tableID = createTable.getTableID();
            String requestingUserID = createTable.getRequestingUserID();

            InternalUser requestingUser = users.query("CreateTableLogic.execute")
                    .equal(Users.USER_ID, requestingUserID).get();
            String aggregateUserIdentifier = requestingUser
                    .getAggregateIdentifier();

            // Check if table exists in Cursor
            // If table exists, return failure
            if (cursors
                    .query("CreateTableLogic.execute")
                    .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                            aggregateUserIdentifier)
                    .equal(UserTableMappings.TABLE_ID, tableID).exists())
            {
                return CreateTableResult.failure(tableID,
                        FailureReason.TABLE_ALREADY_EXISTS);
            }
            // Create table
            InternalTableEntry entry = new InternalTableEntry(
                    aggregateUserIdentifier, createTable.getTableName(), false,
                    cc);
            entitiesToSave.add(entry);
            for (org.opendatakit.aggregate.odktables.client.entity.Column clientColumn : createTable
                    .getColumns())
            {
                InternalColumn column = new InternalColumn(
                        entry.getAggregateIdentifier(), clientColumn.getName(),
                        clientColumn.getType(), clientColumn.isNullable(), cc);
                entitiesToSave.add(column);
            }
            InternalUserTableMapping mapping = new InternalUserTableMapping(
                    aggregateUserIdentifier, entry.getAggregateIdentifier(),
                    tableID, cc);
            entitiesToSave.add(mapping);

            // Add creation user's full permissions
            InternalPermission perm = new InternalPermission(
                    entry.getAggregateIdentifier(), aggregateUserIdentifier,
                    true, true, true, cc);
            entitiesToSave.add(perm);
        } catch (ODKDatastoreException e)
        {
            throw new AggregateInternalErrorException(e.getMessage());
        }

        boolean success = CommandLogicFunctions.saveEntities(entitiesToSave);
        if (!success)
        {
            throw new SnafuException("Could not save entities: "
                    + entitiesToSave);
        }

        return CreateTableResult.success(tableID);
    }
}
