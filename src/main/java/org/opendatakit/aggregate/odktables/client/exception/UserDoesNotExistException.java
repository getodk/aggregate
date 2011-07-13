package org.opendatakit.aggregate.odktables.client.exception;

public class UserDoesNotExistException extends ODKTablesClientException
{
    /**
     * 
     */
    private static final long serialVersionUID = -6626310799740356641L;

    private final String userID;
    private final String userUUID;

    public UserDoesNotExistException(String userID, String userUUID)
    {
        super("User does not exist!");
        this.userID = userID;
        this.userUUID = userUUID;
    }

    public String getUserID()
    {
        return this.userID;
    }
    
    public String getUserUUID()
    {
        return this.userUUID;
    }
}
