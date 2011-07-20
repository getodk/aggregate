package org.opendatakit.aggregate.odktables.command.logic;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.CommandLogic;
import org.opendatakit.aggregate.odktables.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.client.Row;
import org.opendatakit.aggregate.odktables.command.QueryForRows;
import org.opendatakit.aggregate.odktables.command.result.QueryForRowsResult;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.aggregate.odktables.relation.TableIndex;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.DataField;
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
        TableIndex index = TableIndex.getInstance(cc);
        Users users = Users.getInstance(cc);

        String userUri = queryForRows.getUserUri();
        String tableId = queryForRows.getTableId();
        String convertedTableId = convertTableId(tableId);

        if (!users.containsEntity(userUri, cc))
        {
            return QueryForRowsResult.failure(userUri, tableId,
                    FailureReason.USER_DOES_NOT_EXIST);
        }
        Entity user = users.getEntity(userUri, cc);
        String convertedUserId = user.getField(Users.USER_ID);

        if (!index.tableExists(convertedUserId, convertedTableId))
        {
            return QueryForRowsResult.failure(userUri, tableId,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        Table table = index.getTable(convertedUserId, convertedTableId);
        List<DataField> fields = table.getDataFields();
        List<Entity> entities = table.getAllEntities(cc);
        List<Row> rows = new ArrayList<Row>();

        for (Entity entity : entities)
        {
            Row row = new Row(getRowId(entity.getUri()));
            for (DataField field : fields)
            {
                String column = field.getName();
                String value = entity.getField(column);
                row.setColumn(column, value);
            }
            rows.add(row);
        }

        return QueryForRowsResult.success(userUri, tableId, rows);
    }
}
