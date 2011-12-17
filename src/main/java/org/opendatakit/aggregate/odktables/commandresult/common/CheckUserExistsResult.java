package org.opendatakit.aggregate.odktables.commandresult.common;

import org.opendatakit.aggregate.odktables.command.common.CheckUserExists;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;

/**
 * A CheckUserExistsResult represents the result of executing a CheckUserExists
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CheckUserExistsResult extends CommandResult<CheckUserExists> {
    private final boolean userExists;

    private CheckUserExistsResult() {
	super(true, null);
	this.userExists = false;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private CheckUserExistsResult(boolean userExists) {
	super(true, null);
	this.userExists = userExists;
    }

    /**
     * Retrieve the results from the CheckUserExists command.
     */
    public boolean getUserExists() {
	return userExists;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return String.format("CheckUserExistsResult [userExists=%s]",
		userExists);
    }

    /**
     * @return a new CheckUserExistsResult representing the successful
     *         completion of a CheckUserExists command.
     * 
     */
    public static CheckUserExistsResult success(boolean userExists) {
	return new CheckUserExistsResult(userExists);
    }
}
