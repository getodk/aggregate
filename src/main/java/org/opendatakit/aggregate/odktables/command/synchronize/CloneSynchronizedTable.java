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
    private static final String path = "/synchronize/cloneSynchronizedTable";
    
    private final String tableID;
    private final String requestingUserID;
    private final String aggregateTableIdentifier;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private CloneSynchronizedTable()
    {
       this.tableID = null;
       this.requestingUserID = null;
       this.aggregateTableIdentifier = null;
       
    }

    /**
     * Constructs a new CloneSynchronizedTable.
     */
    public CloneSynchronizedTable(String requestingUserID, String tableID, String aggregateTableIdentifier)
    {
        
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(aggregateTableIdentifier, "aggregateTableIdentifier"); 
        
        this.tableID = tableID;
        this.requestingUserID = requestingUserID;
        this.aggregateTableIdentifier = aggregateTableIdentifier;
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
     * @return the aggregateTableIdentifier
     */
    public String getAggregateTableIdentifier()
    {
        return this.aggregateTableIdentifier;
    }
    

    @Override
    public String toString()
    {
        return String.format("CloneSynchronizedTable: " +
                "tableID=%s " +
                "requestingUserID=%s " +
                "aggregateTableIdentifier=%s " +
                "", tableID, requestingUserID, aggregateTableIdentifier);
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

