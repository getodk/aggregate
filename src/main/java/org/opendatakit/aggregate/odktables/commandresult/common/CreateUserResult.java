package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
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

    private final String userID;

    private CreateUserResult()
    {
        super(true, null);
        this.userID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private CreateUserResult(String userID)
    {
        super(true, null);

        Check.notNullOrEmpty(userID, "userID");

        this.userID = userID;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private CreateUserResult(String userID, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(userID, "userID");

        this.userID = userID;
    }

    /**
     * Retrieve the results from the CreateUser command.
     * 
     * @return the userID of the successfully created user.
     * @throws UserAlreadyExistsException
     *             if the user with the userID given to the CreateUser command
     *             already existed.
     * @throws PermissionDeniedException
     *             if the request user who made the call did not have write
     *             permission on the Users table.
     * @throws AggregateInternalErrorException
     *             if Aggregate encountered an internal error that caused the
     *             call to fail
     */
    public String getCreatedUserID() throws UserAlreadyExistsException,
            PermissionDeniedException, AggregateInternalErrorException
    {
        if (successful())
        {
            return this.userID;
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
        return String.format("CreateUserResult [userID=%s]", userID);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
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
        if (userID == null)
        {
            if (other.userID != null)
                return false;
        } else if (!userID.equals(other.userID))
            return false;
        return true;
    }

    /**
     * @param userID
     *            the userID of the user who was successfully created.
     * @return a new CreateUserResult representing the successful creation of a
     *         new user.
     */
    public static CreateUserResult success(String userID)
    {
        return new CreateUserResult(userID);
    }

    /**
     * @param userID
     *            the userID of the user who failed to be created.
     * @param reason
     *            the reason that the user could not be created. Currently, this
     *            can only be USER_ALREADY_EXISTS.
     * @return a new CreateUserResult representing the failed creation of a new
     *         user.
     */
    public static CreateUserResult failure(String userID, FailureReason reason)
    {
        return new CreateUserResult(userID, reason);
    }
}
