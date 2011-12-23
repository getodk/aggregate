package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.synchronize.DeleteSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A DeleteSynchronizedTableResult represents the result of executing a
 * DeleteSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteSynchronizedTableResult extends
	CommandResult<DeleteSynchronizedTable> {
    private static final List<FailureReason> possibleFailureReasons;
    static {
	possibleFailureReasons = new ArrayList<FailureReason>();
	possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
	possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String tableID;

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private DeleteSynchronizedTableResult() {
	super(true, null);
	this.tableID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private DeleteSynchronizedTableResult(String tableID, FailureReason reason) {
	super(false, reason);

	Check.notNullOrEmpty(tableID, "tableID");
	if (!possibleFailureReasons.contains(reason))
	    throw new IllegalArgumentException(
		    String.format(
			    "Failure reason %s not a valid failure reason for DeleteSynchronizedTable.",
			    reason));

	this.tableID = tableID;
    }

    /**
     * Retrieve the results from the DeleteSynchronizedTable command.
     * 
     * @throws TableDoesNotExistException
     */
    public void checkResults() throws PermissionDeniedException,
	    TableDoesNotExistException {
	if (!successful()) {
	    switch (getReason()) {
	    case TABLE_DOES_NOT_EXIST:
		throw new TableDoesNotExistException(tableID);
	    case PERMISSION_DENIED:
		throw new PermissionDeniedException();
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
	return String.format("DeleteSynchronizedTableResult [tableID=%s]",
		tableID);
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
	result = prime * result + ((tableID == null) ? 0 : tableID.hashCode());
	return result;
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
	if (!(obj instanceof DeleteSynchronizedTableResult))
	    return false;
	DeleteSynchronizedTableResult other = (DeleteSynchronizedTableResult) obj;
	if (tableID == null) {
	    if (other.tableID != null)
		return false;
	} else if (!tableID.equals(other.tableID))
	    return false;
	return true;
    }

    /**
     * @return a new DeleteSynchronizedTableResult representing the successful
     *         completion of a DeleteSynchronizedTable command.
     * 
     */
    public static DeleteSynchronizedTableResult success() {
	return new DeleteSynchronizedTableResult();
    }

    /**
     * @return a new DeleteSynchronizedTableResult representing the failed
     *         completion of a DeleteSynchronizedTable command.
     */
    public static DeleteSynchronizedTableResult failure(String tableID,
	    FailureReason reason) {
	return new DeleteSynchronizedTableResult(tableID, reason);
    }
}
