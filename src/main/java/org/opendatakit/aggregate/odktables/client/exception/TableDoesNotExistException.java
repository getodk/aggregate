package org.opendatakit.aggregate.odktables.client.exception;

public class TableDoesNotExistException extends ODKTablesClientException
{

    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = 4499968035606050096L;

    private String tableId;

    public TableDoesNotExistException(String tableId)
    {
        super(String.format("Table with tableId '%s' does not exist!", tableId));
        this.tableId = tableId;
    }

    public String getTableId()
    {
        return this.tableId;
    }
}
