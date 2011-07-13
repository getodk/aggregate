package org.opendatakit.aggregate.odktables.command.simple;

import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * InsertRows is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class InsertRows implements Command
{
    private static final String path = "/odktables/simple/insertRows";
    
    private final List<Row> rows;
    private final String requestingUserID;
    private final String tableUUID;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private InsertRows()
    {
       this.rows = null;
       this.requestingUserID = null;
       this.tableUUID = null;
       
    }

    /**
     * Constructs a new InsertRows.
     */
    public InsertRows(List<Row> rows, String requestingUserID, String tableUUID)
    {
        
        Check.notNull(rows, "rows");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableUUID, "tableUUID"); 
        
        this.rows = rows;
        this.requestingUserID = requestingUserID;
        this.tableUUID = tableUUID;
    }

    
    /**
     * @return the rows
     */
    public List<Row> getRows()
    {
        return this.rows;
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
        return String.format("InsertRows: " +
                "rows=%s " +
                "requestingUserID=%s " +
                "tableUUID=%s " +
                "", rows, requestingUserID, tableUUID);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((requestingUserID == null) ? 0 : requestingUserID.hashCode());
        result = prime * result + ((rows == null) ? 0 : rows.hashCode());
        result = prime * result
                + ((tableUUID == null) ? 0 : tableUUID.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof InsertRows))
            return false;
        InsertRows other = (InsertRows) obj;
        if (requestingUserID == null)
        {
            if (other.requestingUserID != null)
                return false;
        } else if (!requestingUserID.equals(other.requestingUserID))
            return false;
        if (rows == null)
        {
            if (other.rows != null)
                return false;
        } else if (!rows.equals(other.rows))
            return false;
        if (tableUUID == null)
        {
            if (other.tableUUID != null)
                return false;
        } else if (!tableUUID.equals(other.tableUUID))
            return false;
        return true;
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

