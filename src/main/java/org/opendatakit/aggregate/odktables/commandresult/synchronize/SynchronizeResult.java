package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.synchronize.Synchronize;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A SynchronizeResult represents the result of executing a Synchronize command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SynchronizeResult extends CommandResult<Synchronize> {
    private static final List<FailureReason> possibleFailureReasons;
    static {
	possibleFailureReasons = new ArrayList<FailureReason>();
	possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
	possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final Modification modification;
    private final String tableID;

    private SynchronizeResult() {
	super(true, null);
	this.modification = null;
	this.tableID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private SynchronizeResult(Modification modification) {
	super(true, null);
	Check.notNull(modification, "modification");
	this.modification = modification;
	this.tableID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private SynchronizeResult(String tableID, FailureReason reason) {
	super(false, reason);

	Check.notNullOrEmpty(tableID, "tableID");
	if (!possibleFailureReasons.contains(reason))
	    throw new IllegalArgumentException(
		    String.format(
			    "Failure reason %s not a valid failure reason for Synchronize.",
			    reason));

	this.modification = null;
	this.tableID = tableID;
    }

    /**
     * Retrieve the results from the Synchronize command.
     * 
     * @throws TableDoesNotExistException
     */
    public Modification getModification() throws PermissionDeniedException,
	    TableDoesNotExistException {
	if (successful()) {
	    return modification;
	} else {
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
	return String.format("SynchronizeResult [modification=%s, tableID=%s]",
		modification, tableID);
    }

    /**
     * @return a new SynchronizeResult representing the successful completion of
     *         a Synchronize command.
     * 
     */
    public static SynchronizeResult success(Modification modification) {
	return new SynchronizeResult(modification);
    }

    /**
     * @return a new SynchronizeResult representing the failed completion of a
     *         Synchronize command.
     */
    public static SynchronizeResult failure(String tableID, FailureReason reason) {
	return new SynchronizeResult(tableID, reason);
    }
}
