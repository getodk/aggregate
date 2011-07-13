package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.GetUserByID;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A GetUserByIDResult represents the result of executing a GetUserByID command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetUserByIDResult extends CommandResult<GetUserByID>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final User user;
    private final String userID;

    private GetUserByIDResult()
    {
        super(true, null);
        this.user = null;
        this.userID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private GetUserByIDResult(User user)
    {
        super(true, null);
        Check.notNull(user, "user");
        this.user = user;
        this.userID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private GetUserByIDResult(String userID, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(userID, "userID");
        Check.notNull(reason, "reason");

        if (!possibleFailureReasons.contains(reason))
        {
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for GetUserByID.",
                            reason));
        }
        this.user = null;
        this.userID = userID;
    }

    /**
     * Retrieve the results from the GetUserByID command.
     * 
     * @return the user requested
     * @throws UserDoesNotExistException 
     */
    public User getUser() throws PermissionDeniedException, UserDoesNotExistException
    {
        if (successful())
        {
            return this.user;
        } else
        {
            switch (getReason())
            {
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(this.userID, null);
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
        return String.format("GetUserByIDResult [user=%s, userID=%s]", user,
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
        if (!(obj instanceof GetUserByIDResult))
            return false;
        GetUserByIDResult other = (GetUserByIDResult) obj;
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
     *            the user retrieved
     * @return a new GetUserByIDResult representing the successful completion of
     *         a GetUserByID command.
     * 
     */
    public static GetUserByIDResult success(User user)
    {
        return new GetUserByIDResult(user);
    }

    /**
     * @param userID
     *            the userID of the user who failed to be retrieved
     * @param reason
     *            the reason the command failed. Must be either
     *            USER_DOES_NOT_EXIST or PERMISSION_DENIED.
     * @return a new GetUserByIDResult representing the failed GetUserByID command.
     */
    public static GetUserByIDResult failure(String userID, FailureReason reason)
    {
        return new GetUserByIDResult(userID, reason);
    }
}
