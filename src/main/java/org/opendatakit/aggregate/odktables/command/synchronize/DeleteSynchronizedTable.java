package org.opendatakit.aggregate.odktables.command.synchronize;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * DeleteSynchronizedTable is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteSynchronizedTable implements Command {
    private static final String path = "/synchronize/deleteSynchronizedTable";

    private final String requestingUserID;
    private final String tableID;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private DeleteSynchronizedTable() {
	this.requestingUserID = null;
	this.tableID = null;

    }

    /**
     * Constructs a new DeleteSynchronizedTable.
     */
    public DeleteSynchronizedTable(String requestingUserID,
	    String aggregateTableIdentifier) {

	Check.notNullOrEmpty(requestingUserID, "requestingUserID");
	Check.notNullOrEmpty(aggregateTableIdentifier,
		"aggregateTableIdentifier");

	this.requestingUserID = requestingUserID;
	this.tableID = aggregateTableIdentifier;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID() {
	return this.requestingUserID;
    }

    /**
     * @return the aggregateTableIdentifier
     */
    public String getTableID() {
	return this.tableID;
    }

    @Override
    public String toString() {
	return String.format("DeleteSynchronizedTable: "
		+ "requestingUserID=%s " + "aggregateTableIdentifier=%s " + "",
		requestingUserID, tableID);
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
