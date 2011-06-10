package org.opendatakit.aggregate.odktables.client;

/**
 * <p>
 * A User represents a simple user created through the
 * {@link AggregateConnection#createUser} command in the odktables API.
 * </p>
 * 
 * <p>
 * A User has three attributes:
 * <ul>
 * <li>userId: the private unique identifier of the user which is known only to
 * the user it represents</li>
 * <li>userUri: the public unique identifier of the user</li>
 * <li>userName: a human readable name for the user</li>
 * </ul>
 * </p>
 * 
 * <p>
 * User is immutable.
 * <p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class User
{

    private final String userId;
    private final String userUri;
    private final String userName;

    /**
     * Creates a new User.
     * 
     * @param userId
     *            the private identifier of the user which is known only to the
     *            user whom it represents
     * @param userUri
     *            the public unique identifier of the user
     * @param userName
     *            the human readable name of the user
     */
    public User(String userId, String userUri, String userName)
    {
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId was null or empty");
        if (userUri == null || userUri.length() == 0)
            throw new IllegalArgumentException("userUri was null or empty");
        if (userName == null || userName.length() == 0)
            throw new IllegalArgumentException("userName was null or empty");
        
        this.userId = userId;
        this.userUri = userUri;
        this.userName = userName;
    }

    /**
     * @return the userId
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * @return the userUri
     */
    public String getUserUri()
    {
        return userUri;
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
        return String.format("User [userId=%s, userUri=%s, userName=%s]",
                userId, userUri, userName);
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
        result = prime * result + ((userUri == null) ? 0 : userUri.hashCode());
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
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
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
        if (userUri == null)
        {
            if (other.userUri != null)
                return false;
        } else if (!userUri.equals(other.userUri))
            return false;
        return true;
    }
    
}
