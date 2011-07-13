package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import org.opendatakit.aggregate.odktables.command.common.CloneSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A CloneSynchronizedTableResult represents the result of executing a CloneSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CloneSynchronizedTableResult extends CommandResult<CloneSynchronizedTable>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
    }

    
    private final String tableID;
    private final String requestingUserID;
    private final String tableUUID;

    private CloneSynchronizedTableResult()
    {
       super(true, null);
       this.tableID = null;
       this.requestingUserID = null;
       this.tableUUID = null;
       
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private CloneSynchronizedTableResult(String tableID, String requestingUserID, String tableUUID)
    {
        super(true, null);

        
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableUUID, "tableUUID"); 
        
        this.tableID = tableID;
        this.requestingUserID = requestingUserID;
        this.tableUUID = tableUUID;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private CloneSynchronizedTableResult(String tableID, String requestingUserID, String tableUUID, FailureReason reason)
    {
        super(false, reason);

        
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableUUID, "tableUUID"); 
        if (!this.possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(String.format("Failure reason %s not a valid failure reason for CloneSynchronizedTable.", reason));
        
        this.tableID = tableID;
        this.requestingUserID = requestingUserID;
        this.tableUUID = tableUUID;
    }

    /**
     * Retrieve the results from the CloneSynchronizedTable command.
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
        return String.format("CloneSynchronizedTableResult: " +
                "tableID=%s " +
                "requestingUserID=%s " +
                "tableUUID=%s " +
                "", tableID, requestingUserID, tableUUID);
    }


    /**
     * TODO
     * @return a new CloneSynchronizedTableResult representing the successful 
     * 
     */
    public static CloneSynchronizedTableResult success()
    {
        return new CloneSynchronizedTableResult();
    }

    /**
     * TODO
     * @return a new CloneSynchronizedTableResult representing the failed      
     */
    public static CloneSynchronizedTableResult failure(FailureReason reason)
    {
        return new CloneSynchronizedTableResult(reason);
    }
}
