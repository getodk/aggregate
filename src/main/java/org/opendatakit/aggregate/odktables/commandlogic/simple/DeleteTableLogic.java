package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.List;

import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.simple.DeleteTableResult;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Cursor;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Cursors;
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

        String tableId = this.deleteTable.getTableId();
        String userId = this.deleteTable.getUserId();

        try
        {
            User user = users.query().equal(Users.USER_ID, userId).get();
            String userUri = user.getUri();

            Cursor cursor = cursors.query().equal(Cursors.USER_UUID, userUri)
                    .equal(Cursors.TABLE_ID, tableId).get();
            String tableUri = cursor.getTableUUID();

            TableEntry table = tables.get(tableUri);
            List<Column> tableColumns = columns.query()
                    .equal(Columns.TABLE_UUID, tableUri).execute();

            try
            {
                cursor.delete();
                table.delete();
                for (Column column : tableColumns)
                    column.delete();
            } catch (ODKDatastoreException e)
            {
                // TODO: try delete again
            }
        } catch (ODKDatastoreException e)
        {
            // TODO: what if user, cursor, or table do not exist?
        }

        return DeleteTableResult.success(deleteTable.getTableId());
    }
}
