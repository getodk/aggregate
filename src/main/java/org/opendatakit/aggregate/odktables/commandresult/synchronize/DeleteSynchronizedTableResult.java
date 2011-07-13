package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import org.opendatakit.aggregate.odktables.command.common.DeleteSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A DeleteSynchronizedTableResult represents the result of executing a DeleteSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteSynchronizedTableResult extends CommandResult<DeleteSynchronizedTable>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
    }

    
    private final String requestingUserID;
    private final String tableUUID;

    private DeleteSynchronizedTableResult()
    {
       super(true, null);
       this.requestingUserID = null;
       this.tableUUID = null;
       
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private DeleteSynchronizedTableResult(String requestingUserID, String tableUUID)
    {
        super(true, null);

        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableUUID, "tableUUID"); 
        
        this.requestingUserID = requestingUserID;
        this.tableUUID = tableUUID;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private DeleteSynchronizedTableResult(String requestingUserID, String tableUUID, FailureReason reason)
    {
        super(false, reason);

        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableUUID, "tableUUID"); 
        if (!this.possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(String.format("Failure reason %s not a valid failure reason for DeleteSynchronizedTable.", reason));
        
        this.requestingUserID = requestingUserID;
        this.tableUUID = tableUUID;
    }

    /**
     * Retrieve the results from the DeleteSynchronizedTable command.
     */
    public void get() throws 
            PermissionDeniedException
    {
        if (successful())
        {
            throw new RuntimeException("not implemented");
        } else
        {
            switch (getReason())
            {
            case PERMISSION_DENIED:
                throw new PermissionDeniedException();
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("DeleteSynchronizedTableResult: " +
                "requestingUserID=%s " +
                "tableUUID=%s " +
                "", requestingUserID, tableUUID);
    }


    /**
     * TODO
     * @return a new DeleteSynchronizedTableResult representing the successful 
     * 
     */
    public static DeleteSynchronizedTableResult success()
    {
        return new DeleteSynchronizedTableResult();
    }

    /**
     * TODO
     * @return a new DeleteSynchronizedTableResult representing the failed      
     */
    public static DeleteSynchronizedTableResult failure(FailureReason reason)
    {
        return new DeleteSynchronizedTableResult(reason);
    }
}
