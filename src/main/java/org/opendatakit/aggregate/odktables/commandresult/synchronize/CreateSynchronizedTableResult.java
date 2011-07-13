package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import org.opendatakit.aggregate.odktables.command.common.CreateSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A CreateSynchronizedTableResult represents the result of executing a CreateSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateSynchronizedTableResult extends CommandResult<CreateSynchronizedTable>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
    }

    
    private final String requestingUserID;
    private final String tableID;

    private CreateSynchronizedTableResult()
    {
       super(true, null);
       this.requestingUserID = null;
       this.tableID = null;
       
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private CreateSynchronizedTableResult(String requestingUserID, String tableID)
    {
        super(true, null);

        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID"); 
        
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private CreateSynchronizedTableResult(String requestingUserID, String tableID, FailureReason reason)
    {
        super(false, reason);

        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID"); 
        if (!this.possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(String.format("Failure reason %s not a valid failure reason for CreateSynchronizedTable.", reason));
        
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
    }

    /**
     * Retrieve the results from the CreateSynchronizedTable command.
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
        return String.format("CreateSynchronizedTableResult: " +
                "requestingUserID=%s " +
                "tableID=%s " +
                "", requestingUserID, tableID);
    }


    /**
     * TODO
     * @return a new CreateSynchronizedTableResult representing the successful 
     * 
     */
    public static CreateSynchronizedTableResult success()
    {
        return new CreateSynchronizedTableResult();
    }

    /**
     * TODO
     * @return a new CreateSynchronizedTableResult representing the failed      
     */
    public static CreateSynchronizedTableResult failure(FailureReason reason)
    {
        return new CreateSynchronizedTableResult(reason);
    }
}
