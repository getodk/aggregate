package org.opendatakit.aggregate.odktables.client;

/**
 * <p>
 * A TableEntry represents the metadata associated with a table stored in ODK
 * Aggregate. This consists of:
 * <ul>
 * <li>The public uri of the user who owns the table</li>
 * <li>The human readable name of the user who owns the table</li>
 * <li>The tableId of the table</li>
 * <li>the human readable name of the table</li>
 * </ul>
 * </p>
 * 
 * <p>
 * TableEntry is immutable.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class TableEntry
{
    private final String userUri;
    private final String userName;
    private final String tableId;
    private final String tableName;

    /**
     * For Gson deserialization.
     */
    @SuppressWarnings("unused")
    private TableEntry()
    {
        this.userUri = null;
        this.userName = null;
        this.tableId = null;
        this.tableName = null;
    }

    /**
     * Constructs a new TableEntry.
     * 
     * @param userUri
     *            the public uri of the user who owns the table in the
     *            TableEntry
     * @param userName
     *            the human readable name of the user who owns the table
     * @param tableId
     *            the id of the table
     * @param tableName
     *            the human readable name of the table
     */
    public TableEntry(String userUri, String userName, String tableId,
            String tableName)
    {
        this.userUri = userUri;
        this.userName = userName;
        this.tableId = tableId;
        this.tableName = tableName;
    }

    /**
     * @return the tableId
     */
    public String getTableId()
    {
        return tableId;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @return the userName
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * @return the userUri
     */
    public String getUserUri()
    {
        return userUri;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String
                .format("TableEntry [userUri=%s, userName=%s, tableId=%s, tableName=%s]",
                        userUri, userName, tableId, tableName);
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
        result = prime * result
                + ((tableName == null) ? 0 : tableName.hashCode());
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
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
        if (!(obj instanceof TableEntry))
            return false;
        TableEntry other = (TableEntry) obj;
        if (tableId == null)
        {
            if (other.tableId != null)
                return false;
        } else if (!tableId.equals(other.tableId))
            return false;
        if (tableName == null)
        {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        if (userName == null)
        {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        if (userUri == null)
        {
            if (other.userUri != null)
                return false;
        } else if (!userUri.equals(other.userUri))
            return false;
        return true;
    }

}
