package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.TableList;
import org.opendatakit.aggregate.odktables.command.simple.QueryForTables;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.simple.QueryForTablesResult;
import org.opendatakit.aggregate.odktables.entity.Cursor;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Cursors;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * QueryForTablesLogic encapsulates the logic necessary to validate and execute
 * a QueryForTables Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class QueryForTablesLogic extends CommandLogic<QueryForTables>
{
    private final QueryForTables queryForTables;

    public QueryForTablesLogic(QueryForTables queryForTables)
    {
        this.queryForTables = queryForTables;
    }

    @Override
    public QueryForTablesResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableEntries tables = TableEntries.getInstance(cc);
        Users users = Users.getInstance(cc);
        Cursors cursors = Cursors.getInstance(cc);

        List<TableEntry> allEntries = tables.query().execute();

        TableList tableList = new TableList();
        for (TableEntry entry : allEntries)
        {
            String userUUID = entry.getOwnerUUID();
            String tableName = entry.getName();
            User user = users.get(userUUID);
            String userName = user.getName();
            Cursor cursor = cursors.query().equal(Cursors.USER_UUID, userUUID)
                    .equal(Cursors.TABLE_UUID, entry.getUri()).get();
            String tableId = cursor.getTableID();
            
            tableList.addEntry(userUUID, userName, tableId, tableName);
        }
        return QueryForTablesResult.success(tableList);
    }

}
