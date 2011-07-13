package org.opendatakit.aggregate.odktables.commandlogic.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.command.common.GetTablePermissions;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.GetTablePermissionsResult;
import org.opendatakit.aggregate.odktables.entity.Permission;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * GetTablePermissonsLogic encapsulates the logic necessary to validate and
 * execute a GetTablePermissons command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class GetTablePermissionsLogic extends CommandLogic<GetTablePermissions>
{

    private final GetTablePermissions getTablePermissions;

    public GetTablePermissionsLogic(GetTablePermissions getTablePermissions)
    {
        this.getTablePermissions = getTablePermissions;
    }

    @Override
    public GetTablePermissionsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        Permissions permissions = Permissions.getInstance(cc);
        TableEntries entries = TableEntries.getInstance(cc);

        String requestingUserID = getTablePermissions.getRequestingUserID();
        String tableUUID = getTablePermissions.getTableUUID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();

        if (!requestingUser.hasPerm(permissions.getUUID(), Permissions.READ))
        {
            return GetTablePermissionsResult.failure(tableUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        try
        {
            entries.get(tableUUID);
        } catch (ODKDatastoreException e)
        {
            return GetTablePermissionsResult.failure(tableUUID,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        List<Permission> perms = permissions.query()
                .equal(Permissions.TABLE_UUID, tableUUID).execute();
        List<org.opendatakit.aggregate.odktables.client.entity.Permission> clientPerms = new ArrayList<org.opendatakit.aggregate.odktables.client.entity.Permission>();
        for (Permission perm : perms)
        {
            org.opendatakit.aggregate.odktables.client.entity.Permission clientPerm = new org.opendatakit.aggregate.odktables.client.entity.Permission(
                    perm.getUserUUID(), perm.getTableUUID(), perm.getRead(),
                    perm.getWrite(), perm.getDelete());
            clientPerms.add(clientPerm);
        }

        return GetTablePermissionsResult.success(clientPerms);
    }
}