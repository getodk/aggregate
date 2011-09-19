package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.InsertRowsResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
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
            throws AggregateInternalErrorException
    {
        Map<String, String> rowIDstoaggregateRowIdentifiers;
        List<TypedEntity> entitiesToSave = new ArrayList<TypedEntity>();
        try
        {
            TableEntries entries = TableEntries.getInstance(cc);
            Users users = Users.getInstance(cc);
            UserTableMappings mappings = UserTableMappings.getInstance(cc);
            Columns columns = Columns.getInstance(cc);

            String requestingUserID = insertRows.getRequestingUserID();
            String tableID = insertRows.getTableID();

            InternalUser requestingUser = users.query("InsertRowsLogic.execute")
                    .equal(Users.USER_ID, requestingUserID).get();

            String aggregateRequestingUserIdentifier = requestingUser
                    .getAggregateIdentifier();

            InternalUserTableMapping mapping = mappings
                    .query("InsertRowsLogic.execute")
                    .equal(UserTableMappings.TABLE_ID, tableID)
                    .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                            aggregateRequestingUserIdentifier).get();

            String aggregateTableIdentifier = mapping
                    .getAggregateTableIdentifier();

            if (!requestingUser.hasPerm(aggregateTableIdentifier,
                    Permissions.WRITE))
            {
                return InsertRowsResult.failure(tableID,
                        FailureReason.PERMISSION_DENIED);
            }

            try
            {
                entries.getEntity(aggregateTableIdentifier);
            } catch (ODKDatastoreException e)
            {
                return InsertRowsResult.failure(tableID,
                        FailureReason.TABLE_DOES_NOT_EXIST);
            }

            List<Row> clientRows = insertRows.getRows();
            rowIDstoaggregateRowIdentifiers = new HashMap<String, String>();
            for (Row clientRow : clientRows)
            {
                InternalRow row = new InternalRow(aggregateTableIdentifier, cc);
                for (Entry<String, String> entry : clientRow
                        .getColumnValuePairs().entrySet())
                {
                    String columnName = entry.getKey();
                    try
                    {
                        InternalColumn col = columns
                                .query("InsertRowsLogic.execute")
                                .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                                        aggregateTableIdentifier)
                                .equal(Columns.COLUMN_NAME, columnName).get();
                        row.setValue(col.getAggregateIdentifier(),
                                entry.getValue());
                    } catch (ODKDatastoreException e)
                    {
                        return InsertRowsResult.failure(tableID, columnName);
                    }
                }
                entitiesToSave.add(row);
                rowIDstoaggregateRowIdentifiers.put(clientRow.getRowID(),
                        row.getAggregateIdentifier());
            }
        } catch (ODKDatastoreException e)
        {
            throw new AggregateInternalErrorException(e.getMessage());
        }

        boolean success = CommandLogicFunctions.saveEntities(entitiesToSave);
        if (!success)
            throw new SnafuException("Could not save entities: "
                    + entitiesToSave);

        return InsertRowsResult.success(rowIDstoaggregateRowIdentifiers);
    }
}
