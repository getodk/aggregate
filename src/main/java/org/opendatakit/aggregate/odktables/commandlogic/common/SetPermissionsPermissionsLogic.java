package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.command.common.SetPermissionsPermissions;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.SetPermissionsPermissionsResult;
import org.opendatakit.aggregate.odktables.entity.Permission;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SetPermissionsPermissionsLogic encapsulates the logic necessary to validate
 * and execute a SetPermissionsPermissions command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SetPermissionsPermissionsLogic extends
        CommandLogic<SetPermissionsPermissions>
{

    private final SetPermissionsPermissions setPermissionsPermissions;

    public SetPermissionsPermissionsLogic(
            SetPermissionsPermissions setPermissionsPermissions)
    {
        this.setPermissionsPermissions = setPermissionsPermissions;
    }

    @Override
    public SetPermissionsPermissionsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        Permissions permissions = Permissions.getInstance(cc);

        String requestingUserID = setPermissionsPermissions
                .getRequestingUserID();
        String userUUID = setPermissionsPermissions.getUserUUID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();

        if (!requestingUser.hasPerm(permissions.getUUID(), Permissions.WRITE))
        {
            return SetPermissionsPermissionsResult.failure(
                    requestingUser.getUUID(), FailureReason.PERMISSION_DENIED);
        }

        try
        {
            users.get(userUUID);
        } catch (ODKDatastoreException e)
        {
            return SetPermissionsPermissionsResult.failure(userUUID,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        Permission perm = new Permission(permissions.getUUID(), userUUID,
                setPermissionsPermissions.getRead(),
                setPermissionsPermissions.getWrite(), false, cc);
        perm.save();

        return SetPermissionsPermissionsResult.success();
    }
}