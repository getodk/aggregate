package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.List;

import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.DeleteTableResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
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
 * DeleteTableLogic encapsulates the logic necessary to validate and execute a
 * DeleteTable Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteTableLogic extends CommandLogic<DeleteTable>
{

    private final DeleteTable deleteTable;

    public DeleteTableLogic(DeleteTable deleteTable)
    {
        this.deleteTable = deleteTable;
    }

    @Override
    public DeleteTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableEntries tables = TableEntries.getInstance(cc);
        Users users = Users.getInstance(cc);
        UserTableMappings mappings = UserTableMappings.getInstance(cc);
        Columns columns = Columns.getInstance(cc);
        Permissions permissions = Permissions.getInstance(cc);

        String tableID = this.deleteTable.getTableID();
        String requestingUserID = this.deleteTable.getRequestingUserID();

        InternalUser requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();

        String aggregateRequestingUserIdentifier = requestingUser
                .getAggregateIdentifier();

        InternalUserTableMapping mapping; 
        try
        {
            mapping = mappings
                    .query()
                    .equal(UserTableMappings.TABLE_ID, tableID)
                    .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                            aggregateRequestingUserIdentifier).get();
        }
        catch (ODKDatastoreException e)
        {
            return DeleteTableResult.failure(null,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        String aggregateTableIdentifier = mapping.getAggregateTableIdentifier();

        if (!requestingUser.hasPerm(aggregateTableIdentifier,
                Permissions.DELETE))
        {
            return DeleteTableResult.failure(aggregateTableIdentifier,
                    FailureReason.PERMISSION_DENIED);
        }

        String aggregateUserIdentifier = requestingUser
                .getAggregateIdentifier();

        Table table = Table.getInstance(aggregateTableIdentifier, cc);
        InternalUserTableMapping cursor = mappings
                .query()
                .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                        aggregateUserIdentifier)
                .equal(UserTableMappings.AGGREGATE_TABLE_IDENTIFIER,
                        aggregateTableIdentifier).get();

        InternalTableEntry entry = tables.getEntity(aggregateTableIdentifier);
        List<InternalColumn> tableColumns = columns
                .query()
                .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
                        aggregateTableIdentifier).execute();

        List<InternalPermission> perms = permissions
                .query()
                .equal(Permissions.AGGREGATE_TABLE_IDENTIFIER,
                        aggregateTableIdentifier).execute();

        try
        {
            table.dropRelation();
            cursor.delete();
            entry.delete();
            for (InternalColumn column : tableColumns)
                column.delete();
            for (InternalPermission perm : perms)
                perm.delete();
        } catch (ODKDatastoreException e)
        {
            // TODO: try delete again
        }
        
        return DeleteTableResult.success();
    }
}
