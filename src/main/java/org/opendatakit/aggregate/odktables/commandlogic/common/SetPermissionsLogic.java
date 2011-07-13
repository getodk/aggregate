package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.command.common.SetPermissions;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.SetPermissionsResult;
import org.opendatakit.aggregate.odktables.entity.Permission;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SetPermissionsLogic encapsulates the logic necessary to validate and execute
 * a SetPermissions command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SetPermissionsLogic extends CommandLogic<SetPermissions>
{

    private final SetPermissions setPermissions;

    public SetPermissionsLogic(SetPermissions setPermissions)
    {
        this.setPermissions = setPermissions;
    }

    @Override
    public SetPermissionsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        TableEntries tableEntries = TableEntries.getInstance(cc);

        String userUUID = setPermissions.getUserUUID();
        String tableUUID = setPermissions.getTableUUID();
        boolean read = setPermissions.getRead();
        boolean write = setPermissions.getWrite();
        boolean delete = setPermissions.getDelete();
        String requestingUserID = setPermissions.getRequestingUserID();

        User requestUser = users.query().equal(Users.USER_ID, requestingUserID)
                .get();
        if (!requestUser.hasPerm(tableUUID, Permissions.WRITE))
        {
            return SetPermissionsResult.failure(tableUUID, userUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        try
        {
            users.get(userUUID);
        } catch (ODKDatastoreException e)
        {
            return SetPermissionsResult.failure(tableUUID, userUUID,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        try
        {
            tableEntries.get(tableUUID);
        } catch (ODKDatastoreException e)
        {
            return SetPermissionsResult.failure(tableUUID, userUUID,
                    FailureReason.TABLE_DOES_NOT_EXIST);
        }

        Permission perm = new Permission(tableUUID, userUUID, read, write,
                delete, cc);
        perm.save();
        // TODO: try-catch error handling for above?

        return SetPermissionsResult.success();
    }
}