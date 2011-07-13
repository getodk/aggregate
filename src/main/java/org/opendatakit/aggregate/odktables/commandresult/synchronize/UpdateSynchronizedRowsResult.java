package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import org.opendatakit.aggregate.odktables.command.common.UpdateSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A UpdateSynchronizedRowsResult represents the result of executing a UpdateSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class UpdateSynchronizedRowsResult extends CommandResult<UpdateSynchronizedRows>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
    }

    
    private final List<SynchronizedRow> changedRows;
    private final String requestingUserID;
    private final String tableID;
    private final int modificationNumber;

    private UpdateSynchronizedRowsResult()
    {
       super(true, null);
       this.changedRows = null;
       this.requestingUserID = null;
       this.tableID = null;
       this.modificationNumber = null;
       
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private UpdateSynchronizedRowsResult(List<SynchronizedRow> changedRows, String requestingUserID, String tableID, int modificationNumber)
    {
        super(true, null);

        
        Check.notNull(changedRows, "changedRows");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber"); 
        
        this.changedRows = changedRows;
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private UpdateSynchronizedRowsResult(List<SynchronizedRow> changedRows, String requestingUserID, String tableID, int modificationNumber, FailureReason reason)
    {
        super(false, reason);

        
        Check.notNull(changedRows, "changedRows");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber"); 
        if (!this.possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(String.format("Failure reason %s not a valid failure reason for UpdateSynchronizedRows.", reason));
        
        this.changedRows = changedRows;
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
    }

    /**
     * Retrieve the results from the UpdateSynchronizedRows command.
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
        return String.format("UpdateSynchronizedRowsResult: " +
                "changedRows=%s " +
                "requestingUserID=%s " +
                "tableID=%s " +
                "modificationNumber=%s " +
                "", changedRows, requestingUserID, tableID, modificationNumber);
    }


    /**
     * TODO
     * @return a new UpdateSynchronizedRowsResult representing the successful 
     * 
     */
    public static UpdateSynchronizedRowsResult success()
    {
        return new UpdateSynchronizedRowsResult();
    }

    /**
     * TODO
     * @return a new UpdateSynchronizedRowsResult representing the failed      
     */
    public static UpdateSynchronizedRowsResult failure(FailureReason reason)
    {
        return new UpdateSynchronizedRowsResult(reason);
    }
}
