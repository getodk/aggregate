package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.List;

import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.DeleteTableResult;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Cursor;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Cursors;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * DeleteTableLogic encapsulates the logic necessary to validate and execute a
 * DeleteTable Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteTableLogic extends CommandLogic<DeleteTable>
{

    private final DeleteTable deleteTable;

    public DeleteTableLogic(DeleteTable deleteTable)
    {
        this.deleteTable = deleteTable;
    }

    @Override
    public DeleteTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableEntries tables = TableEntries.getInstance(cc);
        Users users = Users.getInstance(cc);
        Cursors cursors = Cursors.getInstance(cc);
        Columns columns = Columns.getInstance(cc);

        String tableUUID = this.deleteTable.getTableUUID();
        String requestingUserID = this.deleteTable.getRequestingUserID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();

        if (!requestingUser.hasPerm(tableUUID, Permissions.DELETE))
        {
            return DeleteTableResult.failure(tableUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        try
        {
            String userUUID = requestingUser.getUUID();

            Cursor cursor = cursors.query().equal(Cursors.USER_UUID, userUUID)
                    .equal(Cursors.TABLE_UUID, tableUUID).get();

            TableEntry entry = tables.get(tableUUID);
            List<Column> tableColumns = columns.query()
                    .equal(Columns.TABLE_UUID, tableUUID).execute();

            try
            {
                cursor.delete();
                entry.delete();
                for (Column column : tableColumns)
                    column.delete();
            } catch (ODKDatastoreException e)
            {
                // TODO: try delete again
            }
        } catch (ODKDatastoreException e)
        {
            return DeleteTableResult.failure(tableUUID,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        return DeleteTableResult.success();
    }
}
