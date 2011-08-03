package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.exception.ColumnDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.RowOutOfSynchException;
import org.opendatakit.aggregate.odktables.command.synchronize.UpdateSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.UpdateSynchronizedRowsResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalModification;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * UpdateSynchronizedRowsLogic encapsulates the logic necessary to validate and
 * execute a UpdateSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class UpdateSynchronizedRowsLogic extends
        CommandLogic<UpdateSynchronizedRows>
{

    private final UpdateSynchronizedRows updateSynchronizedRows;

    public UpdateSynchronizedRowsLogic(
            UpdateSynchronizedRows updateSynchronizedRows)
    {
        this.updateSynchronizedRows = updateSynchronizedRows;
    }

    @Override
    public UpdateSynchronizedRowsResult execute(CallingContext cc)
            throws ODKDatastoreException, ODKTaskLockException
    {
        Users users = Users.getInstance(cc);
        TableEntries entries = TableEntries.getInstance(cc);
        UserTableMappings mappings = UserTableMappings.getInstance(cc);
        Columns columns = Columns.getInstance(cc);

        String requestingUserID = updateSynchronizedRows.getRequestingUserID();
        String tableID = updateSynchronizedRows.getTableID();
        int clientModificationNumber = updateSynchronizedRows
                .getModificationNumber();
        List<SynchronizedRow> changedRows = updateSynchronizedRows
                .getChangedRows();

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
            return UpdateSynchronizedRowsResult.failure(null, tableID, null,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        String aggregateTableIdentifier = mapping.getAggregateTableIdentifier();

        if (!requestUser.hasPerm(aggregateTableIdentifier, Permissions.WRITE))
        {
            return UpdateSynchronizedRowsResult.failure(null, tableID, null,
                    FailureReason.PERMISSION_DENIED);
        }

        InternalTableEntry entry = entries.getEntity(aggregateTableIdentifier);

        if (entry.getModificationNumber() != clientModificationNumber)
        {
            return UpdateSynchronizedRowsResult.failure(null, tableID, null,
                    FailureReason.OUT_OF_SYNCH);
        }

        // Get new modification number
        int newModificationNumber = CommandLogicFunctions
                .incrementModificationNumber(entry, aggregateTableIdentifier,
                        cc);

        // Update changed rows and create modification
        Modification clientModification;
        try
        {
            clientModification = updateChangedRows(changedRows,
                    aggregateTableIdentifier, newModificationNumber, columns,
                    cc);
        } catch (RowOutOfSynchException e)
        {
            return UpdateSynchronizedRowsResult.failure(
                    e.getAggregateRowIdentifier(), tableID, null,
                    FailureReason.ROW_OUT_OF_SYNCH);
        } catch (ColumnDoesNotExistException e)
        {
            return UpdateSynchronizedRowsResult.failure(null, tableID,
                    e.getBadColumnName(), FailureReason.COLUMN_DOES_NOT_EXIST);
        }

        return UpdateSynchronizedRowsResult.success(clientModification);
    }

    private Modification updateChangedRows(List<SynchronizedRow> changedRows,
            String aggregateTableIdentifier, int newModificationNumber,
            Columns columns, CallingContext cc) throws ODKDatastoreException,
            RowOutOfSynchException, ColumnDoesNotExistException
    {
        List<SynchronizedRow> updatedRows = new ArrayList<SynchronizedRow>();
        Table table = Table.getInstance(aggregateTableIdentifier, cc);
        for (SynchronizedRow clientRow : changedRows)
        {
            // Get original row and make sure revisionTags match
            InternalRow row = table.getEntity(clientRow
                    .getAggregateRowIdentifier());
            if (!row.getRevisionTag().equals(clientRow.getRevisionTag()))
            {
                throw new RowOutOfSynchException(row.getAggregateIdentifier());
            }

            // Save values in new row
            for (Entry<String, String> rowEntry : clientRow
                    .getColumnValuePairs().entrySet())
            {
                InternalColumn col;
                try
                {
                    col = columns
                            .query()
                            .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                                    aggregateTableIdentifier)
                            .equal(Columns.COLUMN_NAME, rowEntry.getKey())
                            .get();
                } catch (ODKDatastoreException e)
                {
                    throw new ColumnDoesNotExistException(null,
                            rowEntry.getKey());
                }
                row.setValue(col.getAggregateIdentifier(), rowEntry.getValue());
            }
            row.updateRevisionTag();
            row.save();

            // Add row to this modification
            String aggregateRowIdentifier = row.getAggregateIdentifier();
            InternalModification modification = new InternalModification(
                    aggregateTableIdentifier, newModificationNumber,
                    aggregateRowIdentifier, cc);
            modification.save();

            // Create the row that will be sent back to the client
            SynchronizedRow updatedRow = new SynchronizedRow();
            updatedRow.setAggregateRowIdentifier(aggregateRowIdentifier);
            updatedRow.setRevisionTag(row.getRevisionTag());
            updatedRows.add(updatedRow);
        }
        Modification clientModification = new Modification(
                newModificationNumber, updatedRows);

        return clientModification;
    }
}