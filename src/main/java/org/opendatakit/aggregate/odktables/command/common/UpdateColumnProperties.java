package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * UpdateColumnProperties is immutable.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class UpdateColumnProperties implements Command {

    private static final String path = "/common/updateColumnProperties";

    private final String requestingUserID;
    private final String tableID;
    private final String columnName;
    private final String properties;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private UpdateColumnProperties() {
	this.requestingUserID = null;
	this.tableID = null;
	this.columnName = null;
	this.properties = null;
    }

    /**
     * Constructs a new QueryForTables.
     */
    public UpdateColumnProperties(String requestingUserID, String tableID,
	    String columnName, String properties) {

	Check.notNullOrEmpty(requestingUserID, "requestingUserID");
	Check.notNullOrEmpty(tableID, "tableID");
	Check.notNullOrEmpty(columnName, "columnName");
	// properties may be null or empty

	this.requestingUserID = requestingUserID;
	this.tableID = tableID;
	this.columnName = columnName;
	this.properties = properties;
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
	return columnName;
    }

    /**
     * @return the tableID
     */
    public String getTableID() {
	return tableID;
    }

    /**
     * @return the properties
     */
    public String getProperties() {
	return properties;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID() {
	return this.requestingUserID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "UpdateColumnProperties [requestingUserID=" + requestingUserID
		+ ", tableID=" + tableID + ", columnName=" + columnName
		+ ", properties=" + properties + "]";
    }

    @Override
    public String getMethodPath() {
	return methodPath();
    }

    /**
     * @return the path of this Command relative to the address of an Aggregate
     *         instance. For example, if the full path to a command is
     *         http://aggregate.opendatakit.org/odktables/createTable, then this
     *         method would return '/odktables/createTable'.
     */
    public static String methodPath() {
	return path;
    }
}
