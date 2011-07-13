package org.opendatakit.aggregate.odktables.client.exception;

public class TableDoesNotExistException extends ODKTablesClientException
{

    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = 4499968035606050096L;

    private String tableID;
    private String tableUUID;

    public TableDoesNotExistException(String tableID, String tableUUID)
    {
        super(String.format("Table does not exist!"));
        this.tableID = tableID;
        this.tableUUID = tableUUID;
    }

    public String getTableID()
    {
        return this.tableID;
    }
    
    public String getTableUUID()
    {
        return this.tableUUID;
    }
}
