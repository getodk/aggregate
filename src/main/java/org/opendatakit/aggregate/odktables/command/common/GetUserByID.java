package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * GetUserByID is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class GetUserByID implements Command
{
    private static final String path = "/odktables/common/getUserByID";
    
    private final String requestingUserID;
    private final String userID;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private GetUserByID()
    {
       this.requestingUserID = null;
       this.userID = null;
       
    }

    /**
     * Constructs a new GetUserByID.
     */
    public GetUserByID(String requestingUserID, String userID)
    {
        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(userID, "userID"); 
        
        this.requestingUserID = requestingUserID;
        this.userID = userID;
    }

    
    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }
    
    /**
     * @return the userID
     */
    public String getUserID()
    {
        return this.userID;
    }
    

    @Override
    public String toString()
    {
        return String.format("GetUserByID: " +
                "requestingUserID=%s " +
                "userID=%s " +
                "", requestingUserID, userID);
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
        if (!(obj instanceof GetUserByID))
            return false;
        GetUserByID other = (GetUserByID) obj;
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

