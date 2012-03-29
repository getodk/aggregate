package org.opendatakit.aggregate.odktables.client.exception;

public class UserAlreadyExistsException extends ODKTablesClientException {
    /**
     * 
     */
    private static final long serialVersionUID = -6626310799740356641L;

    private final String aggregateUserIdentifier;

    public UserAlreadyExistsException(String aggregateUserIdentifier) {
	super(String.format(
		"User with aggregateUserIdentifier %s already exists!",
		aggregateUserIdentifier));
	this.aggregateUserIdentifier = aggregateUserIdentifier;
    }

    public String getAggregateUserIdentifier() {
	return this.aggregateUserIdentifier;
    }
}
