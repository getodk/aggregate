package org.opendatakit.aggregate.odktables.client.exception;

public class TableDoesNotExistException extends ODKTablesClientException
{

    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = 4499968035606050096L;

    private String tableID;

    public TableDoesNotExistException(String tableID)
    {
        super(String.format("Table with ID %s does not exist!", tableID));
        this.tableID = tableID;
    }

    public String getTableID()
    {
        return this.tableID;
    }
}
