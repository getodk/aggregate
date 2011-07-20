package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.command.common.GetUserByID;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.GetUserByIDResult;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * GetUserByIDLogic encapsulates the logic necessary to validate and execute a
 * GetUserByID command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class GetUserByIDLogic extends CommandLogic<GetUserByID>
{

    private final GetUserByID getUserByID;

    public GetUserByIDLogic(GetUserByID getUserByID)
    {
        this.getUserByID = getUserByID;
    }

    @Override
    public GetUserByIDResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);

        String userID = this.getUserByID.getUserID();
        String requestingUserID = this.getUserByID.getRequestingUserID();
        String usersTable = users.getAggregateIdentifier();

        InternalUser requestUser = users.query()
                .equal(Users.USER_ID, requestingUserID).get();
        if (!requestUser.hasPerm(usersTable, Permissions.READ))
        {
            return GetUserByIDResult.failure(
                    requestUser.getAggregateIdentifier(),
                    FailureReason.PERMISSION_DENIED);
        }

        InternalUser user = null;
        try
        {
            user = users.query().equal(Users.USER_ID, userID).get();
        } catch (ODKDatastoreException e)
        {
            return GetUserByIDResult.failure(userID,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        User retrievedUser = new User(userID, user.getAggregateIdentifier(),
                user.getName());

        return GetUserByIDResult.success(retrievedUser);
    }
}