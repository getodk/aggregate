package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * GetUserByUUID is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class GetUserByUUID implements Command
{
    private static final String path = "/odktables/common/getUserByUUID";
    
    private final String userUUID;
    private final String requestingUserID;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private GetUserByUUID()
    {
       this.userUUID = null;
       this.requestingUserID = null;
       
    }

    /**
     * Constructs a new GetUserByUUID.
     */
    public GetUserByUUID(String userUUID, String requestingUserID)
    {
        
        Check.notNullOrEmpty(userUUID, "userUUID");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID"); 
        
        this.userUUID = userUUID;
        this.requestingUserID = requestingUserID;
    }

    
    /**
     * @return the userUUID
     */
    public String getUserUUID()
    {
        return this.userUUID;
    }
    
    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }
    

    @Override
    public String toString()
    {
        return String.format("GetUserByUUID: " +
                "userUUID=%s " +
                "requestingUserID=%s " +
                "", userUUID, requestingUserID);
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
        result = prime * result
                + ((userUUID == null) ? 0 : userUUID.hashCode());
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
        if (!(obj instanceof GetUserByUUID))
            return false;
        GetUserByUUID other = (GetUserByUUID) obj;
        if (requestingUserID == null)
        {
            if (other.requestingUserID != null)
                return false;
        } else if (!requestingUserID.equals(other.requestingUserID))
            return false;
        if (userUUID == null)
        {
            if (other.userUUID != null)
                return false;
        } else if (!userUUID.equals(other.userUUID))
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

