package org.opendatakit.aggregate.odktables.commandlogic.simple;

import org.opendatakit.aggregate.odktables.command.logic.CreateTableLogic;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.CreateTableResult;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Cursor;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Cursors;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
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
        TableEntries tables = TableEntries.getInstance(cc);
        Users users = Users.getInstance(cc);
        Cursors cursors = Cursors.getInstance(cc);
        Columns columns = Columns.getInstance(cc);

        String tableId = createTable.getTableId();
        String userId = createTable.getUserId();
        User user = users.query().equal(Users.USER_ID, userId).get();

        // Check if user exists, if not return failure
        if (!users.query().equal(Users.USER_ID, userId).exists())
        {
            return CreateTableResult.failure(userId, tableId,
                    FailureReason.USER_DOES_NOT_EXIST);
        }
        // Check if table exists in Cursor
        // If table exists, return failure
        String userUri = user.getUri();
        if (cursors.query().equal(Cursors.USER_UUID, userUri)
                .equal(Cursors.TABLE_ID, tableId).exists())
        {
            return CreateTableResult.failure(userId, tableId,
                    FailureReason.TABLE_ALREADY_EXISTS);
        }
        // Create table in Tables, Columns, and Cursors.
        try
        {
            TableEntry table = new TableEntry(userUri, createTable.getTableName(), cc);
            table.save();
            for (org.opendatakit.aggregate.odktables.client.entity.Column clientColumn : createTable.getColumns())
            {
                Column column = new Column(table.getUri(), clientColumn.getName(), clientColumn.getType(), clientColumn.isNullable(), cc);
                column.save();
            }
            Cursor cursor = new Cursor(userUri, table.getUri(), tableId, cc);
            cursor.save();
        }
        catch (ODKEntityPersistException e)
        {
            // TODO: query to see what got created and delete it
            // TODO: add an internal error failure reason
        }
        return CreateTableResult.success(userId, tableId);
    }

    @Override
    public String toString()
    {
        return createTable.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof CreateTableLogic))
            return false;
        CreateTableLogic o = (CreateTableLogic) obj;
        return o.createTable.equals(this.createTable);
    }

    @Override
    public int hashCode()
    {
        return 36 * createTable.hashCode();
    }
}
