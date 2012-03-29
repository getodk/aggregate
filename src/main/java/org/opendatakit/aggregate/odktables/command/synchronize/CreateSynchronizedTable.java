package org.opendatakit.aggregate.odktables.command.synchronize;

import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * CreateSynchronizedTable is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateSynchronizedTable implements Command {
    private static final String path = "/synchronize/createSynchronizedTable";

    private final String tableName;
    private final String requestingUserID;
    private final String tableID;
    private final String properties;
    private final List<Column> columns;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private CreateSynchronizedTable() {
	this.tableName = null;
	this.requestingUserID = null;
	this.tableID = null;
	this.properties = null;
	this.columns = null;

    }

    /**
     * Constructs a new CreateSynchronizedTable.
     */
    public CreateSynchronizedTable(String requestingUserID, String tableName,
	    String tableID, String properties, List<Column> columns) {

	Check.notNullOrEmpty(tableName, "tableName");
	Check.notNullOrEmpty(requestingUserID, "requestingUserID");
	Check.notNullOrEmpty(tableID, "tableID");
	// properties may be null or empty
	Check.notNull(columns, "columns");

	this.tableName = tableName;
	this.requestingUserID = requestingUserID;
	this.tableID = tableID;
	this.properties = properties;
	this.columns = columns;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
	return this.tableName;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID() {
	return this.requestingUserID;
    }

    /**
     * @return the tableID
     */
    public String getTableID() {
	return this.tableID;
    }

    public String getProperties() {
	return this.properties;
    }

    /**
     * @return the columns
     */
    public List<Column> getColumns() {
	return this.columns;
    }

    @Override
    public String toString() {
	return String.format("CreateSynchronizedTable: " + "tableName=%s "
		+ "requestingUserID=%s " + "tableID=%s " + "properties=%s"
		+ "columns=%s " + "", tableName, requestingUserID, tableID,
		properties, columns);
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
