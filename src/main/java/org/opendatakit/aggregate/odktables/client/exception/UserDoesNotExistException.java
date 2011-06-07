package org.opendatakit.aggregate.odktables.client.exception;

public class UserDoesNotExistException extends ODKTablesClientException
{
    /**
     * 
     */
    private static final long serialVersionUID = -6626310799740356641L;

    private final String userId;

    public UserDoesNotExistException(String userId)
    {
        super(String.format("User with userId %s does not exist!", userId));
        this.userId = userId;
    }

    public String getUserId()
    {
        return this.userId;
    }
}
