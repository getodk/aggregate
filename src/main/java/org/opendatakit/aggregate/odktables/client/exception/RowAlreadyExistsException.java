package org.opendatakit.aggregate.odktables.client.exception;

public class RowAlreadyExistsException extends ODKTablesClientException
{
    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = 360720642714410573L;

    private String tableId;
    private String rowId;

    public RowAlreadyExistsException(String tableId, String rowId)
    {
        super(String.format("Row with rowId '%s' already exists!", rowId));

        this.tableId = tableId;
        this.rowId = rowId;
    }

    public String getTableId()
    {
        return this.tableId;
    }

    /**
     * @return the rowId of the row that already exists.
     */
    public String getRowId()
    {
        return this.rowId;
    }
}
