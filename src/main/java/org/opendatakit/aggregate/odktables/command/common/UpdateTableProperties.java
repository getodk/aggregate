package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * UpdateTableProperties is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class UpdateTableProperties implements Command {

    private static final String path = "/common/updateTableProperties";

    private final String requestingUserID;
    private final String tableID;
    private final String properties;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private UpdateTableProperties() {
	this.requestingUserID = null;
	this.tableID = null;
	this.properties = null;
    }

    /**
     * Constructs a new QueryForTables.
     */
    public UpdateTableProperties(String requestingUserID, String tableID,
	    String properties) {

	Check.notNullOrEmpty(requestingUserID, "requestingUserID");
	Check.notNullOrEmpty(tableID, "tableID");
	// properties may be null or empty

	this.requestingUserID = requestingUserID;
	this.tableID = tableID;
	this.properties = properties;
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
	return "UpdateTableProperties [requestingUserID=" + requestingUserID
		+ ", tableID=" + tableID + ", properties=" + properties + "]";
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
