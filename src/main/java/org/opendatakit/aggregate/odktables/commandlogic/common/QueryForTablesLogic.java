package org.opendatakit.aggregate.odktables.commandlogic.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.TableEntry;
import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.command.common.QueryForTables;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.common.QueryForTablesResult;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
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
        UserTableMappings mappings = UserTableMappings.getInstance(cc);

        String requestingUserID = queryForTables.getRequestingUserID();

        InternalUser requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        String aggregateUserIdentifier = requestingUser
                .getAggregateIdentifier();
        List<String> aggregateTableIdentifiers = (List<String>) permissions
                .query()
                .equal(Permissions.AGGREGATE_USER_IDENTIFIER,
                        aggregateUserIdentifier).equal(Permissions.READ, true)
                .getDistinct(Permissions.AGGREGATE_TABLE_IDENTIFIER);

        List<InternalTableEntry> allEntries = new ArrayList<InternalTableEntry>();
        for (String aggregateTableIdentifier : aggregateTableIdentifiers)
        {
            allEntries.add(entries.getEntity(aggregateTableIdentifier));
        }

        List<TableEntry> clientEntries = new ArrayList<TableEntry>();
        for (InternalTableEntry entry : allEntries)
        {
            String aggregateOwnerIdentifier = entry
                    .getAggregateOwnerIdentifier();
            String tableName = entry.getName();
            boolean isSynchronized = entry.isSynchronized();
            InternalUser user = users.getEntity(aggregateOwnerIdentifier);
            String tableID = null;
            try
            {
                InternalUserTableMapping mapping = mappings
                        .query()
                        .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                                aggregateUserIdentifier)
                        .equal(UserTableMappings.AGGREGATE_TABLE_IDENTIFIER,
                                entry.getAggregateIdentifier()).get();
                tableID = mapping.getTableID();
            }
            catch (ODKDatastoreException e)
            {
               // Do nothing, this just means the user is not registered with this table right now. 
            }
            // send null for userID because we don't want people finding out what it is
            User clientUser = new User(null,
                    user.getAggregateIdentifier(), user.getName());

            TableEntry clientEntry = new TableEntry(clientUser,
                    entry.getAggregateIdentifier(), tableID, tableName,
                    isSynchronized);
            clientEntries.add(clientEntry);
        }

        return QueryForTablesResult.success(clientEntries);
    }

}
