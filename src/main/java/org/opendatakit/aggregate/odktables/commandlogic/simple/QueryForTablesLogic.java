package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.command.simple.QueryForTables;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.simple.QueryForTablesResult;
import org.opendatakit.aggregate.odktables.entity.Cursor;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Cursors;
import org.opendatakit.aggregate.odktables.relation.Permissions;
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
        TableEntries entries = TableEntries.getInstance(cc);
        Users users = Users.getInstance(cc);
        Permissions permissions = Permissions.getInstance(cc);
        Cursors cursors = Cursors.getInstance(cc);

        String requestingUserID = queryForTables.getRequestingUserID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        String userUUID = requestingUser.getUUID();
        List<String> tableUUIDs = (List<String>) permissions.query()
                .equal(Permissions.USER_UUID, userUUID)
                .equal(Permissions.READ, true)
                .getDistinct(Permissions.TABLE_UUID);

        List<TableEntry> allEntries = new ArrayList<TableEntry>();
        for (String tableUUID : tableUUIDs)
        {
            allEntries.add(entries.get(tableUUID));
        }

        List<org.opendatakit.aggregate.odktables.client.entity.TableEntry> clientEntries = new ArrayList<org.opendatakit.aggregate.odktables.client.entity.TableEntry>();
        for (TableEntry entry : allEntries)
        {
            String ownerUUID = entry.getOwnerUUID();
            String tableName = entry.getName();
            User user = users.get(ownerUUID);
            Cursor cursor = cursors.query().equal(Cursors.USER_UUID, userUUID)
                    .equal(Cursors.TABLE_UUID, entry.getUUID()).get();
            String tableID = cursor.getTableID();
            org.opendatakit.aggregate.odktables.client.entity.User clientUser = new org.opendatakit.aggregate.odktables.client.entity.User(
                    user.getID(), user.getUUID(), user.getName());

            org.opendatakit.aggregate.odktables.client.entity.TableEntry clientEntry = new org.opendatakit.aggregate.odktables.client.entity.TableEntry(
                    clientUser, tableID, tableName);
            clientEntries.add(clientEntry);
        }

        return QueryForTablesResult.success(clientEntries);
    }

}
