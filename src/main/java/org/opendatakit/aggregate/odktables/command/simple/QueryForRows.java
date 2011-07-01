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

    private final String userUri;
    private final String tableId;

    @SuppressWarnings("unused")
    private QueryForRows()
    {
        this.userUri = null;
        this.tableId = null;
    }

    /**
     * Constructs a new QueryForRows.
     * 
     * @param userUri
     *            the public unique identifier of the user who owns the table
     *            with the given tableId
     * @param tableId
     *            the unique identifier of the table owned by the user with uri
     *            userUri
     */
    public QueryForRows(String userUri, String tableId)
    {
        this.userUri = userUri;
        this.tableId = tableId;
    }

    /**
     * @return the userUri
     */
    public String getUserUri()
    {
        return userUri;
    }

    /**
     * @return the tableId
     */
    public String getTableId()
    {
        return tableId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("QueryForRows [userUri=%s, tableId=%s]", userUri,
                tableId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
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
        if (!(obj instanceof QueryForRows))
            return false;
        QueryForRows other = (QueryForRows) obj;
        if (tableId == null)
        {
            if (other.tableId != null)
                return false;
        } else if (!tableId.equals(other.tableId))
            return false;
        if (userUri == null)
        {
            if (other.userUri != null)
                return false;
        } else if (!userUri.equals(other.userUri))
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
