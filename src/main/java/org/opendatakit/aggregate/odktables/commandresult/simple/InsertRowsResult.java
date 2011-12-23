package org.opendatakit.aggregate.odktables.commandresult.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.odktables.client.exception.ColumnDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * An InsertRowsResult represents the result of executing and insertRows
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertRowsResult extends CommandResult<InsertRows> {

    private static final List<FailureReason> possibleFailureReasons;
    static {
	possibleFailureReasons = new ArrayList<FailureReason>();
	possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
	possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
	possibleFailureReasons.add(FailureReason.COLUMN_DOES_NOT_EXIST);
    }

    private final String tableID;
    private final Map<String, String> rowIDToaggregateRowIdentifier;
    private final String badColumnName;

    /**
     * Base constructor
     */
    private InsertRowsResult(boolean successful, FailureReason reason,
	    Map<String, String> rowIDToaggregateRowIdentifier, String tableID,
	    String badColumnName) {
	super(successful, reason);
	this.rowIDToaggregateRowIdentifier = rowIDToaggregateRowIdentifier;
	this.tableID = tableID;
	this.badColumnName = badColumnName;
    }

    /**
     * For serialization by Gson we need a no-arg constructor
     */
    private InsertRowsResult() {
	this(true, null, null, null, null);
    }

    /**
     * The success constructor. Constructs a successful InsertRowsResult. See
     * {@link #success(String, List)} for param info.
     */
    private InsertRowsResult(Map<String, String> rowIDToaggregateRowIdentifier) {
	this(true, null, rowIDToaggregateRowIdentifier, null, null);
	Check.notNull(rowIDToaggregateRowIdentifier,
		"rowIDtoaggregateRowIdentifier");
    }

    /**
     * The failure constructor. Constructs a failure InsertRowsResult. See
     * {@link #failure(String, FailureReason)} and
     * {@link #failure(String, String)} for param info.
     */
    private InsertRowsResult(String tableID, String badColumnName,
	    FailureReason reason) {
	this(false, reason, null, tableID, badColumnName);
	Check.notNullOrEmpty(tableID, "tableID");
	if (!possibleFailureReasons.contains(getReason())) {
	    throw new IllegalArgumentException("Not a valid FailureReason: "
		    + getReason());
	}
    }

    /**
     * Retrieve the results from the insertRows command.
     * 
     * @return a map of rowIDs to aggregateRowIdentifiers for the successfully
     *         inserted rows
     * @throws TableDoesNotExistException
     *             if the table that the insertRows command tried to insert to
     *             did not exist
     * @throws PermissionDeniedException
     * @throws ColumnDoesNotExistException
     */
    public Map<String, String> getMapOfInsertedRowIDsToAggregateRowIdentifiers()
	    throws TableDoesNotExistException, PermissionDeniedException,
	    ColumnDoesNotExistException {
	if (successful()) {
	    return this.rowIDToaggregateRowIdentifier;
	} else {
	    switch (getReason()) {
	    case TABLE_DOES_NOT_EXIST:
		throw new TableDoesNotExistException(tableID);
	    case PERMISSION_DENIED:
		throw new PermissionDeniedException();
	    case COLUMN_DOES_NOT_EXIST:
		throw new ColumnDoesNotExistException(tableID, badColumnName);
	    default:
		throw new RuntimeException("An unknown error occured.");
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return String
		.format("InsertRowsResult [tableID=%s, rowIDToaggregateRowIdentifier=%s]",
			tableID, rowIDToaggregateRowIdentifier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (!(obj instanceof InsertRowsResult))
	    return false;
	InsertRowsResult other = (InsertRowsResult) obj;
	if (rowIDToaggregateRowIdentifier == null) {
	    if (other.rowIDToaggregateRowIdentifier != null)
		return false;
	} else if (!rowIDToaggregateRowIdentifier
		.equals(other.rowIDToaggregateRowIdentifier))
	    return false;
	if (tableID == null) {
	    if (other.tableID != null)
		return false;
	} else if (!tableID.equals(other.tableID))
	    return false;
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime
		* result
		+ ((rowIDToaggregateRowIdentifier == null) ? 0
			: rowIDToaggregateRowIdentifier.hashCode());
	result = prime * result + ((tableID == null) ? 0 : tableID.hashCode());
	return result;
    }

    /**
     * Returns a new, successful InsertRowsResult.
     * 
     * @param rowIDstoaggregateRowIdentifiers
     *            a map of successfully inserted rowIDs to their corresponding
     *            aggregateRowIdentifiers.
     * 
     * @return a new InsertRowsResult which represents the successful outcome of
     *         an insertRows command.
     */
    public static InsertRowsResult success(
	    Map<String, String> rowIDstoaggregateRowIdentifiers) {
	return new InsertRowsResult(rowIDstoaggregateRowIdentifiers);
    }

    /**
     * Returns a new, failed InsertRowsResult
     * 
     * @param tableID
     *            the client's identifier for the table that the insertRows
     *            command dealt with. Must not be null or empty.
     * @param reason
     *            the reason the command failed. Must be either
     *            TABLE_DOES_NOT_EXIST or PERMISSION_DENIED.
     * @return a new InsertRowsResult which represents the failed outcome of an
     *         insertRows command.
     */
    public static InsertRowsResult failure(String tableID, FailureReason reason) {
	return new InsertRowsResult(tableID, null, reason);
    }

    /**
     * Returns a new, failed InsertRowsResult which failed because the client
     * tried to set a column which did not exist.
     * 
     * @param tableID
     *            the client's identifier of the table that the insertRows
     *            command deal with. Must not be null or empty.
     * @param badColumnName
     *            the name of the column which does not exist in the table
     * @return a new InsertRowsResult which represents the failed outcome of an
     *         insertRows command.
     */
    public static InsertRowsResult failure(String tableID, String badColumnName) {
	return new InsertRowsResult(tableID, badColumnName,
		FailureReason.COLUMN_DOES_NOT_EXIST);
    }
}
