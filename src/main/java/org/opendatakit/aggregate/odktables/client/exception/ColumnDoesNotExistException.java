package org.opendatakit.aggregate.odktables.client.exception;

public class ColumnDoesNotExistException extends ODKTablesClientException
{
    private static final long serialVersionUID = 6072347922175962290L;

    private final String tableID;
    private final String badColumnName;

    public ColumnDoesNotExistException(String tableID, String badColumnName)
    {
        super(String.format(
                "Column with name %s does not exist in table with tableID: %s",
                badColumnName, tableID));
        this.tableID = tableID;
        this.badColumnName = badColumnName;
    }

    /**
     * @return the tableID
     */
    public String getTableID()
    {
        return tableID;
    }

    /**
     * @return the badColumnName
     */
    public String getBadColumnName()
    {
        return badColumnName;
    }
}
