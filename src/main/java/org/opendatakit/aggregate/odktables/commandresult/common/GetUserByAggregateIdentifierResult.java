package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.GetUserByAggregateIdentifier;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A GetUserByAggregate IdentifierResult represents the result of executing a
 * GetUserByAggregate Identifier command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetUserByAggregateIdentifierResult extends
	CommandResult<GetUserByAggregateIdentifier> {
    private static final List<FailureReason> possibleFailureReasons;
    static {
	possibleFailureReasons = new ArrayList<FailureReason>();
	possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
	possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final User user;
    private final String aggregateUserIdentifier;

    private GetUserByAggregateIdentifierResult() {
	super(true, null);
	this.user = null;
	this.aggregateUserIdentifier = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private GetUserByAggregateIdentifierResult(User user) {
	super(true, null);
	Check.notNull(user, "user");
	this.user = user;
	this.aggregateUserIdentifier = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private GetUserByAggregateIdentifierResult(String aggregateUserIdentifier,
	    FailureReason reason) {
	super(false, reason);

	Check.notNullOrEmpty(aggregateUserIdentifier, "aggregateUserIdentifier");
	Check.notNull(reason, "reason");

	if (!possibleFailureReasons.contains(reason)) {
	    throw new IllegalArgumentException(
		    String.format(
			    "Failure reason %s not a valid failure reason for GetUserByAggregate Identifier.",
			    reason));
	}
	this.user = null;
	this.aggregateUserIdentifier = aggregateUserIdentifier;
    }

    /**
     * Retrieve the results from the GetUserByAggregate Identifier command.
     * 
     * @return the user requested
     */
    public User getUser() throws PermissionDeniedException,
	    UserDoesNotExistException {
	if (successful()) {
	    return this.user;
	} else {
	    switch (getReason()) {
	    case USER_DOES_NOT_EXIST:
		throw new UserDoesNotExistException(
			this.aggregateUserIdentifier);
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
	return String.format("GetUserByAggregate IdentifierResult [user=%s]",
		user);
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
	result = prime * result + ((user == null) ? 0 : user.hashCode());
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
	if (!(obj instanceof GetUserByAggregateIdentifierResult))
	    return false;
	GetUserByAggregateIdentifierResult other = (GetUserByAggregateIdentifierResult) obj;
	if (user == null) {
	    if (other.user != null)
		return false;
	} else if (!user.equals(other.user))
	    return false;
	return true;
    }

    /**
     * @param user
     *            the user retrieved
     * @return a new GetUserByAggregate IdentifierResult representing the
     *         successful completion of a GetUserByAggregate Identifier command.
     * 
     */
    public static GetUserByAggregateIdentifierResult success(User user) {
	return new GetUserByAggregateIdentifierResult(user);
    }

    /**
     * @param aggregateUserIdentifier
     *            the Aggregate Identifier of the user who failed to be
     *            retrieved
     * @param reason
     *            the reason the command failed. Must be either
     *            USER_DOES_NOT_EXIST or PERMISSION_DENIED.
     * @return a new GetUserByAggregate IdentifierResult representing the failed
     *         GetUserByAggregate Identifier command.
     */
    public static GetUserByAggregateIdentifierResult failure(
	    String aggregateUserIdentifier, FailureReason reason) {
	return new GetUserByAggregateIdentifierResult(aggregateUserIdentifier,
		reason);
    }
}