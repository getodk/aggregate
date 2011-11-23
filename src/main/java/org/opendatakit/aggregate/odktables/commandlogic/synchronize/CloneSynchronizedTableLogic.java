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
import org.opendatakit.aggregate.odktables.commandresult.synchronize.CloneSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalFilter;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
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
        List<TypedEntity> entitiesToSave = new ArrayList<TypedEntity>();
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
                return CloneSynchronizedTableResult.failurePermissionDenied();
            }

            // Check if the user is already using the tableID
            boolean mappingExists = mappings
                    .query("CloneSynchronizedTableLogic.execute")
                    .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                            user.getAggregateIdentifier())
                    .equal(UserTableMappings.TABLE_ID, tableID).exists();
            if (mappingExists)
            {
                return CloneSynchronizedTableResult
                        .failureTableAlreadyExists(tableID);
            }

            // Retrieve the table entry for the table that is to be cloned
            InternalTableEntry entry;
            try
            {
                entry = entries.getEntity(aggregateTableIdentifier);
            } catch (ODKDatastoreException e)
            {
                return CloneSynchronizedTableResult
                        .failureTableDoesNotExist(tableID);
            }

            // add entry to user table mapping
            InternalUserTableMapping mapping = new InternalUserTableMapping(
                    user.getAggregateIdentifier(), aggregateTableIdentifier,
                    tableID, cc);
            entitiesToSave.add(mapping);

            // get columns of table
            List<InternalColumn> cols = columns
                    .query("CloneSynchronizedTableLogic.execute")
                    .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                            aggregateTableIdentifier).execute();

            // save filters
            for (Filter filter : filters)
            {
                InternalColumn col = InternalColumn.search(cols,
                        filter.getColumnName());
                if (col == null)
                {
                    return CloneSynchronizedTableResult
                            .failureColumnDoesNotExist(tableID,
                                    filter.getColumnName());
                }
                try
                {
                    CommandLogicFunctions.convert(col.getType(),
                            filter.getValue());
                } catch (Exception e)
                {
                    return CloneSynchronizedTableResult
                            .failureFilterValueTypeMismatch(tableID,
                                    col.getType(), filter.getValue());
                }
                InternalFilter internalFilter = new InternalFilter(
                        user.getAggregateIdentifier(),
                        aggregateTableIdentifier, filter.getColumnName(),
                        FilterOperation.valueOf(filter.getOp().name()),
                        filter.getValue(), cc);
                entitiesToSave.add(internalFilter);
            }

            // create modification of the latest rows, applying any filters
            Table table = Table.getInstance(aggregateTableIdentifier, cc);

            int modificationNumber = entry.getModificationNumber();

            TypedEntityQuery<InternalRow> query = table
                    .query("CloneSynchronizedTableLogic.execute");
            for (Filter filter : filters)
            {
                InternalColumn col = InternalColumn.search(cols,
                        filter.getColumnName());
                String columnName = Table.convertIdentifier(col
                        .getAggregateIdentifier());
                Object value = CommandLogicFunctions.convert(table
                        .getAttribute(columnName).getType(), filter.getValue());
                query.addFilter(columnName,
                        FilterOperation.valueOf(filter.getOp().name()), value);
            }

            // convert rows to SynchronizedRow
            List<InternalRow> rows = query.execute();
            List<SynchronizedRow> clientRows = CommandLogicFunctions.convert(
                    rows, cols);

            clientModification = new Modification(modificationNumber,
                    clientRows);
        } catch (ODKDatastoreException e)
        {
            throw new AggregateInternalErrorException(e.getMessage());
        }

        // save entities
        boolean success = CommandLogicFunctions.saveEntities(entitiesToSave);
        if (!success)
            throw new SnafuException("Could not save entities: "
                    + entitiesToSave);

        return CloneSynchronizedTableResult.success(clientModification);
    }
}
