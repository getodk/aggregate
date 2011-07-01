package org.opendatakit.aggregate.odktables.commandresult.common;

import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.command.result.DeleteUserResult;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;

/**
 * DeleteUserResult represents the result of the execution of a DeleteUser
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteUserResult extends CommandResult<DeleteUser>
{

    private final String userId;

    /**
     * For serialization by Gson we need a no-arg constructor
     */
    private DeleteUserResult()
    {
        super(true, null);
        this.userId = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private DeleteUserResult(String userId)
    {
        super(true, null);
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");
        this.userId = userId;
    }

    /**
     * Retrieves the result from the DeleteTable command.
     * 
     * @return the userId of the successfully deleted user.
     */
    public String getDeletedUserId()
    {
        return this.userId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("DeleteUserResult [userId=%s]", userId);
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
        if (!(obj instanceof DeleteUserResult))
            return false;
        DeleteUserResult other = (DeleteUserResult) obj;
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
     *            the unique identifier of the user that was successfully
     *            deleted.
     * @return a new DeleteUserResult representing the successful deletion of a
     *         user.
     */
    public static DeleteUserResult success(String userId)
    {
        return new DeleteUserResult(userId);
    }
}
