package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.exception.ColumnDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.OutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.synchronize.InsertSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A InsertSynchronizedRowsResult represents the result of executing a
 * InsertSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertSynchronizedRowsResult extends
	CommandResult<InsertSynchronizedRows> {
    private static final List<FailureReason> possibleFailureReasons;
    static {
	possibleFailureReasons = new ArrayList<FailureReason>();
	possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
	possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
	possibleFailureReasons.add(FailureReason.OUT_OF_SYNCH);
	possibleFailureReasons.add(FailureReason.COLUMN_DOES_NOT_EXIST);
    }

    private final Modification modification;
    private final String tableID;
    private final String badColumnName;

    private InsertSynchronizedRowsResult(boolean successful,
	    FailureReason reason, Modification modification, String tableID,
	    String badColumnName) {
	super(successful, reason);
	this.modification = modification;
	this.tableID = tableID;
	this.badColumnName = badColumnName;
    }

    private InsertSynchronizedRowsResult() {
	this(true, null, null, null, null);
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private InsertSynchronizedRowsResult(Modification modification) {
	this(true, null, modification, null, null);
	Check.notNull(modification, "modfication");
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private InsertSynchronizedRowsResult(String tableID, String badColumnName,
	    FailureReason reason) {
	this(false, reason, null, tableID, badColumnName);
	Check.notNullOrEmpty(tableID, "tableID");
	if (!possibleFailureReasons.contains(reason))
	    throw new IllegalArgumentException(
		    String.format(
			    "Failure reason %s not a valid failure reason for InsertSynchronizedRows.",
			    reason));
    }

    /**
     * Retrieve the results from the InsertSynchronizedRows command.
     * 
     * @throws OutOfSynchException
     * @throws TableDoesNotExistException
     * @throws PermissionDeniedException
     * @throws ColumnDoesNotExistException
     */
    public Modification getModification() throws OutOfSynchException,
	    TableDoesNotExistException, PermissionDeniedException,
	    ColumnDoesNotExistException {
	if (successful()) {
	    return this.modification;
	} else {
	    switch (getReason()) {
	    case OUT_OF_SYNCH:
		throw new OutOfSynchException();
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
	return String.format(
		"InsertSynchronizedRowsResult [modification=%s, tableID=%s]",
		modification, tableID);
    }

    /**
     * @param modification
     *            the latest modification of the table in Aggregate.
     * @return a new InsertSynchronizedRowsResult representing the successful
     *         completion of an InsertSynchronizedRows command.
     * 
     */
    public static InsertSynchronizedRowsResult success(Modification modification) {
	return new InsertSynchronizedRowsResult(modification);
    }

    /**
     * @return a new InsertSynchronizedRowsResult representing the failed
     *         completion of an InsertSynchronizedRows command.
     */
    public static InsertSynchronizedRowsResult failure(String tableID,
	    FailureReason reason) {
	return new InsertSynchronizedRowsResult(tableID, null, reason);
    }

    public static InsertSynchronizedRowsResult failure(String tableID,
	    String badColumnName) {
	return new InsertSynchronizedRowsResult(tableID, badColumnName,
		FailureReason.COLUMN_DOES_NOT_EXIST);
    }
}
