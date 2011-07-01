package org.opendatakit.aggregate.odktables.client.entity;

/**
 * <p>
 * A User represents a user of the odktables API.
 * </p>
 * 
 * <p>
 * A User has three attributes:
 * <ul>
 * <li>userID: the private unique identifier of the user which is known only to
 * the user it represents</li>
 * <li>userUUID: the public unique identifier of the user</li>
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

    private final String userID;
    private final String userUUID;
    private final String userName;

    /**
     * Creates a new User.
     * 
     * @param userID
     *            the private identifier of the user which is known only to the
     *            user whom it represents
     * @param userUUID
     *            the public unique identifier of the user
     * @param userName
     *            the human readable name of the user
     */
    public User(String userID, String userUUID, String userName)
    {
        if (userID == null || userID.length() == 0)
            throw new IllegalArgumentException("userID was null or empty");
        if (userUUID == null || userUUID.length() == 0)
            throw new IllegalArgumentException("userUUID was null or empty");
        if (userName == null || userName.length() == 0)
            throw new IllegalArgumentException("userName was null or empty");

        this.userID = userID;
        this.userUUID = userUUID;
        this.userName = userName;
    }

    /**
     * @return the userID
     */
    public String getUserID()
    {
        return userID;
    }

    /**
     * @return the userUUID
     */
    public String getUserUUID()
    {
        return userUUID;
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
        return String.format("User [userID=%s, userUUID=%s, userName=%s]",
                userID, userUUID, userName);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userID == null) ? 0 : userID.hashCode());
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + ((userUUID == null) ? 0 : userUUID.hashCode());
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
        if (userID == null)
        {
            if (other.userID != null)
                return false;
        } else if (!userID.equals(other.userID))
            return false;
        if (userName == null)
        {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        if (userUUID == null)
        {
            if (other.userUUID != null)
                return false;
        } else if (!userUUID.equals(other.userUUID))
            return false;
        return true;
    }

}
