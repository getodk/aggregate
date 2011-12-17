package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.UpdateTableProperties;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;

/**
 * An UpdateTablePropertiesResult represents the result of executing an
 * UpdateTableProperties command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class UpdateTablePropertiesResult extends
	CommandResult<UpdateTableProperties> {

    private static final List<FailureReason> possibleFailureReasons;
    static {
	possibleFailureReasons = new ArrayList<FailureReason>();
	possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
	possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String tableID;

    /**
     * The success constructor. See {@link #success} for param info. Also need a
     * no-arg constructor for serialization by Gson.
     */
    private UpdateTablePropertiesResult() {
	super(true, null);
	this.tableID = null;
    }

    /**
     * The failure constructor. See one of the static failure methods for param
     * info.
     */
    private UpdateTablePropertiesResult(FailureReason reason, String tableID) {
	super(false, reason);

	if (!possibleFailureReasons.contains(reason))
	    throw new IllegalArgumentException(
		    String.format(
			    "Failure reason %s not a valid failure reason for UpdateTablePropertiesResult.",
			    reason));

	this.tableID = tableID;
    }

    /**
     * Check result of UpdateTableProperties command.
     * 
     * @throws TableDoesNotExistException
     * @throws PermissionDeniedException
     */
    public void checkResult() throws TableDoesNotExistException,
	    PermissionDeniedException {
	if (!successful()) {
	    switch (getReason()) {
	    case TABLE_DOES_NOT_EXIST:
		throw new TableDoesNotExistException(tableID);
	    case PERMISSION_DENIED:
		throw new PermissionDeniedException();
	    }
	}
    }

    public static UpdateTablePropertiesResult success() {
	return new UpdateTablePropertiesResult();
    }

    public static UpdateTablePropertiesResult failureTableDoesNotExist(
	    String tableID) {
	return new UpdateTablePropertiesResult(
		FailureReason.TABLE_DOES_NOT_EXIST, tableID);
    }

    public static UpdateTablePropertiesResult failurePermissionDenied() {
	return new UpdateTablePropertiesResult(FailureReason.PERMISSION_DENIED,
		null);
    }
}
