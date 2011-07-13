package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.command.common.SetUsersPermissions;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.SetUsersPermissionsResult;
import org.opendatakit.aggregate.odktables.entity.Permission;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
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
public class SetUsersPermissionsLogic extends CommandLogic<SetUsersPermissions>
{

    private final SetUsersPermissions setUsersPermissions;

    public SetUsersPermissionsLogic(SetUsersPermissions setUsersPermissions)
    {
        this.setUsersPermissions = setUsersPermissions;
    }

    @Override
    public SetUsersPermissionsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);

        String requestingUserID = this.setUsersPermissions
                .getRequestingUserID();
        String userUUID = this.setUsersPermissions.getUserUUID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        if (!requestingUser.hasPerm(users.getUUID(), Permissions.WRITE))
        {
            return SetUsersPermissionsResult.failure(userUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        try
        {
            users.get(userUUID);
        } catch (ODKDatastoreException e)
        {
            return SetUsersPermissionsResult.failure(userUUID,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        Permission perm = new Permission(users.getUUID(), userUUID,
                setUsersPermissions.getRead(), setUsersPermissions.getWrite(),
                setUsersPermissions.getDelete(), cc);
        perm.save();
        
        return SetUsersPermissionsResult.success();
    }
}