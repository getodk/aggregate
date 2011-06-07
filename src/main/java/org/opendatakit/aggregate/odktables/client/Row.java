package org.opendatakit.aggregate.odktables.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A Row represents a row in a table. A row has two attributes:
 * <ul>
 * <li>rowId: the unique identifier of the row</li>
 * <li>values: a set of name-value pairs where each name is a column name and
 * the value is the value for that column</li>
 * </ul>
 * 
 * Row is mutable and currently not threadsafe.
 * 
 * @author the.dylan.price@gmail.com
 */
public final class Row
{

    private final String rowId;
    private final Map<String, String> values;

    /**
     * So that Gson can serialize this class.
     */
    @SuppressWarnings("unused")
    private Row()
    {
        this.rowId = null;
        this.values = null;
    }

    /**
     * Constructs a new Row.
     * 
     * @param rowId
     *            the unique identifier for this row. This must consist of only
     *            letters, numbers, and underscores.
     */
    public Row(String rowId)
    {
        this.rowId = rowId;
        this.values = new HashMap<String, String>();
    }

    /**
     * Sets the value of a column.
     * 
     * @param column
     *            the name of the column
     * @param value
     *            the value of the column
     */
    public void setColumn(String column, String value)
    {
        this.values.put(column, value);
    }

    /**
     * Retrieves the current value of the specified column.
     * 
     * @param column
     *            the name of the column
     * @return the value currently set for the given column name, or null if no
     *         such value exists.
     */
    public String getValue(String column)
    {
        return this.values.get(column);
    }

    /**
     * @return a map of column names to column values in this Row, as an unmodifiable map.
     */
    public Map<String, String> getColumnValuePairs()
    {
        return Collections.unmodifiableMap(values);
    }

    /**
     * @return the rowId of this Row.
     */
    public String getRowId()
    {
        return this.rowId;
    }

    @Override
    public String toString()
    {
        return this.rowId;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Row))
            return false;
        Row o = (Row) other;
        return o.rowId.equals(this.rowId) && o.values.equals(this.values);
    }

    @Override
    public int hashCode()
    {
        return 3 * this.rowId.hashCode() + 7 * this.values.hashCode();
    }
}
