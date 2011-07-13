package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * SetPermissionsPermissions is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class SetPermissionsPermissions implements Command
{
    private static final String path = "/odktables/common/setPermissionsPermissions";
    
    private final String userUUID;
    private final boolean read;
    private final boolean write;
    private final String requestingUserID;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private SetPermissionsPermissions()
    {
       this.userUUID = null;
       this.read = false;
       this.write = false;
       this.requestingUserID = null;
       
    }

    /**
     * Constructs a new SetPermissionsPermissions.
     */
    public SetPermissionsPermissions(String userUUID, boolean read, boolean write, String requestingUserID)
    {
        
        Check.notNullOrEmpty(userUUID, "userUUID");
        Check.notNull(read, "read");
        Check.notNull(write, "write");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID"); 
        
        this.userUUID = userUUID;
        this.read = read;
        this.write = write;
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
     * @return the read
     */
    public boolean getRead()
    {
        return this.read;
    }
    
    /**
     * @return the write
     */
    public boolean getWrite()
    {
        return this.write;
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
        return String.format("SetPermissionsPermissions: " +
                "userUUID=%s " +
                "read=%s " +
                "write=%s " +
                "requestingUserID=%s " +
                "", userUUID, read, write, requestingUserID);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (read ? 1231 : 1237);
        result = prime
                * result
                + ((requestingUserID == null) ? 0 : requestingUserID.hashCode());
        result = prime * result
                + ((userUUID == null) ? 0 : userUUID.hashCode());
        result = prime * result + (write ? 1231 : 1237);
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
        if (!(obj instanceof SetPermissionsPermissions))
            return false;
        SetPermissionsPermissions other = (SetPermissionsPermissions) obj;
        if (read != other.read)
            return false;
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
        if (write != other.write)
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

