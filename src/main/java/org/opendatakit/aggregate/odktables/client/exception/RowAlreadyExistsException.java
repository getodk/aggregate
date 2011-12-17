package org.opendatakit.aggregate.odktables.client.exception;

public class RowAlreadyExistsException extends ODKTablesClientException {
    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = 360720642714410573L;

    private String tableID;
    private String rowID;

    public RowAlreadyExistsException(String tableID, String rowID) {
	super(String.format("Row with rowID '%s' already exists!", rowID));

	this.tableID = tableID;
	this.rowID = rowID;
    }

    public String getTableID() {
	return this.tableID;
    }

    /**
     * @return the rowID of the row that already exists.
     */
    public String getRowID() {
	return this.rowID;
    }
}
