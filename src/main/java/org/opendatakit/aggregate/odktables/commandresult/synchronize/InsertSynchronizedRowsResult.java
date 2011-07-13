package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import org.opendatakit.aggregate.odktables.command.common.InsertSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A InsertSynchronizedRowsResult represents the result of executing a InsertSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertSynchronizedRowsResult extends CommandResult<InsertSynchronizedRows>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
    }

    
    private final String requestingUserID;
    private final String tableID;
    private final int modificationNumber;
    private final List<Row> newRows;

    private InsertSynchronizedRowsResult()
    {
       super(true, null);
       this.requestingUserID = null;
       this.tableID = null;
       this.modificationNumber = null;
       this.newRows = null;
       
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private InsertSynchronizedRowsResult(String requestingUserID, String tableID, int modificationNumber, List<Row> newRows)
    {
        super(true, null);

        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber");
        Check.notNull(newRows, "newRows"); 
        
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
        this.newRows = newRows;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private InsertSynchronizedRowsResult(String requestingUserID, String tableID, int modificationNumber, List<Row> newRows, FailureReason reason)
    {
        super(false, reason);

        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber");
        Check.notNull(newRows, "newRows"); 
        if (!this.possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(String.format("Failure reason %s not a valid failure reason for InsertSynchronizedRows.", reason));
        
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
        this.newRows = newRows;
    }

    /**
     * Retrieve the results from the InsertSynchronizedRows command.
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
        return String.format("InsertSynchronizedRowsResult: " +
                "requestingUserID=%s " +
                "tableID=%s " +
                "modificationNumber=%s " +
                "newRows=%s " +
                "", requestingUserID, tableID, modificationNumber, newRows);
    }


    /**
     * TODO
     * @return a new InsertSynchronizedRowsResult representing the successful 
     * 
     */
    public static InsertSynchronizedRowsResult success()
    {
        return new InsertSynchronizedRowsResult();
    }

    /**
     * TODO
     * @return a new InsertSynchronizedRowsResult representing the failed      
     */
    public static InsertSynchronizedRowsResult failure(FailureReason reason)
    {
        return new InsertSynchronizedRowsResult(reason);
    }
}
