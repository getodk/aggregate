package org.opendatakit.aggregate.odktables.command.result;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.CommandResult;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.command.CreateUser;

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
    }

    private final String userId;

    private CreateUserResult()
    {
        super(true, null);
        this.userId = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private CreateUserResult(String userId)
    {
        super(true, null);
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");
        this.userId = userId;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private CreateUserResult(String userId, FailureReason reason)
    {
        super(false, reason);
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");
        this.userId = userId;
    }

    /**
     * Retrieve the results from the CreateUser command.
     * 
     * @return the userId of the successfully created user.
     * @throws UserAlreadyExistsException
     *             if the user with the userId given to the CreateUser command
     *             already existed.
     */
    public String getCreatedUserId() throws UserAlreadyExistsException
    {
        if (successful())
        {
            return this.userId;
        } else
        {
            switch (getReason())
            {
            case USER_ALREADY_EXISTS:
                throw new UserAlreadyExistsException(this.userId);
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
        return String.format("CreateUserResult [userId=%s]", userId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
        if (userId == null)
        {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }

    /**
     * @param userId
     *            the userId of the user who was successfully created.
     * @return a new CreateUserResult representing the successful creation of a
     *         new user.
     */
    public static CreateUserResult success(String userId)
    {
        return new CreateUserResult(userId);
    }

    /**
     * @param userId
     *            the userId of the user who failed to be created.
     * @param reason
     *            the reason that the user could not be created. Currently, this
     *            can only be USER_ALREADY_EXISTS.
     * @return a new CreateUserResult representing the failed creation of a new
     *         user.
     */
    public static CreateUserResult failure(String userId, FailureReason reason)
    {
        return new CreateUserResult(userId, reason);
    }
}
