package org.opendatakit.aggregate.odktables.client.exception;

public class UserDoesNotExistException extends ODKTablesClientException
{
    /**
     * 
     */
    private static final long serialVersionUID = -6626310799740356641L;

    private final String aggregateUserIdentifier;

    public UserDoesNotExistException(String aggregateUserIdentifier)
    {
        super(String.format(
                "User with aggregateUserIdentifier %s does not exist!",
                aggregateUserIdentifier));
        this.aggregateUserIdentifier = aggregateUserIdentifier;
    }

    public String getAggregateUserIdentifier()
    {
        return this.aggregateUserIdentifier;
    }
}
