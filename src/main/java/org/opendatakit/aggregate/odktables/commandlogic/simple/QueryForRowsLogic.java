package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.command.simple.QueryForRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.QueryForRowsResult;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Rows;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * QueryForRowsLogic encapsulates the logic necessary to validate and execute a
 * QueryForRows Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class QueryForRowsLogic extends CommandLogic<QueryForRows>
{

    private QueryForRows queryForRows;

    public QueryForRowsLogic(QueryForRows queryForRows)
    {
        this.queryForRows = queryForRows;
    }

    @Override
    public QueryForRowsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableEntries entries = TableEntries.getInstance(cc);
        Users users = Users.getInstance(cc);
        Columns columns = Columns.getInstance(cc);

        String requestingUserID = queryForRows.getRequestingUserID();
        String tableUUID = queryForRows.getTableUUID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();

        if (!requestingUser.hasPerm(tableUUID, Permissions.READ))
        {
            return QueryForRowsResult.failure(tableUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        try
        {
            entries.get(tableUUID);
        } catch (ODKDatastoreException e)
        {
            return QueryForRowsResult.failure(tableUUID,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        Rows table = Rows.getInstance(tableUUID, cc);
        List<Row> rows = table.query().execute();

        List<String> columnNames = (List<String>) columns.query()
                .equal(Columns.TABLE_UUID, tableUUID)
                .getDistinct(Columns.COLUMN_NAME);
        List<org.opendatakit.aggregate.odktables.client.entity.Row> clientRows = new ArrayList<org.opendatakit.aggregate.odktables.client.entity.Row>();

        for (Row row : rows)
        {
            org.opendatakit.aggregate.odktables.client.entity.Row clientRow = new org.opendatakit.aggregate.odktables.client.entity.Row();
            clientRow.setRowUUID(row.getUUID());
            for (String columnName : columnNames)
            {
                clientRow.setValue(columnName, row.getValue(columnName));
            }
            clientRows.add(clientRow);
        }

        return QueryForRowsResult.success(tableUUID, clientRows);
    }
}
