package org.opendatakit.aggregate.odktables.client.entity;

/**
 * A Filter selects a subset of rows from a table.
 * 
 * Filter is immutable and threadsafe.
 * 
 * @author the.dylan.price@gmail.com
 */
public class Filter {

    private final String columnName;
    private final FilterOperation op;
    private final String value;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private Filter() {
	this.columnName = null;
	this.op = null;
	this.value = null;
    }

    public Filter(String columnName, FilterOperation op, String value) {
	this.columnName = columnName;
	this.op = op;
	this.value = value;
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
	return columnName;
    }

    /**
     * @return the op
     */
    public FilterOperation getOp() {
	return op;
    }

    /**
     * @return the value
     */
    public String getValue() {
	return value;
    }
}
