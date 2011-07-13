package org.opendatakit.aggregate.odktables.command.simple;

import org.opendatakit.aggregate.odktables.command.Command;


/**
 * QueryForRows is a Command to query ODK Aggregate for rows in a specific
 * table. QueryForRows is immutable.
 * 
 * @author dylan-price
 * 
 */
public class QueryForRows implements Command
{

    private static final String path = "/odktables/queryForRows";

    private final String requestingUserID;
    private final String tableUUID;

    @SuppressWarnings("unused")
    private QueryForRows()
    {
        this.requestingUserID = null;
        this.tableUUID = null;
    }

    /**
     * Constructs a new QueryForRows.
     */
    public QueryForRows(String requestingUserID, String tableUUID)
    {
        this.requestingUserID = requestingUserID;
        this.tableUUID = tableUUID;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return requestingUserID;
    }

    /**
     * @return the tableUUID
     */
    public String getTableUUID()
    {
        return tableUUID;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("QueryForRows [requestingUserID=%s, tableUUID=%s]", requestingUserID,
                tableUUID);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tableUUID == null) ? 0 : tableUUID.hashCode());
        result = prime * result + ((requestingUserID == null) ? 0 : requestingUserID.hashCode());
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
        if (!(obj instanceof QueryForRows))
            return false;
        QueryForRows other = (QueryForRows) obj;
        if (tableUUID == null)
        {
            if (other.tableUUID != null)
                return false;
        } else if (!tableUUID.equals(other.tableUUID))
            return false;
        if (requestingUserID == null)
        {
            if (other.requestingUserID != null)
                return false;
        } else if (!requestingUserID.equals(other.requestingUserID))
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
