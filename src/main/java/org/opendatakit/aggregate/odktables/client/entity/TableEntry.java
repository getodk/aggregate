package org.opendatakit.aggregate.odktables.client.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A TableEntry represents the metadata associated with a table stored in ODK
 * Aggregate. This consists of:
 * <ul>
 * <li>user: the User who owns the table</li>
 * <li>aggregateTableIdentifier: aggregate's identifier for the table</li>
 * <li>tableID: the client's unique identifier for the table.</li>
 * <li>tableName: the human readable name of the table</li>
 * <li>isSynchronized: true if the table is a synchronized table</li>
 * <li>columns: a list of the Columns defined for the table</li>
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
    private final User user;
    private final String aggregateTableIdentifier;
    private final String tableID;
    private final String tableName;
    private final List<Column> columns;
    private final boolean isSynchronized;

    /**
     * For Gson deserialization.
     */
    @SuppressWarnings("unused")
    private TableEntry()
    {
        this.aggregateTableIdentifier = null;
        this.user = null;
        this.tableID = null;
        this.tableName = null;
        this.columns = null;
        this.isSynchronized = false;
    }

    /**
     * Constructs a new TableEntry.
     * 
     * @param user
     *            the user who owns the table
     * @param tableID
     *            the id of the table
     * @param tableName
     *            the human readable name of the table
     */
    public TableEntry(User user, String aggregateTableIdentifier,
            String tableID, String tableName, List<Column> columns,
            boolean isSynchronized)
    {
        this.user = user;
        this.aggregateTableIdentifier = aggregateTableIdentifier;
        this.tableID = tableID;
        this.tableName = tableName;
        this.columns = new ArrayList<Column>(columns);
        this.isSynchronized = isSynchronized;
    }

    /**
     * @return the user who owns the table
     */
    public User getUser()
    {
        return this.user;
    }

    /**
     * @return the tableID
     */
    public String getTableID()
    {
        return tableID;
    }

    /**
     * @return the aggregateTableIdentifier
     */
    public String getAggregateTableIdentifier()
    {
        return aggregateTableIdentifier;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }

    public List<Column> getColumns()
    {
        return Collections.unmodifiableList(this.columns);
    }

    /**
     * @return the isSynchronized
     */
    public boolean isSynchronized()
    {
        return isSynchronized;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String
                .format("TableEntry [user=%s, aggregateTableIdentifier=%s, tableID=%s, tableName=%s, isSynchronized=%s]",
                        user, getAggregateTableIdentifier(), tableID,
                        tableName, isSynchronized);
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
                + ((aggregateTableIdentifier == null) ? 0
                        : aggregateTableIdentifier.hashCode());
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + (isSynchronized ? 1231 : 1237);
        result = prime * result + ((tableID == null) ? 0 : tableID.hashCode());
        result = prime * result
                + ((tableName == null) ? 0 : tableName.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
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
        if (aggregateTableIdentifier == null)
        {
            if (other.aggregateTableIdentifier != null)
                return false;
        } else if (!aggregateTableIdentifier
                .equals(other.aggregateTableIdentifier))
            return false;
        if (columns == null)
        {
            if (other.columns != null)
                return false;
        } else if (!columns.equals(other.columns))
            return false;
        if (isSynchronized != other.isSynchronized)
            return false;
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
        if (user == null)
        {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }
}
