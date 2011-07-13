package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.command.common.CreateUser;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.CreateUserResult;
import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * CreateUserLogic encapsulates the logic necessary to validate and execute a
 * CreateUser command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CreateUserLogic extends CommandLogic<CreateUser>
{

    private final CreateUser createUser;

    public CreateUserLogic(CreateUser createUser)
    {
        this.createUser = createUser;
    }

    @Override
    public CreateUserResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        Users users = Users.getInstance(cc);

        String userID = createUser.getUserID();
        String requestingUserID = createUser.getRequestingUserID();
        String userTableUUID = users.getUUID();

        User requestUser = users.query().equal(Users.USER_ID, requestingUserID)
                .get();

        if (!requestUser.hasPerm(userTableUUID, Permissions.WRITE))
        {
            return CreateUserResult.failure(userID,
                    FailureReason.PERMISSION_DENIED);
        }
        if (users.query().equal(Users.USER_ID, userID).exists())
        {
            return CreateUserResult.failure(userID,
                    FailureReason.USER_ALREADY_EXISTS);
        }
        User newUser = new User(userID, createUser.getUserName(), cc);
        newUser.save();
        
        org.opendatakit.aggregate.odktables.client.entity.User user = 
                new org.opendatakit.aggregate.odktables.client.entity.User(
                newUser.getID(), newUser.getUUID(), newUser.getName());
        
        return CreateUserResult.success(user);
    }
}
