package org.opendatakit.aggregate.odktables.commandresult.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A CreateTableResult represents the result of executing a CreateTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateTableResult extends CommandResult<CreateTable> {
    private static final List<FailureReason> possibleFailureReasons;
    static {
	possibleFailureReasons = new ArrayList<FailureReason>();
	possibleFailureReasons.add(FailureReason.TABLE_ALREADY_EXISTS);
    }

    private final String tableID;

    /**
     * For serialization by Gson we need a no-arg constructor.
     */
    private CreateTableResult() {
	super(true, null);
	this.tableID = null;
    }

    /**
     * The success constructor. See {@link #success(String)} for param info.
     */
    private CreateTableResult(String tableID) {
	super(true, null);
	this.tableID = tableID;
    }

    /**
     * The failure constructor. See
     * {@link #failure(String, org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason)}
     * for param info.
     */
    private CreateTableResult(String tableID, FailureReason reason) {
	super(false, reason);

	Check.notNullOrEmpty(tableID, "tableID");

	if (!possibleFailureReasons.contains(getReason())) {
	    throw new IllegalArgumentException("Not a valid FailureReason: "
		    + getReason());
	}

	this.tableID = tableID;
    }

    /**
     * Retrieve the results from the createTable Command.
     * 
     * @return the tableID of the table that was successfully created
     * @throws TableAlreadyExistsException
     *             if the table that the createTable Command tried to create
     *             already existed.
     */
    public String getCreatedTableId() throws TableAlreadyExistsException,
	    UserDoesNotExistException {
	if (successful()) {
	    return getTableId();
	} else {
	    switch (getReason()) {
	    case TABLE_ALREADY_EXISTS:
		throw new TableAlreadyExistsException(getTableId());
	    default:
		throw new RuntimeException("An unknown error occured.");
	    }
	}

    }

    /**
     * @return the tableID associated with this result.
     */
    public String getTableId() {
	return this.tableID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return String.format("CreateTableResult [tableID=%s]", tableID);
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
	if (!(obj instanceof CreateTableResult))
	    return false;
	CreateTableResult other = (CreateTableResult) obj;
	if (tableID == null) {
	    if (other.tableID != null)
		return false;
	} else if (!tableID.equals(other.tableID))
	    return false;
	return true;
    }

    /**
     * @param tableID
     *            the id of the table that was successfully created.
     * @return a new CreateTableResult representing the successful creation of a
     *         table.
     */
    public static CreateTableResult success(String tableID) {
	return new CreateTableResult(tableID);
    }

    /**
     * @param tableID
     *            the id of the table that was not created.
     * @param reason
     *            the reason the table was not created. Must be
     *            TABLE_ALREADY_EXISTS.
     * @return a new CreateTableResult representing the failed creation of a
     *         table.
     */
    public static CreateTableResult failure(String tableID, FailureReason reason) {
	return new CreateTableResult(tableID, reason);
    }
}
