package org.opendatakit.aggregate.odktables.command.common;

import java.util.Collections;
import java.util.Map;

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
    private final Map<String, String> columnsToProperties;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private UpdateColumnProperties() {
	this.requestingUserID = null;
	this.tableID = null;
	this.columnsToProperties = null;
    }

    /**
     * Constructs a new QueryForTables.
     */
    public UpdateColumnProperties(String requestingUserID, String tableID,
	    Map<String, String> columnsToProperties) {

	Check.notNullOrEmpty(requestingUserID, "requestingUserID");
	Check.notNullOrEmpty(tableID, "tableID");
	Check.notNullOrEmpty(columnsToProperties, "columnsToProperties");
	// properties may be null or empty

	this.requestingUserID = requestingUserID;
	this.tableID = tableID;
	this.columnsToProperties = columnsToProperties;
    }

    /**
     * @return the tableID
     */
    public String getTableID() {
	return tableID;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID() {
	return this.requestingUserID;
    }
    
    /**
     * @return the columnsToProperties as an unmodifiable map
     */
    public Map<String, String> getColumnsToProperties()
    {
	return Collections.unmodifiableMap(this.columnsToProperties);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "UpdateColumnProperties [requestingUserID=" + requestingUserID
		+ ", tableID=" + tableID + ", columnsToProperties="
		+ columnsToProperties + "]";
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
