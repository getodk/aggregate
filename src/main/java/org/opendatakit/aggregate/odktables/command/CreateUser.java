package org.opendatakit.aggregate.odktables.command;

import org.opendatakit.aggregate.odktables.Command;

/**
 * CreateUser is a Command to create a new user in ODK Aggregate. CreateUser is
 * immutable.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CreateUser implements Command
{
    private static final String path = "/odktables/createUser";

    private final String userId;
    private final String userName;

    /**
     * For serialization by Gson.
     */
    @SuppressWarnings("unused")
    private CreateUser()
    {
        this.userId = null;
        this.userName = null;
    }

    /**
     * Constructs a new CreateUser.
     * 
     * @param userId
     *            the unique identifier the user will have. Must consist of only
     *            letters, numbers, and underscores, and must not be empty or
     *            null.
     * @param userName
     *            the human readable name of the user. Must not be empty or
     *            null.
     */
    public CreateUser(String userId, String userName)
    {
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");
        if (userName == null || userName.length() == 0)
            throw new IllegalArgumentException("userName '" + userName
                    + "' was null or empty");

        this.userId = userId;
        this.userName = userName;
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

    /**
     * @return the userName
     */
    public String getUserName()
    {
        return userName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "CreateUser [userId=" + userId + ", userName=" + userName + "]";
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
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
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
        if (!(obj instanceof CreateUser))
            return false;
        CreateUser other = (CreateUser) obj;
        if (userId == null)
        {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        if (userName == null)
        {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
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
