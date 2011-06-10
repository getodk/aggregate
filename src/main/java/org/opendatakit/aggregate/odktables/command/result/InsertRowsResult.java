package org.opendatakit.aggregate.odktables.command.result;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.CommandResult;
import org.opendatakit.aggregate.odktables.client.exception.RowAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.InsertRows;

/**
 * An InsertRowsResult represents the result of executing and insertRows
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertRowsResult extends CommandResult<InsertRows>
{

    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.ROW_ALREADY_EXISTS);
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
    }

    private final String tableId;
    private final List<String> rowIds;
    private final String failedRowId;

    /**
     * For serialization by Gson we need a no-arg constructor
     */
    private InsertRowsResult()
    {
        super(true, null);
        tableId = "0";
        rowIds = null;
        failedRowId = null;
    }

    /**
     * The success constructor. Constructs a successful InsertRowsResult. See
     * {@link #success(String, List)} for param info.
     */
    private InsertRowsResult(String tableId, List<String> rowIds)
    {
        super(true, null);
        if (rowIds == null || rowIds.size() == 0)
        {
            throw new IllegalArgumentException(
                    "rowIds can not be null or empty");
        }
        if (tableId == null || tableId.length() == 0)
        {
            throw new IllegalArgumentException(
                    "Cant not have a null or empty tableId");
        }

        this.tableId = tableId;
        this.rowIds = rowIds;
        this.failedRowId = null;
    }

    /**
     * The failure constructor. Constructs a failure InsertRowsResult. See
     * {@link #failure(String)} and {@link #failure(String, String)} for param
     * info.
     */
    private InsertRowsResult(String tableId, String failedRowId,
            FailureReason reason)
    {
        super(false, reason);
        if (!possibleFailureReasons.contains(getReason()))
        {
            throw new IllegalArgumentException("Not a valid FailureReason: "
                    + getReason());
        }
        if (tableId == null || tableId.length() == 0)
        {
            throw new IllegalArgumentException(
                    "Cant not have a null or empty tableId");
        }

        this.tableId = tableId;
        this.rowIds = null;
        this.failedRowId = failedRowId;
    }

    /**
     * Retrieve the results from the insertRows command.
     * 
     * @return a list of rowIds that represent the successfully inserted rows
     * @throws RowAlreadyExistsException
     *             if one of the rows that the insertRows command tried to
     *             insert already existed
     * @throws TableDoesNotExistException
     *             if the table that the insertRows command tried to insert to
     *             did not exist
     */
    public List<String> getInsertedRowIds() throws RowAlreadyExistsException,
            TableDoesNotExistException
    {
        if (successful())
        {
            return this.rowIds;
        } else
        {
            switch (getReason())
            {
            case ROW_ALREADY_EXISTS:
                throw new RowAlreadyExistsException(getTableId(),
                        this.failedRowId);
            case TABLE_DOES_NOT_EXIST:
                throw new TableDoesNotExistException(getTableId());
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /**
     * @return the tableId associated with this result.
     */
    public String getTableId()
    {
        return this.tableId;
    }

    @Override
    public String toString()
    {
        return String.format(
                "{%s, tableId = %s, rowIds = %s, failedRowIds = %s",
                super.toString(), this.tableId, this.rowIds, this.failedRowId);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof InsertRowsResult))
            return false;
        if (!super.equals(obj))
            return false;

        InsertRowsResult o = (InsertRowsResult) obj;
        boolean tableIdsEqual = o.tableId.equals(this.tableId);
        boolean rowIdsEqual = (o.rowIds == null && this.rowIds == null)
                || o.rowIds.equals(this.rowIds);
        boolean failedRowIdsEqual = (o.failedRowId == null && this.failedRowId == null)
                || o.failedRowId.equals(this.failedRowId);
        return tableIdsEqual && rowIdsEqual && failedRowIdsEqual;
    }

    @Override
    public int hashCode()
    {
        int rowIdsHash = (this.rowIds == null) ? 1 : this.rowIds.hashCode();
        int failedRowIdHash = (this.failedRowId == null) ? 1 : this.failedRowId
                .hashCode();
        return super.hashCode() + 69 * this.tableId.hashCode() + 37
                * rowIdsHash + 22 * failedRowIdHash;
    }

    /**
     * Returns a new, successful InsertRowsResult.
     * 
     * @param tableId
     *            the unique identifier of the table that the insertRows command
     *            dealt with. Must not be null or empty.
     * @param rowIds
     *            a list of the rowIds corresponding to the rows that were
     *            successfully inserted. Must not be null or empty.
     * 
     * @return a new InsertRowsResult which represents the successful outcome of
     *         an insertRows command.
     */
    public static InsertRowsResult success(String tableId, List<String> rowIds)
    {
        return new InsertRowsResult(tableId, rowIds);
    }

    /**
     * Returns a new, failed InsertRowsResult which failed because a row already
     * exists.
     * 
     * @param tableId
     *            the unique identifier of the table that the insertRows command
     *            dealt with. Must not be null or empty.
     * @param failedRowId
     *            the rowId of the row that failed to be inserted. Must not be
     *            null or empty.
     * @return a new InsertRowsResult which represents the failed outcome of an
     *         insertRows command.
     */
    public static InsertRowsResult failure(String tableId, String failedRowId)
    {
        if (failedRowId == null || failedRowId.length() == 0)
        {
            throw new IllegalArgumentException(
                    "failedRowId can not be null or empty");
        }
        return new InsertRowsResult(tableId, failedRowId,
                FailureReason.ROW_ALREADY_EXISTS);
    }

    /**
     * Returns a new, failed InsertRowsResult which failed because the table
     * does not exist.
     * 
     * @param tableId
     *            the unique identifier of the table the did not exist. Must not
     *            be null or empty.
     * @return a new InsertRowsResult which represents the failed outcome of an
     *         insertRows command.
     */
    public static InsertRowsResult failure(String tableId)
    {
        return new InsertRowsResult(tableId, null,
                FailureReason.TABLE_DOES_NOT_EXIST);
    }
}
