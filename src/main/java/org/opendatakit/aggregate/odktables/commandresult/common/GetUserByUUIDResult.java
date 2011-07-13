package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.GetUserByUUID;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A GetUserByUUIDResult represents the result of executing a GetUserByUUID
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetUserByUUIDResult extends CommandResult<GetUserByUUID>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final User user;
    private final String userUUID;

    private GetUserByUUIDResult()
    {
        super(true, null);
        this.user = null;
        this.userUUID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private GetUserByUUIDResult(User user)
    {
        super(true, null);
        Check.notNull(user, "user");
        this.user = user;
        this.userUUID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private GetUserByUUIDResult(String userUUID, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(userUUID, "userUUID");
        Check.notNull(reason, "reason");

        if (!possibleFailureReasons.contains(reason))
        {
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for GetUserByUUID.",
                            reason));
        }
        this.user = null;
        this.userUUID = userUUID;
    }

    /**
     * Retrieve the results from the GetUserByUUID command.
     * 
     * @return the user requested
     */
    public User getUser() throws PermissionDeniedException,
            UserDoesNotExistException
    {
        if (successful())
        {
            return this.user;
        } else
        {
            switch (getReason())
            {
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(null, this.userUUID);
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
        return String.format("GetUserByUUIDResult [user=%s]", user);
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
        if (!(obj instanceof GetUserByUUIDResult))
            return false;
        GetUserByUUIDResult other = (GetUserByUUIDResult) obj;
        if (user == null)
        {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

    /**
     * @param user
     *            the user retrieved
     * @return a new GetUserByUUIDResult representing the successful completion
     *         of a GetUserByUUID command.
     * 
     */
    public static GetUserByUUIDResult success(User user)
    {
        return new GetUserByUUIDResult(user);
    }

    /**
     * @param userUUID
     *            the UUID of the user who failed to be retrieved
     * @param reason
     *            the reason the command failed. Must be either
     *            USER_DOES_NOT_EXIST or PERMISSION_DENIED.
     * @return a new GetUserByUUIDResult representing the failed GetUserByUUID
     *         command.
     */
    public static GetUserByUUIDResult failure(String userUUID,
            FailureReason reason)
    {
        return new GetUserByUUIDResult(userUUID, reason);
    }
}