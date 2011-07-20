package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.command.synchronize.CloneSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.CloneSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
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
import org.opendatakit.common.web.CallingContext;

/**
 * CloneSynchronizedTableLogic encapsulates the logic necessary to validate and
 * execute a CloneSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CloneSynchronizedTableLogic extends
        CommandLogic<CloneSynchronizedTable>
{

    private final CloneSynchronizedTable cloneSynchronizedTable;

    public CloneSynchronizedTableLogic(
            CloneSynchronizedTable cloneSynchronizedTable)
    {
        this.cloneSynchronizedTable = cloneSynchronizedTable;
    }

    @Override
    public CloneSynchronizedTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableEntries entries = TableEntries.getInstance(cc);
        UserTableMappings mappings = UserTableMappings.getInstance(cc);
        Users users = Users.getInstance(cc);
        Columns columns = Columns.getInstance(cc);

        String tableID = cloneSynchronizedTable.getTableID();
        String aggregateTableIdentifier = cloneSynchronizedTable
                .getAggregateTableIdentifier();
        String requestingUserID = cloneSynchronizedTable.getRequestingUserID();

        InternalUser user = users.query()
                .equal(Users.USER_ID, requestingUserID).get();

        if (!user.hasPerm(users.getAggregateIdentifier(), Permissions.READ))
        {
            return CloneSynchronizedTableResult.failure(tableID,
                    aggregateTableIdentifier, FailureReason.PERMISSION_DENIED);
        }

        boolean mappingExists = mappings
                .query()
                .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                        user.getAggregateIdentifier())
                .equal(UserTableMappings.TABLE_ID, tableID).exists();
        if (mappingExists)
        {
            return CloneSynchronizedTableResult.failure(tableID,
                    aggregateTableIdentifier,
                    FailureReason.TABLE_ALREADY_EXISTS);
        }

        InternalTableEntry entry;
        try
        {
            entry = entries.get(aggregateTableIdentifier);
        } catch (ODKDatastoreException e)
        {
            return CloneSynchronizedTableResult.failure(tableID,
                    aggregateTableIdentifier,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        // add entry to user table mapping
        InternalUserTableMapping mapping = new InternalUserTableMapping(
                user.getAggregateIdentifier(), aggregateTableIdentifier,
                tableID, cc);
        mapping.save();

        // create modification of all latest rows
        Table table = Table.getInstance(aggregateTableIdentifier, cc);
        List<InternalColumn> cols = columns
                .query()
                .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                        aggregateTableIdentifier).execute();

        int modificationNumber = entry.getModificationNumber();

        List<InternalRow> rows = table.query().execute();
        List<SynchronizedRow> clientRows = new ArrayList<SynchronizedRow>();
        for (InternalRow row : rows)
        {
            SynchronizedRow clientRow = new SynchronizedRow();
            clientRow.setAggregateRowIdentifier(row.getAggregateIdentifier());
            clientRow.setRevisionTag(row.getRevisionTag());
            for (InternalColumn column : cols)
            {
                String value = row.getValue(column.getName());
                clientRow.setValue(column.getName(), value);
            }
            clientRows.add(clientRow);
        }

        Modification clientModification = new Modification(modificationNumber,
                clientRows);
        return CloneSynchronizedTableResult.success(clientModification);
    }
}
