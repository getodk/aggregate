package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.common.SetTablePermissions;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.SetTablePermissionsResult;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SetTablePermissionsLogic encapsulates the logic necessary to validate and
 * execute a SetTablePermissions command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SetTablePermissionsLogic extends CommandLogic<SetTablePermissions>
{

    private final SetTablePermissions setTablePermissions;

    public SetTablePermissionsLogic(SetTablePermissions setTablePermissions)
    {
        this.setTablePermissions = setTablePermissions;
    }

    @Override
    public SetTablePermissionsResult execute(CallingContext cc)
            throws AggregateInternalErrorException
    {
        try
        {
            // get relation instances
            Users users = Users.getInstance(cc);
            TableEntries tableEntries = TableEntries.getInstance(cc);
            Permissions permissions = Permissions.getInstance(cc);
            UserTableMappings mappings = UserTableMappings.getInstance(cc);

            // get request data
            String aggregateUserIdentifier = setTablePermissions
                    .getAggregateUserIdentifier();
            String tableID = setTablePermissions.getTableID();
            boolean read = setTablePermissions.getRead();
            boolean write = setTablePermissions.getWrite();
            boolean delete = setTablePermissions.getDelete();
            String requestingUserID = setTablePermissions.getRequestingUserID();

            // retrieve request user
            InternalUser requestUser = users.query("SetTablePermissionsLogic.execute")
                    .equal(Users.USER_ID, requestingUserID).get();

            // retrieve table's aggregateTableIdentifier
            String aggregateTableIdentifier;
            try
            {
                InternalUserTableMapping mapping = mappings
                        .query("SetTablePermissionsLogic.execute")
                        .equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
                                requestUser.getAggregateIdentifier())
                        .equal(UserTableMappings.TABLE_ID, tableID).get();
                aggregateTableIdentifier = mapping
                        .getAggregateTableIdentifier();
            } catch (ODKDatastoreException e)
            {
                return SetTablePermissionsResult.failure(tableID,
                        aggregateUserIdentifier,
                        FailureReason.TABLE_DOES_NOT_EXIST);
            }

            if (!requestUser.hasPerm(aggregateTableIdentifier,
                    Permissions.WRITE))
            {
                return SetTablePermissionsResult.failure(null,
                        aggregateUserIdentifier,
                        FailureReason.PERMISSION_DENIED);
            }

            try
            {
                users.getEntity(aggregateUserIdentifier);
            } catch (ODKDatastoreException e)
            {
                return SetTablePermissionsResult.failure(
                        aggregateTableIdentifier, aggregateUserIdentifier,
                        FailureReason.USER_DOES_NOT_EXIST);
            }

            try
            {
                tableEntries.getEntity(aggregateTableIdentifier);
            } catch (ODKDatastoreException e)
            {
                return SetTablePermissionsResult.failure(
                        aggregateTableIdentifier, aggregateUserIdentifier,
                        FailureReason.TABLE_DOES_NOT_EXIST);
            }

            // see if permission exists, if not create it
            InternalPermission perm;
            try
            {
                perm = permissions
                        .query("SetTablePermissionsLogic.execute")
                        .equal(Permissions.AGGREGATE_TABLE_IDENTIFIER,
                                aggregateTableIdentifier)
                        .equal(Permissions.AGGREGATE_USER_IDENTIFIER,
                                aggregateUserIdentifier).get();
                perm.setRead(read);
                perm.setWrite(write);
                perm.setDelete(delete);
            } catch (ODKDatastoreException e)
            {
                perm = new InternalPermission(aggregateTableIdentifier,
                        aggregateUserIdentifier, read, write, delete, cc);
            }
            perm.save();
        } catch (ODKDatastoreException e)
        {
            throw new AggregateInternalErrorException(e.getMessage());
        }

        return SetTablePermissionsResult.success();
    }
}