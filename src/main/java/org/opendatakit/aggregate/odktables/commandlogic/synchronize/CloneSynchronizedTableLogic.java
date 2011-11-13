package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Filter;
import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.synchronize.CloneSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.CloneSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalFilter;
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
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityQuery;
import org.opendatakit.common.persistence.Query.FilterOperation;
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
            throws AggregateInternalErrorException
    {
        Modification clientModification;
        try
        {
            // Get relation instances
            TableEntries entries = TableEntries.getInstance(cc);
            UserTableMappings mappings = UserTableMappings.getInstance(cc);
            Users users = Users.getInstance(cc);
            Columns columns = Columns.getInstance(cc);

            // Get data from request
            String tableID = cloneSynchronizedTable.getTableID();
            String aggregateTableIdentifier = cloneSynchronizedTable
                    .getAggregateTableIdentifier();
            String requestingUserID = cloneSynchronizedTable
                    .getRequestingUserID();
            Collection<Filter> filters = cloneSynchronizedTable.getFilters();

            // Get request user
            InternalUser user = users
                    .query("CloneSynchronizedTableLogic.execute")
                    .equal(Users.USER_ID, requestingUserID).get();

            // Check if user is allowed to read the table they want to clone
            if (!user.hasPerm(aggregateTableIdentifier, Permissions.READ))
            {
                return CloneSynchronizedTableResult.failure(tableID,
                        FailureReason.PERMISSION_DENIED);
            }

            // Check if the user is already using the tableID
            boolean mappingExists = mappings
                    .query("CloneSynchronizedTableLogic.execute")
                    .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                            user.getAggregateIdentifier())
                    .equal(UserTableMappings.TABLE_ID, tableID).exists();
            if (mappingExists)
            {
                return CloneSynchronizedTableResult.failure(tableID,
                        FailureReason.TABLE_ALREADY_EXISTS);
            }

            // Retrieve the table entry for the table that is to be cloned
            InternalTableEntry entry;
            try
            {
                entry = entries.getEntity(aggregateTableIdentifier);
            } catch (ODKDatastoreException e)
            {
                return CloneSynchronizedTableResult.failure(tableID,
                        FailureReason.TABLE_DOES_NOT_EXIST);
            }

            // add entry to user table mapping
            InternalUserTableMapping mapping = new InternalUserTableMapping(
                    user.getAggregateIdentifier(), aggregateTableIdentifier,
                    tableID, cc);
            mapping.save();

            // save filters
            for (Filter filter : filters)
            {
                InternalFilter internalFilter = new InternalFilter(
                        user.getAggregateIdentifier(),
                        aggregateTableIdentifier, filter.getColumnName(),
                        FilterOperation.valueOf(filter.getOp().name()),
                        filter.getValue(), cc);
                internalFilter.save();
            }

            // create modification of the latest rows, applying any filters
            Table table = Table.getInstance(aggregateTableIdentifier, cc);
            List<InternalColumn> cols = columns
                    .query("CloneSynchronizedTableLogic.execute")
                    .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                            aggregateTableIdentifier).execute();

            int modificationNumber = entry.getModificationNumber();

            TypedEntityQuery<InternalRow> query = table
                    .query("CloneSynchronizedTableLogic.execute");
            for (Filter filter : filters)
            {
                InternalColumn col = InternalColumn.search(cols,
                        filter.getColumnName());
                String columnName = Table.convertIdentifier(col
                        .getAggregateIdentifier());
                Object value = CommandLogicFunctions.convert(table, columnName,
                        filter.getValue());
                query.addFilter(columnName,
                        FilterOperation.valueOf(filter.getOp().name()), value);
            }

            // convert rows to SynchronizedRow
            List<InternalRow> rows = query.execute();
            List<SynchronizedRow> clientRows = new ArrayList<SynchronizedRow>();
            for (InternalRow row : rows)
            {
                SynchronizedRow clientRow = new SynchronizedRow();
                clientRow.setAggregateRowIdentifier(row
                        .getAggregateIdentifier());
                clientRow.setRevisionTag(row.getRevisionTag());
                for (InternalColumn column : cols)
                {
                    String value = row
                            .getValue(column.getAggregateIdentifier());
                    clientRow.setValue(column.getName(), value);
                }
                clientRows.add(clientRow);
            }

            clientModification = new Modification(modificationNumber,
                    clientRows);
        } catch (ODKDatastoreException e)
        {
            throw new AggregateInternalErrorException(e.getMessage());
        }

        return CloneSynchronizedTableResult.success(clientModification);
    }
}
