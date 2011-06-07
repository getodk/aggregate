package org.opendatakit.aggregate.odktables.command;

import org.opendatakit.aggregate.odktables.Command;

/**
 * DeleteUser is a Command to delete a user from ODK Aggregate. DeleteUser is
 * immutable.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class DeleteUser implements Command
{
    private static final String path = "/odktables/deleteUser";

    private final String userId;

    @SuppressWarnings("unused")
    private DeleteUser()
    {
        this.userId = null;
    }

    /**
     * Constructs a new DeleteUser.
     * 
     * @param userId
     *            the unique identifier of the user to delete. Must consist of
     *            only letters, numbers, and underscores, and must not be empty
     *            or null.
     */
    public DeleteUser(String userId)
    {
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");

        this.userId = userId;
    }

    /**
     * @return the path
     */
    public static String getPath()
    {
        return path;
    }

    /**
     * @return the userId
     */
    public String getUserId()
    {
        return userId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("DeleteUser [userId=%s]", userId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
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
        if (obj == null)
            return false;
        if (!(obj instanceof DeleteUser))
            return false;
        DeleteUser other = (DeleteUser) obj;
        if (userId == null)
        {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }

    @Override
    public String getMethodPath()
    {
        return methodPath();
    }

    /**
     * @return the path of this Command relative to the address of an Aggregate
     *         instance. For example, if the full path to a command is
     *         http://aggregate.opendatakit.org/odktables/createTable, then this
     *         method would return '/odktables/createTable'.
     */
    public static String methodPath()
    {
        return path;
    }
}
