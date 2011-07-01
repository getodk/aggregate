package org.opendatakit.aggregate.odktables.client.exception;

public class UserDoesNotExistException extends ODKTablesClientException
{
    /**
     * 
     */
    private static final long serialVersionUID = -6626310799740356641L;

    private final String userID;

    public UserDoesNotExistException(String userID)
    {
        super(String.format("User with userID %s does not exist!", userID));
        this.userID = userID;
    }

    public String getUserID()
    {
        return this.userID;
    }
}
