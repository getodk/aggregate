package org.opendatakit.aggregate.odktables.command.synchronize;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * CreateSynchronizedTable is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class CreateSynchronizedTable implements Command
{
    private static final String path = "/odktables/synchronize/createSynchronizedTable";
    
    private final String requestingUserID;
    private final String tableID;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private CreateSynchronizedTable()
    {
       this.requestingUserID = null;
       this.tableID = null;
       
    }

    /**
     * Constructs a new CreateSynchronizedTable.
     */
    public CreateSynchronizedTable(String requestingUserID, String tableID)
    {
        
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID"); 
        
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
    }

    
    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }
    
    /**
     * @return the tableID
     */
    public String getTableID()
    {
        return this.tableID;
    }
    

    @Override
    public String toString()
    {
        return String.format("CreateSynchronizedTable: " +
                "requestingUserID=%s " +
                "tableID=%s " +
                "", requestingUserID, tableID);
    }

    @Override
    public String getMethodPath()
    {
        return methodPath();
    }

    /**
     * @return the path of this Command relative to the address of an Aggregate
     *         instance. For example, if the full path to a command is
     *         http://aggregate.opendatakit.org/odktables/createTable, then this
     *         method would return '/odktables/createTable'.
     */
    public static String methodPath()
    {
        return path;
    }
}

