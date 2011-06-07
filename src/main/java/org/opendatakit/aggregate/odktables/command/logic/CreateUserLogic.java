package org.opendatakit.aggregate.odktables.command.logic;

import org.opendatakit.aggregate.odktables.CommandLogic;
import org.opendatakit.aggregate.odktables.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.command.CreateUser;
import org.opendatakit.aggregate.odktables.command.result.CreateUserResult;
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
        String userId = createUser.getUserId();
        String convertedUserId = convertUserId(userId);
        Users users = Users.getInstance(cc);
        if (users.userExists(convertedUserId))
            return CreateUserResult.failure(userId,
                    FailureReason.USER_ALREADY_EXISTS);
        users.createUser(convertedUserId, createUser.getUserName());
        return CreateUserResult.success(userId);
    }

}
