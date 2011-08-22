package org.opendatakit.aggregate.odktables.client.exception;

public class PermissionDeniedException extends ODKTablesClientException
{
    /**
     * 
     */
    private static final long serialVersionUID = -6626310799740356641L;

    public PermissionDeniedException()
    {
        super("Requesting user does not have permission for that operation!");
    }
}