package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.InsertRowsResult;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * InsertRowsLogic encapsulates the logic necessary to validate and execute a
 * InsertRows Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertRowsLogic extends CommandLogic<InsertRows>
{

    private InsertRows insertRows;

    public InsertRowsLogic(InsertRows insertRows)
    {
        this.insertRows = insertRows;
    }

    @Override
    public InsertRowsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableEntries entries = TableEntries.getInstance(cc);
        Users users = Users.getInstance(cc);

        String requestingUserID = insertRows.getRequestingUserID();
        String tableUUID = insertRows.getTableUUID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();

        if (!requestingUser.hasPerm(tableUUID, Permissions.WRITE))
        {
            return InsertRowsResult.failure(tableUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        try
        {
            entries.get(tableUUID);
        } catch (ODKDatastoreException e)
        {
            return InsertRowsResult.failure(tableUUID,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        List<org.opendatakit.aggregate.odktables.client.entity.Row> clientRows = insertRows
                .getRows();
        Map<String, String> rowIDstorowUUIDs = new HashMap<String, String>();
        for (org.opendatakit.aggregate.odktables.client.entity.Row clientRow : clientRows)
        {
            Row row = new Row(tableUUID, cc);
            for (Entry<String, String> entry : clientRow.getColumnValuePairs()
                    .entrySet())
            {
                row.setValue(entry.getKey(), entry.getValue());
            }
            row.save();
            rowIDstorowUUIDs.put(clientRow.getRowID(), row.getUUID());
        }
        return InsertRowsResult.success(rowIDstorowUUIDs);
    }
}
