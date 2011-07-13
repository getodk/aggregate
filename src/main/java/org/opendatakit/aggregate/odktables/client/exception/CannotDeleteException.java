package org.opendatakit.aggregate.odktables.client.exception;

public class CannotDeleteException extends ODKTablesClientException
{
    /**
     * 
     */
    private static final long serialVersionUID = -5845676307084428955L;
    
    private String userUUID;

    public CannotDeleteException(String userUUID)
    {
        super(
                String.format(
                        "Can not delete user with userUUID: %s because they still own or are tracking one or more tables",
                        userUUID));
    }
    
    /**
     * @return the userUUID
     */
    public String getUserUUID()
    {
        return this.userUUID;
    }
}
