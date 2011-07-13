package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * GetTablePermissons is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class GetTablePermissions implements Command
{
    private static final String path = "/odktables/common/getTablePermissons";
    
    private final String requestingUserID;
    private final String tableUUID;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private GetTablePermissions()
    {
       this.requestingUserID = null;
       this.tableUUID = null;
       
    }

    /**
     * Constructs a new GetTablePermissons.
     */
    public GetTablePermissions(String requestingUserID, String tableUUID)
    {
        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableUUID, "tableUUID"); 
        
        this.requestingUserID = requestingUserID;
        this.tableUUID = tableUUID;
    }

    
    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }
    
    /**
     * @return the tableUUID
     */
    public String getTableUUID()
    {
        return this.tableUUID;
    }
    

    @Override
    public String toString()
    {
        return String.format("GetTablePermissons: " +
                "requestingUserID=%s " +
                "tableUUID=%s " +
                "", requestingUserID, tableUUID);
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
                + ((tableUUID == null) ? 0 : tableUUID.hashCode());
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
        if (!(obj instanceof GetTablePermissions))
            return false;
        GetTablePermissions other = (GetTablePermissions) obj;
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

