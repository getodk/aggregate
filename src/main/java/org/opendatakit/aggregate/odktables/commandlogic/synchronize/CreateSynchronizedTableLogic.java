package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.common.CreateSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.CreateSynchronizedTableResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * CreateSynchronizedTableLogic encapsulates the logic necessary to validate and execute a
 * CreateSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CreateSynchronizedTableLogic extends CommandLogic<CreateSynchronizedTable>
{

    private final CreateSynchronizedTable createSynchronizedTable;

    public CreateSynchronizedTableLogic(CreateSynchronizedTable createSynchronizedTable)
    {
        this.createSynchronizedTable = createSynchronizedTable;
    }

    @Override
    public CreateSynchronizedTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        Cursors cursors = Cursors.getInstance(cc);

        String tableID = createSynchronizedTable.getTableID();
        String requestingUserID = createSynchronizedTable.getRequestingUserID();
        
        User requestingUser = users.query().equal(Users.USER_ID, requestingUserID).get();
        String userUUID = requestingUser.getUUID();

        // Check if table exists in Cursor
        // If table exists, return failure
        if (cursors.query().equal(Cursors.USER_UUID, userUUID)
                .equal(Cursors.TABLE_ID, tableID).exists())
        {
            return CreateSynchronizedTableResult.failure(tableID,
                    FailureReason.TABLE_ALREADY_EXISTS);
        }
        // Create table in Tables, Columns, and Cursors.
        try
        {
            TableEntry entry = new TableEntry(userUUID, createSynchronizedTable.getTableName(), cc);
            entry.save();
            for (org.opendatakit.aggregate.odktables.client.entity.Column clientColumn : createSynchronizedTable.getColumns())
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
        }
        org.opendatakit.aggregate.odktables.client.entity.Modification clientModification = new org.opendatakit.aggregate.odktables.client.entity.Modification(0, Collections.<SynchronizedRow>emptyList();
        return CreateSynchronizedTableResult.success(clientModification);

    }
}
