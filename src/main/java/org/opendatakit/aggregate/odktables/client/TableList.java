package org.opendatakit.aggregate.odktables.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * A TableList is a list of TableEntry objects. TableLists are mutable and are
 * currently not threadsafe.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class TableList implements Iterable<TableEntry>
{

    private final List<TableEntry> tableEntries;

    /**
     * Construct a new, empty TableList.
     */
    public TableList()
    {
        tableEntries = new ArrayList<TableEntry>();
    }

    /**
     * Adds a new entry to this TableList.
     * 
     * @param userUri
     *            the public uri of the user who owns the table
     * @param userName
     *            the human readable name of the user who owns the table
     * @param tableId
     *            the id of the table
     * @param tableName
     *            the human readable name of the table
     */
    public void addEntry(String userUri, String userName, String tableId,
            String tableName)
    {
        TableEntry entry = new TableEntry(userUri, userName, tableId, tableName);
        tableEntries.add(entry);
    }

    @Override
    public Iterator<TableEntry> iterator()
    {
        List<TableEntry> unmodifiableEntries = Collections
                .unmodifiableList(tableEntries);
        return unmodifiableEntries.iterator();
    }

    /**
     * @return
     * @see java.util.List#size()
     */
    public int size()
    {
        return tableEntries.size();
    }

    /**
     * @return
     * @see java.util.List#isEmpty()
     */
    public boolean isEmpty()
    {
        return tableEntries.isEmpty();
    }

    /**
     * @param index
     * @return
     * @see java.util.List#get(int)
     */
    public TableEntry get(int index)
    {
        return tableEntries.get(index);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("TableList [tableEntries=%s]", tableEntries);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((tableEntries == null) ? 0 : tableEntries.hashCode());
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
        if (!(obj instanceof TableList))
            return false;
        TableList other = (TableList) obj;
        if (tableEntries == null)
        {
            if (other.tableEntries != null)
                return false;
        } else if (!tableEntries.equals(other.tableEntries))
            return false;
        return true;
    }

}
