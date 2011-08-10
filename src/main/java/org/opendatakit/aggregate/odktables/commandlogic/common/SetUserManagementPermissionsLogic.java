package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.command.common.SetUserManagementPermissions;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.SetUserManagementPermissionsResult;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SetUsersPermissionsLogic encapsulates the logic necessary to validate and
 * execute a SetUsersPermissions command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SetUserManagementPermissionsLogic extends
        CommandLogic<SetUserManagementPermissions>
{

    private final SetUserManagementPermissions setUsersPermissions;

    public SetUserManagementPermissionsLogic(
            SetUserManagementPermissions setUsersPermissions)
    {
        this.setUsersPermissions = setUsersPermissions;
    }

    @Override
    public SetUserManagementPermissionsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        // get relation instances
        Users users = Users.getInstance(cc);
        Permissions permissions = Permissions.getInstance(cc);
        TableEntries entries = TableEntries.getInstance(cc);

        // get request data
        String requestingUserID = this.setUsersPermissions
                .getRequestingUserID();
        String aggregateUserIdentifier = this.setUsersPermissions
                .getAggregateUserIdentifier();

        // retrieve request user
        InternalUser requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        
        // To set user management permissions the user must have write permission on the users table
        if (!requestingUser.hasPerm(users.getAggregateIdentifier(),
                Permissions.WRITE))
        {
            return SetUserManagementPermissionsResult.failure(
                    aggregateUserIdentifier, FailureReason.PERMISSION_DENIED);
        }

        // check if the user we are setting permissions for exists
        try
        {
            users.getEntity(aggregateUserIdentifier);
        } catch (ODKDatastoreException e)
        {
            return SetUserManagementPermissionsResult.failure(
                    aggregateUserIdentifier, FailureReason.USER_DOES_NOT_EXIST);
        }
        
        // see if permission exists, if not create it
        InternalPermission perm;
        try
        {
            perm = permissions
                    .query()
                    .equal(Permissions.AGGREGATE_TABLE_IDENTIFIER,
                            users.getAggregateIdentifier())
                    .equal(Permissions.AGGREGATE_USER_IDENTIFIER,
                            aggregateUserIdentifier).get();
            perm.setRead(setUsersPermissions.getAllowed());
            perm.setWrite(setUsersPermissions.getAllowed());
            perm.setDelete(setUsersPermissions.getAllowed());
        } catch (ODKDatastoreException e)
        {
            perm = new InternalPermission(users.getAggregateIdentifier(),
                    aggregateUserIdentifier, setUsersPermissions.getAllowed(),
                    setUsersPermissions.getAllowed(),
                    setUsersPermissions.getAllowed(), cc);
        }
        perm.save();

        return SetUserManagementPermissionsResult.success();
    }
}