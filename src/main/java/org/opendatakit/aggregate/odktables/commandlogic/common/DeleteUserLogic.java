package org.opendatakit.aggregate.odktables.commandlogic.common;

import java.util.List;

import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.DeleteUserResult;
import org.opendatakit.aggregate.odktables.entity.Permission;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * DeleteUserLogic encapsulates the logic necessary to validate and execute a
 * DeleteUser Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteUserLogic extends CommandLogic<DeleteUser>
{

    private DeleteUser deleteUser;

    public DeleteUserLogic(DeleteUser deleteUser)
    {
        this.deleteUser = deleteUser;
    }

    @Override
    public DeleteUserResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);
        TableEntries tables = TableEntries.getInstance(cc);
        Permissions permissions = Permissions.getInstance(cc);

        String userUUID = this.deleteUser.getUserUUID();
        String requestingUserID = this.deleteUser.getRequestingUserID();
        String userTableUUID = users.getUUID();

        User requestingUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        if (!requestingUser.hasPerm(userTableUUID, Permissions.DELETE))
        {
            return DeleteUserResult.failure(userUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        User user = null;
        try
        {
            user = users.get(userUUID);
        } catch (ODKDatastoreException e)
        {
            // user does not exist
            return DeleteUserResult.failure(userUUID,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        if (tables.query().equal(TableEntries.OWNER_UUID, userUUID).exists())
        {
            // user still has some tables
            return DeleteUserResult.failure(userUUID,
                    FailureReason.CANNOT_DELETE);
        }

        try
        {
            List<Permission> perms = permissions.query()
                    .equal(Permissions.USER_UUID, userUUID).execute();
            for (Permission perm : perms)
            {
                perm.delete();
            }
            user.delete();
        } catch (ODKDatastoreException e)
        {
            // TODO: retry delete?
        }

        return DeleteUserResult.success(userUUID);
    }
}
