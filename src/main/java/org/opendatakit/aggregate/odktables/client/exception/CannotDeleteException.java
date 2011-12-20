package org.opendatakit.aggregate.odktables.client.exception;

public class CannotDeleteException extends ODKTablesClientException {
    /**
     * 
     */
    private static final long serialVersionUID = -5845676307084428955L;

    private String aggregateUserIdentifier;

    public CannotDeleteException(String aggregateUserIdentifier) {
	super(
		String.format(
			"Can not delete user with aggregateUserIdentifier: %s because they still own or are tracking one or more tables",
			aggregateUserIdentifier));
    }

    /**
     * @return the aggregateUserIdentifier
     */
    public String getAggregateUserIdentifier() {
	return this.aggregateUserIdentifier;
    }
}
