package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.synchronize.DeleteSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.DeleteSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalModification;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Modifications;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * DeleteSynchronizedTableLogic encapsulates the logic necessary to validate and
 * execute a DeleteSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class DeleteSynchronizedTableLogic extends
        CommandLogic<DeleteSynchronizedTable>
{

    private final DeleteSynchronizedTable deleteSynchronizedTable;

    public DeleteSynchronizedTableLogic(
            DeleteSynchronizedTable deleteSynchronizedTable)
    {
        this.deleteSynchronizedTable = deleteSynchronizedTable;
    }

    @Override
    public DeleteSynchronizedTableResult execute(CallingContext cc)
            throws AggregateInternalErrorException
    {
        Table table;
        List<TypedEntity> entitiesToDelete = new ArrayList<TypedEntity>();
        try
        {
            // get relation instances
            Users users = Users.getInstance(cc);
            UserTableMappings mappings = UserTableMappings.getInstance(cc);
            TableEntries entries = TableEntries.getInstance(cc);
            Columns columns = Columns.getInstance(cc);
            Permissions permissions = Permissions.getInstance(cc);
            Modifications modifications = Modifications.getInstance(cc);

            // get request data
            String requestingUserID = deleteSynchronizedTable
                    .getRequestingUserID();
            String tableID = deleteSynchronizedTable.getTableID();

            // retrieve request user
            InternalUser requestUser = users.query()
                    .equal(Users.USER_ID, requestingUserID).get();

            // retrieve mapping from user's tableID to aggregateTableIdentifier
            InternalUserTableMapping mapping;
            try
            {
                mapping = mappings
                        .query()
                        .equal(UserTableMappings.TABLE_ID, tableID)
                        .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                                requestUser.getAggregateIdentifier()).get();
            } catch (ODKDatastoreException e)
            {
                return DeleteSynchronizedTableResult.failure(null,
                        FailureReason.TABLE_DOES_NOT_EXIST);
            }

            // in order to delete the table the user must have delete permission on the table
            String aggregateTableIdentifier = mapping
                    .getAggregateTableIdentifier();
            if (!requestUser.hasPerm(aggregateTableIdentifier,
                    Permissions.DELETE))
            {
                return DeleteSynchronizedTableResult.failure(
                        mapping.getTableID(), FailureReason.PERMISSION_DENIED);
            }

            // retrieve all entities that make up a table
            table = Table.getInstance(aggregateTableIdentifier, cc);
            List<InternalRow> rows = table.query().execute();
            List<InternalColumn> cols = columns
                    .query()
                    .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                            aggregateTableIdentifier).execute();
            List<InternalUserTableMapping> maps = mappings
                    .query()
                    .equal(UserTableMappings.AGGREGATE_TABLE_IDENTIFIER,
                            aggregateTableIdentifier).execute();
            List<InternalPermission> perms = permissions
                    .query()
                    .equal(Permissions.AGGREGATE_TABLE_IDENTIFIER,
                            aggregateTableIdentifier).execute();
            List<InternalModification> mods = modifications
                    .query()
                    .equal(Modifications.AGGREGATE_TABLE_IDENTIFIER,
                            aggregateTableIdentifier).execute();
            InternalTableEntry entry = entries
                    .getEntity(aggregateTableIdentifier);

            entitiesToDelete.addAll(rows);
            entitiesToDelete.addAll(cols);
            entitiesToDelete.addAll(maps);
            entitiesToDelete.addAll(perms);
            entitiesToDelete.addAll(mods);
            entitiesToDelete.add(entry);
        } catch (ODKDatastoreException e)
        {
            throw new AggregateInternalErrorException(e.getMessage());
        }

        // delete all entities
        boolean success = CommandLogicFunctions
                .deleteEntities(entitiesToDelete);
        if (!success)
            throw new SnafuException("Could not delete entities: "
                    + entitiesToDelete);

        return DeleteSynchronizedTableResult.success();
    }
}