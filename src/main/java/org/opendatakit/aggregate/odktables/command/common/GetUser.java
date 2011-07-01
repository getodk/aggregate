package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;


/**
 * GetUserUri is a Command to retrieve the public uri of a user based on their
 * userId. GetUserUri is immutable.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class GetUser implements Command
{
    private static final String path = "/odktables/getUser";

    private final String userId;

    /**
     * For serialization by Gson.
     */
    @SuppressWarnings("unused")
    private GetUser()
    {
        this.userId = null;
    }

    /**
     * Constructs a new GetUserUri.
     * 
     * @param userId
     *            the unique identifier of a user. Must consist of only letters,
     *            numbers, and underscores, and must not be null or empty.
     */
    public GetUser(String userId)
    {
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");
        this.userId = userId;
    }

    /**
     * @return the userId.
     */
    public String getUserId()
    {
        return this.userId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "GetUserUri [userId=" + userId + "]";
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
        if (!(obj instanceof GetUser))
            return false;
        GetUser other = (GetUser) obj;
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
