package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.synchronize.RemoveTableSynchronization;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.RemoveTableSynchronizationResult;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * RemoveTableSynchronizationLogic encapsulates the logic necessary to validate
 * and execute a RemoveTableSynchronization command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class RemoveTableSynchronizationLogic extends
        CommandLogic<RemoveTableSynchronization>
{

    private final RemoveTableSynchronization removeTableSynchronization;

    public RemoveTableSynchronizationLogic(
            RemoveTableSynchronization removeTableSynchronization)
    {
        this.removeTableSynchronization = removeTableSynchronization;
    }

    @Override
    public RemoveTableSynchronizationResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        UserTableMappings mappings = UserTableMappings.getInstance(cc);

        String requestingUserID = removeTableSynchronization
                .getRequestingUserID();
        String tableID = removeTableSynchronization.getTableID();

        InternalUser requestUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        try
        {
            InternalUserTableMapping mapping = mappings
                    .query()
                    .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                            requestUser.getAggregateIdentifier())
                    .equal(UserTableMappings.TABLE_ID, tableID).get();
            mapping.delete();
        } catch (ODKDatastoreException e)
        {
            return RemoveTableSynchronizationResult.failure(tableID,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        return RemoveTableSynchronizationResult.success();
    }
}