package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.command.common.GetUserByUUID;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.GetUserByUUIDResult;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * GetUserByUUIDLogic encapsulates the logic necessary to validate and execute a
 * GetUserByUUID command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class GetUserByUUIDLogic extends CommandLogic<GetUserByUUID>
{

    private final GetUserByUUID getUserByUUID;

    public GetUserByUUIDLogic(GetUserByUUID getUserByUUID)
    {
        this.getUserByUUID = getUserByUUID;
    }

    @Override
    public GetUserByUUIDResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);

        String userUUID = this.getUserByUUID.getUserUUID();
        String requestingUserID = this.getUserByUUID.getRequestingUserID();
        String userTableUUID = users.getUUID();

        User requestUser = users.query().equal(Users.USER_ID, requestingUserID)
                .get();
        if (!requestUser.hasPerm(userTableUUID, Permissions.READ))
        {
            return GetUserByUUIDResult.failure(userUUID,
                    FailureReason.PERMISSION_DENIED);
        }

        User user = null;
        try
        {
            user = users.get(userUUID);
        } catch (ODKDatastoreException e)
        {
            return GetUserByUUIDResult.failure(userUUID,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        org.opendatakit.aggregate.odktables.client.entity.User retrievedUser = new org.opendatakit.aggregate.odktables.client.entity.User(
                user.getID(), user.getUUID(), user.getName());

        return GetUserByUUIDResult.success(retrievedUser);
    }
}