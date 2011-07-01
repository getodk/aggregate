package org.opendatakit.aggregate.odktables.client.exception;

public class UserAlreadyExistsException extends ODKTablesClientException
{
    /**
     * 
     */
    private static final long serialVersionUID = -6626310799740356641L;
    
    private final String userID;

    public UserAlreadyExistsException(String userID)
    {
        super(String.format("User with userID %s already exists!", userID));
        this.userID = userID;
    }

    public String getUserID()
    {
        return this.userID;
    }
}
