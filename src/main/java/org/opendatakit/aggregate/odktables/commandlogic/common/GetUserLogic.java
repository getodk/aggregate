package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.command.common.GetUser;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.GetUserResult;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * GetUserLogic encapsulates the logic necessary to validate and execute a
 * GetUserCommand.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetUserLogic extends CommandLogic<GetUser>
{

    private GetUser getUser;

    public GetUserLogic(GetUser getUser)
    {
        this.getUser = getUser;
    }

    @Override
    public GetUserResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);

        String userId = this.getUser.getUserId();

        User user = null;
        try
        {
            user = users.query().equal(Users.USER_ID, userId).get();
        } catch (ODKDatastoreException e)
        {
            return GetUserResult.failure(userId,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        return GetUserResult.success(userId, user.getUri(), user.getName());
    }
}
