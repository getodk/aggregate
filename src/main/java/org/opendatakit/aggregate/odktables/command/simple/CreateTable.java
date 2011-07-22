package org.opendatakit.aggregate.odktables.command.simple;

import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * CreateTable is immutable.
 *
 * @author the.dylan.price@gmail.com
 */
public class CreateTable implements Command
{
    private static final String path = "/simple/createTable";
    
    private final String tableName;
    private final String requestingUserID;
    private final String tableID;
    private final List<Column> columns;
    

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private CreateTable()
    {
       this.tableName = null;
       this.requestingUserID = null;
       this.tableID = null;
       this.columns = null;
       
    }

    /**
     * Constructs a new CreateTable.
     */
    public CreateTable(String requestingUserID, String tableName, String tableID, List<Column> columns)
    {
        
        Check.notNullOrEmpty(tableName, "tableName");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(columns, "columns"); 
        
        this.tableName = tableName;
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.columns = columns;
    }

    
    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return this.tableName;
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
    
    /**
     * @return the columns
     */
    public List<Column> getColumns()
    {
        return this.columns;
    }
    

    @Override
    public String toString()
    {
        return String.format("CreateTable: " +
                "tableName=%s " +
                "requestingUserID=%s " +
                "tableID=%s " +
                "columns=%s " +
                "", tableName, requestingUserID, tableID, columns);
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

