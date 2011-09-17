package org.opendatakit.aggregate.odktables.commandlogic.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.TableEntry;
import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.common.QueryForTables;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.common.QueryForTablesResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.Attribute;
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
            throws AggregateInternalErrorException
    {
        List<TableEntry> clientEntries;
        try
        {
            // get relation instances
            TableEntries entries = TableEntries.getInstance(cc);
            Users users = Users.getInstance(cc);
            Permissions permissions = Permissions.getInstance(cc);
            UserTableMappings mappings = UserTableMappings.getInstance(cc);
            Columns columns = Columns.getInstance(cc);
    
            // get request data
            String requestingUserID = queryForTables.getRequestingUserID();
    
            // retrieve request user
            InternalUser requestingUser = users.query("QueryForTablesLogic.execute")
                    .equal(Users.USER_ID, requestingUserID).get();
            String aggregateUserIdentifier = requestingUser
                    .getAggregateIdentifier();
    
            // get aggregateTableIdentifiers for which this user has read permissions
            @SuppressWarnings("unchecked")
            List<String> aggregateTableIdentifiers = (List<String>) permissions
                    .query("QueryForTablesLogic.execute")
                    .equal(Permissions.AGGREGATE_USER_IDENTIFIER,
                            aggregateUserIdentifier).equal(Permissions.READ, true)
                    .getDistinct(Permissions.AGGREGATE_TABLE_IDENTIFIER);
    
            // retrieve all entries corresponding to the identifiers
            boolean includesUsersTable = false;
            List<InternalTableEntry> allEntries = new ArrayList<InternalTableEntry>();
            for (String aggregateTableIdentifier : aggregateTableIdentifiers)
            {
                if (!aggregateTableIdentifier
                        .equals(users.getAggregateIdentifier()))
                {
                    allEntries.add(entries.getEntity(aggregateTableIdentifier));
                } else
                {
                    // special case Users table
                    includesUsersTable = true;
                }
            }
    
            // create a TableEntry to send back to client for each entry in table
            clientEntries = new ArrayList<TableEntry>();
            for (InternalTableEntry entry : allEntries)
            {
                String aggregateOwnerIdentifier = entry
                        .getAggregateOwnerIdentifier();
                String aggregateTableIdentifier = entry.getAggregateIdentifier();
                String tableName = entry.getName();
                boolean isSynchronized = entry.isSynchronized();
                InternalUser user = users.getEntity(aggregateOwnerIdentifier);
                String tableID = null;
                // if the user is registered with the table then set the tableID they are using
                try
                {
                    InternalUserTableMapping mapping = mappings
                            .query("QueryForTablesLogic.execute")
                            .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                                    aggregateUserIdentifier)
                            .equal(UserTableMappings.AGGREGATE_TABLE_IDENTIFIER,
                                    entry.getAggregateIdentifier()).get();
                    tableID = mapping.getTableID();
                } catch (ODKDatastoreException e)
                {
                    // Do nothing, this just means the user is not registered with this table right now. 
                }
    
                // send null for userID because we don't want people finding out what it is
                User clientUser = new User(null, user.getAggregateIdentifier(),
                        user.getName());
    
                // retrieve the columns for the table
                List<InternalColumn> internalColumns = columns
                        .query("QueryForTablesLogic.execute")
                        .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                                aggregateTableIdentifier).execute();
                List<Column> clientColumns = new ArrayList<Column>();
                for (InternalColumn internalColumn : internalColumns)
                {
                    Column clientColumn = new Column(internalColumn.getName(),
                            internalColumn.getType(), internalColumn.getNullable());
                    clientColumns.add(clientColumn);
                }
    
                // create the TableEntry
                TableEntry clientEntry = new TableEntry(clientUser,
                        entry.getAggregateIdentifier(), tableID, tableName,
                        clientColumns, isSynchronized);
                clientEntries.add(clientEntry);
            }
    
            // special case Users table
            if (includesUsersTable)
            {
                String aggregateIdentifier = users.getAggregateIdentifier();
                String tableID = null;
                String tableName = users.getName();
                List<Attribute> attributes = users.getAttributes();
                List<Column> clientColumns = new ArrayList<Column>();
                for (Attribute attribute : attributes)
                {
                    Column column = new Column(attribute.getName(),
                            attribute.getType(), attribute.isNullable());
                    clientColumns.add(column);
                }
                boolean isSynchronized = false;
                TableEntry clientEntry = new TableEntry(null, aggregateIdentifier,
                        tableID, tableName, clientColumns, isSynchronized);
                clientEntries.add(clientEntry);
            }
        }
        catch (ODKDatastoreException e)
        {
            throw new AggregateInternalErrorException(e.getMessage());
        }

        return QueryForTablesResult.success(clientEntries);
    }

}
