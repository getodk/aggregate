package org.opendatakit.aggregate.odktables.command.synchronize;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * CloneSynchronizedTable is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class CloneSynchronizedTable implements Command
{
    private static final String path = "/odktables/synchronize/cloneSynchronizedTable";
    
    private final String tableID;
    private final String requestingUserID;
    private final String tableUUID;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private CloneSynchronizedTable()
    {
       this.tableID = null;
       this.requestingUserID = null;
       this.tableUUID = null;
       
    }

    /**
     * Constructs a new CloneSynchronizedTable.
     */
    public CloneSynchronizedTable(String tableID, String requestingUserID, String tableUUID)
    {
        
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableUUID, "tableUUID"); 
        
        this.tableID = tableID;
        this.requestingUserID = requestingUserID;
        this.tableUUID = tableUUID;
    }

    
    /**
     * @return the tableID
     */
    public String getTableID()
    {
        return this.tableID;
    }
    
    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }
    
    /**
     * @return the tableUUID
     */
    public String getTableUUID()
    {
        return this.tableUUID;
    }
    

    @Override
    public String toString()
    {
        return String.format("CloneSynchronizedTable: " +
                "tableID=%s " +
                "requestingUserID=%s " +
                "tableUUID=%s " +
                "", tableID, requestingUserID, tableUUID);
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

