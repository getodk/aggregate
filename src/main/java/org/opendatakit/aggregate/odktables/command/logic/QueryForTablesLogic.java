package org.opendatakit.aggregate.odktables.command.logic;

import java.util.List;

import org.opendatakit.aggregate.odktables.CommandLogic;
import org.opendatakit.aggregate.odktables.client.TableList;
import org.opendatakit.aggregate.odktables.command.QueryForTables;
import org.opendatakit.aggregate.odktables.command.result.QueryForTablesResult;
import org.opendatakit.aggregate.odktables.relation.TableIndex;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.Entity;
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
        TableIndex index = TableIndex.getInstance(cc);
        Users users = Users.getInstance(cc);
        List<Entity> entities = index.getAllEntities(cc);
        TableList tableList = new TableList();
        for (Entity entity : entities)
        {
            String userId = entity.getField(TableIndex.USER_ID);
            String tableId = entity.getField(TableIndex.TABLE_ID);
            String tableName = entity.getField(TableIndex.TABLE_NAME);
            Entity user = users.getEntity(userId);
            String userName = user.getField(Users.USER_NAME);
            String uri = user.getUri();
            tableList.addEntry(uri, userName, unconvertTableId(tableId), tableName);
        }
        return QueryForTablesResult.success(tableList);
    }

}
