package org.opendatakit.aggregate.odktables.command.simple;

import org.opendatakit.aggregate.odktables.command.Command;


/**
 * QueryForTables is a Command to query ODK Aggregate for information about what
 * tables are stored in it. QueryForTables is immutable.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class QueryForTables implements Command
{
    private static final String path = "/odktables/queryForTables";
    
    private final String requestingUserID;

    /**
     * Construct a new QueryForTables which will initially query for all tables
     * stored in Aggregate.
     */
    public QueryForTables(String requestingUserID)
    {
        this.requestingUserID = requestingUserID;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return requestingUserID;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("QueryForTables [requestingUserID=%s]",
                requestingUserID);
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
        if (!(obj instanceof QueryForTables))
            return false;
        QueryForTables other = (QueryForTables) obj;
        if (requestingUserID == null)
        {
            if (other.requestingUserID != null)
                return false;
        } else if (!requestingUserID.equals(other.requestingUserID))
            return false;
        return true;
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
        return result;
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
