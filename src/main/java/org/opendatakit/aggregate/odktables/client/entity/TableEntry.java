package org.opendatakit.aggregate.odktables.client.entity;

/**
 * <p>
 * A TableEntry represents the metadata associated with a table stored in ODK
 * Aggregate. This consists of:
 * <ul>
 * <li>user: the User who owns the table</li>
 * <li>tableID: the client's unique identifier for the table.</li>
 * <li>tableName: the human readable name of the table</li>
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
    private final String userUUID;
    private final String tableID;
    private final String tableName;

    /**
     * For Gson deserialization.
     */
    @SuppressWarnings("unused")
    private TableEntry()
    {
        this.userUUID = null;
        this.tableID = null;
        this.tableName = null;
    }

    /**
     * Constructs a new TableEntry.
     * 
     * @param userUUID
     *            the UUID of the user who owns the table
     * @param userName
     *            the human readable name of the user who owns the table
     * @param tableID
     *            the id of the table
     * @param tableName
     *            the human readable name of the table
     */
    public TableEntry(String userUUID, String tableID,
            String tableName)
    {
        this.userUUID = userUUID;
        this.tableID = tableID;
        this.tableName = tableName;
    }
    
    /**
     * @return the UUID of the user who owns the table
     */
    public String getUserUUID()
    {
        return this.userUUID;
    }

    /**
     * @return the tableID
     */
    public String getTableID()
    {
        return tableID;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }

    @Override
    public String toString()
    {
        return String
                .format("TableEntry [userUUID=%s tableID=%s, tableName=%s]",
                       userUUID, tableID, tableName);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tableID == null) ? 0 : tableID.hashCode());
        result = prime * result
                + ((tableName == null) ? 0 : tableName.hashCode());
        result = prime * result
                + ((userUUID == null) ? 0 : userUUID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TableEntry other = (TableEntry) obj;
        if (tableID == null)
        {
            if (other.tableID != null)
                return false;
        } else if (!tableID.equals(other.tableID))
            return false;
        if (tableName == null)
        {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        if (userUUID == null)
        {
            if (other.userUUID != null)
                return false;
        } else if (!userUUID.equals(other.userUUID))
            return false;
        return true;
    }
}
