package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.command.synchronize.Synchronize;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.SynchronizeResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalModification;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Modifications;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SynchronizeLogic encapsulates the logic necessary to validate and execute a
 * Synchronize command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SynchronizeLogic extends CommandLogic<Synchronize>
{

    private final Synchronize synchronize;

    public SynchronizeLogic(Synchronize synchronize)
    {
        this.synchronize = synchronize;
    }

    @Override
    public SynchronizeResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        UserTableMappings mappings = UserTableMappings.getInstance(cc);
        TableEntries entries = TableEntries.getInstance(cc);
        Modifications modifications = Modifications.getInstance(cc);
        Columns columns = Columns.getInstance(cc);

        String requestingUserID = synchronize.getRequestingUserID();
        String tableID = synchronize.getTableID();
        int clientModificationNumber = synchronize.getModificationNumber();

        InternalUser requestUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
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
            return SynchronizeResult.failure(tableID,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        String aggregateTableIdentifier = mapping.getAggregateTableIdentifier();

        if (!requestUser.hasPerm(aggregateTableIdentifier, Permissions.READ))
        {
            return SynchronizeResult.failure(tableID,
                    FailureReason.PERMISSION_DENIED);
        }

        // Get current modification number 
        InternalTableEntry entry = entries.get(aggregateTableIdentifier);
        int currentModificationNumber = entry.getModificationNumber();

        // Get latest modifications
        List<InternalModification> latestModifications = modifications
                .query()
                .equal(Modifications.AGGREGATE_TABLE_IDENTIFIER,
                        aggregateTableIdentifier)
                .greaterThan(Modifications.MODIFICATION_NUMBER,
                        clientModificationNumber).execute();
        List<String> aggregateRowIdentifiers = new ArrayList<String>();

        // Get rows that need to be updated
        for (InternalModification mod : latestModifications)
        {
            aggregateRowIdentifiers.add(mod.getAggregateRowIdentifier());
        }
        Table table = Table.getInstance(aggregateTableIdentifier, cc);
        List<InternalColumn> cols = columns
                .query()
                .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                        aggregateTableIdentifier).execute();

        // Convert rows to SynchronizedRow
        List<SynchronizedRow> latestRows = new ArrayList<SynchronizedRow>();
        for (String aggregateRowIdentifier : aggregateRowIdentifiers)
        {
            InternalRow row = table.get(aggregateRowIdentifier);
            SynchronizedRow latestRow = new SynchronizedRow();
            latestRow.setAggregateRowIdentifier(aggregateRowIdentifier);
            latestRow.setRevisionTag(row.getRevisionTag());
            for (InternalColumn col : cols)
            {
                String name = col.getName();
                String value = row.getValue(name);
                latestRow.setValue(name, value);
            }
            latestRows.add(latestRow);
        }

        Modification modification = new Modification(currentModificationNumber,
                latestRows);

        return SynchronizeResult.success(modification);
    }
}