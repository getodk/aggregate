package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import org.opendatakit.aggregate.odktables.command.common.Synchronize;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A SynchronizeResult represents the result of executing a Synchronize command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SynchronizeResult extends CommandResult<Synchronize>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
    }

    
    private final String requestingUserID;
    private final String tableID;
    private final int modificationNumber;

    private SynchronizeResult()
    {
       super(true, null);
       this.requestingUserID = null;
       this.tableID = null;
       this.modificationNumber = null;
       
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private SynchronizeResult(String requestingUserID, String tableID, int modificationNumber)
    {
        super(true, null);

        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber"); 
        
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private SynchronizeResult(String requestingUserID, String tableID, int modificationNumber, FailureReason reason)
    {
        super(false, reason);

        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber"); 
        if (!this.possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(String.format("Failure reason %s not a valid failure reason for Synchronize.", reason));
        
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
    }

    /**
     * Retrieve the results from the Synchronize command.
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
        return String.format("SynchronizeResult: " +
                "requestingUserID=%s " +
                "tableID=%s " +
                "modificationNumber=%s " +
                "", requestingUserID, tableID, modificationNumber);
    }


    /**
     * TODO
     * @return a new SynchronizeResult representing the successful 
     * 
     */
    public static SynchronizeResult success()
    {
        return new SynchronizeResult();
    }

    /**
     * TODO
     * @return a new SynchronizeResult representing the failed      
     */
    public static SynchronizeResult failure(FailureReason reason)
    {
        return new SynchronizeResult(reason);
    }
}
