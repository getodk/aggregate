package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.command.common.CreateUser;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A CreateUserResult represents the result of executing a CreateUser command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateUserResult extends CommandResult<CreateUser>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_ALREADY_EXISTS);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final User user;
    private final String userID;

    private CreateUserResult()
    {
        super(true, null);
        this.user = null;
        this.userID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private CreateUserResult(User user)
    {
        super(true, null);
        Check.notNull(user, "user");
        this.user = user;
        this.userID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private CreateUserResult(String userID, FailureReason reason)
    {
        super(false, reason);
        Check.notNullOrEmpty(userID, "userID");
        this.user = null;
        this.userID = userID;
    }

    /**
     * Retrieve the results from the CreateUser command.
     * 
     * @return the successfully created User
     * @throws UserAlreadyExistsException
     *             if the user with the aggregateUserIdentifier given to the CreateUser command
     *             already existed.
     * @throws PermissionDeniedException
     *             if the request user who made the call did not have write
     *             permission on the Users table.
     */
    public User getCreatedUser() throws UserAlreadyExistsException,
            PermissionDeniedException
    {
        if (successful())
        {
            return this.user;
        } else
        {
            switch (getReason())
            {
            case USER_ALREADY_EXISTS:
                throw new UserAlreadyExistsException(this.userID);
            case PERMISSION_DENIED:
                throw new PermissionDeniedException();
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("CreateUserResult [user=%s, userID=%s]", user,
                userID);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        result = prime * result + ((userID == null) ? 0 : userID.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof CreateUserResult))
            return false;
        CreateUserResult other = (CreateUserResult) obj;
        if (user == null)
        {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        if (userID == null)
        {
            if (other.userID != null)
                return false;
        } else if (!userID.equals(other.userID))
            return false;
        return true;
    }

    /**
     * @param user
     *            the user who was successfully created.
     * @return a new CreateUserResult representing the successful creation of a
     *         new user.
     */
    public static CreateUserResult success(User user)
    {
        return new CreateUserResult(user);
    }

    /**
     * @param user
     *            the userID of the user who failed to be created
     * @param reason
     *            the reason that the user could not be created. This can be
     *            either USER_ALREADY_EXISTS or PERMISSION_DENIED.
     * @return a new CreateUserResult representing the failed creation of a new
     *         user.
     */
    public static CreateUserResult failure(String userID, FailureReason reason)
    {
        return new CreateUserResult(userID, reason);
    }
}
