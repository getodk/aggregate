package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * SetPermissions is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class SetPermissions implements Command
{
    private static final String path = "/odktables/common/setPermissions";
    
    private final boolean read;
    private final String tableUUID;
    private final boolean write;
    private final String userUUID;
    private final String requestingUserID;
    private final boolean delete;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private SetPermissions()
    {
       this.read = false;
       this.tableUUID = null;
       this.write = false;
       this.userUUID = null;
       this.requestingUserID = null;
       this.delete = false;
       
    }

    /**
     * Constructs a new SetPermissions.
     */
    public SetPermissions(boolean read, String tableUUID, boolean write, String userUUID, String requestingUserID, boolean delete)
    {
        
        Check.notNull(read, "read");
        Check.notNullOrEmpty(tableUUID, "tableUUID");
        Check.notNull(write, "write");
        Check.notNullOrEmpty(userUUID, "userUUID");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNull(delete, "delete"); 
        
        this.read = read;
        this.tableUUID = tableUUID;
        this.write = write;
        this.userUUID = userUUID;
        this.requestingUserID = requestingUserID;
        this.delete = delete;
    }

    
    /**
     * @return the read
     */
    public boolean getRead()
    {
        return this.read;
    }
    
    /**
     * @return the tableUUID
     */
    public String getTableUUID()
    {
        return this.tableUUID;
    }
    
    /**
     * @return the write
     */
    public boolean getWrite()
    {
        return this.write;
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
    
    /**
     * @return the delete
     */
    public boolean getDelete()
    {
        return this.delete;
    }
    

    @Override
    public String toString()
    {
        return String.format("SetPermissions: " +
                "read=%s " +
                "tableUUID=%s " +
                "write=%s " +
                "userUUID=%s " +
                "requestingUserID=%s " +
                "delete=%s " +
                "", read, tableUUID, write, userUUID, requestingUserID, delete);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (delete ? 1231 : 1237);
        result = prime * result + (read ? 1231 : 1237);
        result = prime
                * result
                + ((requestingUserID == null) ? 0 : requestingUserID.hashCode());
        result = prime * result
                + ((tableUUID == null) ? 0 : tableUUID.hashCode());
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
        if (!(obj instanceof SetPermissions))
            return false;
        SetPermissions other = (SetPermissions) obj;
        if (delete != other.delete)
            return false;
        if (read != other.read)
            return false;
        if (requestingUserID == null)
        {
            if (other.requestingUserID != null)
                return false;
        } else if (!requestingUserID.equals(other.requestingUserID))
            return false;
        if (tableUUID == null)
        {
            if (other.tableUUID != null)
                return false;
        } else if (!tableUUID.equals(other.tableUUID))
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

