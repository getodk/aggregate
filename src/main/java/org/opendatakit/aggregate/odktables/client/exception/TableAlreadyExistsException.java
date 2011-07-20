package org.opendatakit.aggregate.odktables.client.exception;

/**
 * An exception for when an attempt to create a table is made but that table
 * already exists.
 * 
 * @author the.dylan.price@gmail.com
 */
public class TableAlreadyExistsException extends ODKTablesClientException
{
    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = -9027773843183177346L;

    private String tableID;
    
    public TableAlreadyExistsException(String tableID)
    {
        super(String.format("Table with tableID '%s' already exists!", tableID));

        this.tableID = tableID;
    }
    
    public String getTableID()
    {
        return this.tableID;
    }
}
