package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

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

    private final String requestingUserID;
    private final String userID;
    private final String userName;

    /**
     * For serialization by Gson.
     */
    @SuppressWarnings("unused")
    private CreateUser()
    {
        this.requestingUserID = null;
        this.userID = null;
        this.userName = null;
    }

    /**
     * Constructs a new CreateUser.
     * 
     * @param requestingUserID
     *            the userID of the user making the API call
     * @param userID
     *            the unique identifier the user will have.
     * @param userName
     *            the human readable name of the user. Must not be empty or
     *            null.
     */
    public CreateUser(String requestingUserID, String userID, String userName)
    {
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(userID, "userID");
        Check.notNullOrEmpty(userName, "userName");

        this.requestingUserID = requestingUserID;
        this.userID = userID;
        this.userName = userName;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return requestingUserID;
    }

    /**
     * @return the userID
     */
    public String getUserID()
    {
        return userID;
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
        return String.format(
                "CreateUser [requestingUserID=%s, userID=%s, userName=%s]",
                requestingUserID, userID, userName);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((requestingUserID == null) ? 0 : requestingUserID.hashCode());
        result = prime * result + ((userID == null) ? 0 : userID.hashCode());
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
        if (requestingUserID == null)
        {
            if (other.requestingUserID != null)
                return false;
        } else if (!requestingUserID.equals(other.requestingUserID))
            return false;
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
