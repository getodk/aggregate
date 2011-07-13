package org.opendatakit.aggregate.odktables.commandlogic.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.command.common.GetUserPermissions;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.GetUserPermissionsResult;
import org.opendatakit.aggregate.odktables.entity.Permission;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * GetUserPermissionsLogic encapsulates the logic necessary to validate and
 * execute a GetUserPermissions command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class GetUserPermissionsLogic extends CommandLogic<GetUserPermissions>
{

    private final GetUserPermissions getUserPermissions;

    public GetUserPermissionsLogic(GetUserPermissions getUserPermissions)
    {
        this.getUserPermissions = getUserPermissions;
    }

    @Override
    public GetUserPermissionsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        Permissions permissions = Permissions.getInstance(cc);

        String requestingUserID = getUserPermissions.getRequestingUserID();
        String userUUID = getUserPermissions.getUserUUID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        if (!requestingUser.hasPerm(permissions.getUUID(), Permissions.READ))
        {
            return GetUserPermissionsResult.failure(userUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        try
        {
            users.get(userUUID);
        } catch (ODKDatastoreException e)
        {
            return GetUserPermissionsResult.failure(userUUID,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        List<Permission> perms = permissions.query()
                .equal(Permissions.USER_UUID, userUUID).execute();
        List<org.opendatakit.aggregate.odktables.client.entity.Permission> clientPerms = new ArrayList<org.opendatakit.aggregate.odktables.client.entity.Permission>();

        for (Permission perm : perms)
        {
            org.opendatakit.aggregate.odktables.client.entity.Permission clientPerm = new org.opendatakit.aggregate.odktables.client.entity.Permission(
                    perm.getUserUUID(), perm.getTableUUID(), perm.getRead(),
                    perm.getWrite(), perm.getDelete());
            clientPerms.add(clientPerm);
        }

        return GetUserPermissionsResult.success(clientPerms);
    }
}