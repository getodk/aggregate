package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.command.synchronize.InsertSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.InsertSynchronizedRowsResult;
import org.opendatakit.aggregate.odktables.entity.InternalModification;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * InsertSynchronizedRowsLogic encapsulates the logic necessary to validate and
 * execute a InsertSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InsertSynchronizedRowsLogic extends
        CommandLogic<InsertSynchronizedRows>
{

    private final InsertSynchronizedRows insertSynchronizedRows;

    public InsertSynchronizedRowsLogic(
            InsertSynchronizedRows insertSynchronizedRows)
    {
        this.insertSynchronizedRows = insertSynchronizedRows;
    }

    @Override
    public InsertSynchronizedRowsResult execute(CallingContext cc)
            throws ODKDatastoreException, ODKTaskLockException
    {
        Users users = Users.getInstance(cc);
        TableEntries entries = TableEntries.getInstance(cc);
        UserTableMappings mappings = UserTableMappings.getInstance(cc);

        String requestingUserID = insertSynchronizedRows.getRequestingUserID();
        String tableID = insertSynchronizedRows.getTableID();
        int clientModificationNumber = insertSynchronizedRows
                .getModificationNumber();
        List<SynchronizedRow> newRows = insertSynchronizedRows.getNewRows();

        InternalUser requestUser = users.getByID(requestingUserID);

        InternalUserTableMapping mapping;
        try
        {
            mapping = mappings
                    .query()
                    .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                            requestUser.getAggregateIdentifier())
                    .equal(UserTableMappings.TABLE_ID, tableID).get();
        } catch (ODKDatastoreException e)
        {
            return InsertSynchronizedRowsResult.failure(tableID,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        String aggregateTableIdentifier = mapping.getAggregateTableIdentifier();

        if (!requestUser.hasPerm(aggregateTableIdentifier, Permissions.WRITE))
        {
            return InsertSynchronizedRowsResult.failure(tableID,
                    FailureReason.PERMISSION_DENIED);
        }

        InternalTableEntry entry = entries.get(aggregateTableIdentifier);

        if (entry.getModificationNumber() != clientModificationNumber)
        {
            return InsertSynchronizedRowsResult.failure(tableID,
                    FailureReason.OUT_OF_SYNCH);
        }

        // Get new modification number
        int newModificationNumber = CommandLogicFunctions
                .incrementModificationNumber(entry, aggregateTableIdentifier,
                        cc);

        // Insert new rows and create modification
        Modification clientModification = insertNewRows(newRows,
                aggregateTableIdentifier, newModificationNumber, cc);

        return InsertSynchronizedRowsResult.success(clientModification);
    }

    private Modification insertNewRows(List<SynchronizedRow> newRows,
            String aggregateTableIdentifier, int newModificationNumber,
            CallingContext cc) throws ODKDatastoreException
    {
        List<SynchronizedRow> insertedRows = new ArrayList<SynchronizedRow>();
        for (SynchronizedRow clientRow : newRows)
        {
            // Convert client's row into an internal row and save to datastore
            InternalRow row = new InternalRow(aggregateTableIdentifier, cc);
            for (Entry<String, String> rowEntry : clientRow
                    .getColumnValuePairs().entrySet())
            {
                row.setValue(rowEntry.getKey(), rowEntry.getValue());
            }
            row.save();

            // Add row to this modification
            String aggregateRowIdentifier = row.getAggregateIdentifier();
            InternalModification modification = new InternalModification(
                    aggregateTableIdentifier, newModificationNumber,
                    aggregateRowIdentifier, cc);
            modification.save();

            // Create the row that will be sent back to the client
            SynchronizedRow insertedRow = new SynchronizedRow();
            insertedRow.setRowID(clientRow.getRowID());
            insertedRow.setAggregateRowIdentifier(aggregateRowIdentifier);
            insertedRow.setRevisionTag(row.getRevisionTag());
            insertedRows.add(insertedRow);
        }
        Modification clientModification = new Modification(
                newModificationNumber, insertedRows);

        return clientModification;
    }
}