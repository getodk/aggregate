package org.opendatakit.aggregate.odktables.commandlogic.simple;

import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.CreateTableResult;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Cursor;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Cursors;
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
        Cursors cursors = Cursors.getInstance(cc);

        String tableID = createTable.getTableID();
        String requestingUserID = createTable.getRequestingUserID();
        
        User requestingUser = users.query().equal(Users.USER_ID, requestingUserID).get();
        String userUUID = requestingUser.getUUID();

        // Check if table exists in Cursor
        // If table exists, return failure
        if (cursors.query().equal(Cursors.USER_UUID, userUUID)
                .equal(Cursors.TABLE_ID, tableID).exists())
        {
            return CreateTableResult.failure(tableID,
                    FailureReason.TABLE_ALREADY_EXISTS);
        }
        // Create table in Tables, Columns, and Cursors.
        try
        {
            TableEntry entry = new TableEntry(userUUID, createTable.getTableName(), cc);
            entry.save();
            for (org.opendatakit.aggregate.odktables.client.entity.Column clientColumn : createTable.getColumns())
            {
                Column column = new Column(entry.getUUID(), clientColumn.getName(), clientColumn.getType(), clientColumn.isNullable(), cc);
                column.save();
            }
            Cursor cursor = new Cursor(userUUID, entry.getUUID(), tableID, cc);
            cursor.save();
        }
        catch (ODKEntityPersistException e)
        {
            // TODO: query to see what got created and delete it
            // TODO: add an internal error failure reason
        }
        return CreateTableResult.success(tableID);
    }
}
