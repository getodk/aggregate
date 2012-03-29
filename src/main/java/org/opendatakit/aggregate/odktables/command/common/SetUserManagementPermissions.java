package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * SetUserManagementPermissions is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SetUserManagementPermissions implements Command {
    private static final String path = "/common/setUserManagementPermissions";

    private final String requestingUserID;
    private final String aggregateUserIdentifier;
    private final boolean allowed;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private SetUserManagementPermissions() {
	this.requestingUserID = null;
	this.aggregateUserIdentifier = null;
	this.allowed = false;

    }

    /**
     * Constructs a new SetUserManagementPermissions.
     */
    public SetUserManagementPermissions(String requestingUserID,
	    String aggregateUserIdentifier, boolean allowed) {

	Check.notNullOrEmpty(requestingUserID, "requestingUserID");
	Check.notNullOrEmpty(aggregateUserIdentifier, "aggregateUserIdentifier");
	Check.notNull(allowed, "allowed");

	this.requestingUserID = requestingUserID;
	this.aggregateUserIdentifier = aggregateUserIdentifier;
	this.allowed = allowed;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID() {
	return this.requestingUserID;
    }

    /**
     * @return the aggregateUserIdentifier
     */
    public String getAggregateUserIdentifier() {
	return this.aggregateUserIdentifier;
    }

    /**
     * @return the allowed
     */
    public boolean getAllowed() {
	return this.allowed;
    }

    @Override
    public String toString() {
	return String.format("SetUserManagementPermissions: "
		+ "requestingUserID=%s " + "aggregateUserIdentifier=%s "
		+ "allowed=%s " + "", requestingUserID,
		aggregateUserIdentifier, allowed);
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
