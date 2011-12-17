package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.ColumnDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.UpdateColumnProperties;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;

/**
 * An UpdateColumnPropertiesResult represents the result of executing an
 * UpdateColumnProperties command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class UpdateColumnPropertiesResult extends CommandResult<UpdateColumnProperties> {

    private static final List<FailureReason> possibleFailureReasons;
    static {
	possibleFailureReasons = new ArrayList<FailureReason>();
	possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
	possibleFailureReasons.add(FailureReason.COLUMN_DOES_NOT_EXIST);
	possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String tableID;
    private final String badColumnName;

    /**
     * The success constructor. See {@link #success} for param info. Also need a
     * no-arg constructor for serialization by Gson.
     */
    private UpdateColumnPropertiesResult() {
	super(true, null);
	this.tableID = null;
	this.badColumnName = null;
    }

    /**
     * The failure constructor. See one of the static failure methods for param
     * info.
     */
    private UpdateColumnPropertiesResult(FailureReason reason, String tableID, String badColumnName) {
	super(false, reason);

	if (!possibleFailureReasons.contains(reason))
	    throw new IllegalArgumentException(
		    String.format(
			    "Failure reason %s not a valid failure reason for UpdateColumnPropertiesResult.",
			    reason));

	this.tableID = tableID;
	this.badColumnName = badColumnName;
    }

    /**
     * Check result of UpdateColumnProperties command.
     * 
     * @throws TableDoesNotExistException
     * @throws PermissionDeniedException
     * @throws ColumnDoesNotExistException
     */
    public void checkResult() throws TableDoesNotExistException,
	    PermissionDeniedException, ColumnDoesNotExistException {
	if (!successful()) {
	    switch (getReason()) {
	    case TABLE_DOES_NOT_EXIST:
		throw new TableDoesNotExistException(tableID);
	    case COLUMN_DOES_NOT_EXIST:
		throw new ColumnDoesNotExistException(tableID, badColumnName);
	    case PERMISSION_DENIED:
		throw new PermissionDeniedException();
	    }
	}
    }

    public static UpdateColumnPropertiesResult success() {
	return new UpdateColumnPropertiesResult();
    }

    public static UpdateColumnPropertiesResult failureTableDoesNotExist(
	    String tableID) {
	return new UpdateColumnPropertiesResult(
		FailureReason.TABLE_DOES_NOT_EXIST, tableID, null);
    }

    public static UpdateColumnPropertiesResult failureColumnDoesNotExist(String tableID, String columnName)
    {
	return new UpdateColumnPropertiesResult(FailureReason.COLUMN_DOES_NOT_EXIST, tableID, columnName);
    }
    
    public static UpdateColumnPropertiesResult failurePermissionDenied() {
	return new UpdateColumnPropertiesResult(FailureReason.PERMISSION_DENIED,
		null, null);
    }
}
