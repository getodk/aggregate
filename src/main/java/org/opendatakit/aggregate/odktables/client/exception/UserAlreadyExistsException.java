package org.opendatakit.aggregate.odktables.client.exception;

public class UserAlreadyExistsException extends ODKTablesClientException
{
    /**
     * 
     */
    private static final long serialVersionUID = -6626310799740356641L;
    
    private final String userId;

    public UserAlreadyExistsException(String userId)
    {
        super(String.format("User with userId %s already exists!", userId));
        this.userId = userId;
    }

    public String getUserId()
    {
        return this.userId;
    }
}
