package org.opendatakit.aggregate.odktables.commandresult.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

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
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String tableID;
    private final Map<String, String> rowIDToaggregateRowIdentifier;

    /**
     * For serialization by Gson we need a no-arg constructor
     */
    private InsertRowsResult()
    {
        super(true, null);
        this.tableID = null;
        this.rowIDToaggregateRowIdentifier = null;
    }

    /**
     * The success constructor. Constructs a successful InsertRowsResult. See
     * {@link #success(String, List)} for param info.
     */
    private InsertRowsResult(Map<String, String> rowIDToaggregateRowIdentifier)
    {
        super(true, null);
        Check.notNull(rowIDToaggregateRowIdentifier,
                "rowIDtoaggregateRowIdentifier");

        this.tableID = null;
        this.rowIDToaggregateRowIdentifier = rowIDToaggregateRowIdentifier;
    }

    /**
     * The failure constructor. Constructs a failure InsertRowsResult. See
     * {@link #failure(String)} and {@link #failure(String, String)} for param
     * info.
     */
    private InsertRowsResult(String tableID,
            FailureReason reason)
    {
        super(false, reason);
        Check.notNullOrEmpty(tableID,
                "tableID");
        if (!possibleFailureReasons.contains(getReason()))
        {
            throw new IllegalArgumentException("Not a valid FailureReason: "
                    + getReason());
        }

        this.tableID = tableID;
        this.rowIDToaggregateRowIdentifier = null;
    }

    /**
     * Retrieve the results from the insertRows command.
     * 
     * @return a map of rowIDs to aggregateRowIdentifiers for the successfully
     *         inserted rows
     * @throws TableDoesNotExistException
     *             if the table that the insertRows command tried to insert to
     *             did not exist
     * @throws PermissionDeniedException
     */
    public Map<String, String> getMapOfInsertedRowIDsToAggregateRowIdentifiers()
            throws TableDoesNotExistException, PermissionDeniedException
    {
        if (successful())
        {
            return this.rowIDToaggregateRowIdentifier;
        } else
        {
            switch (getReason())
            {
            case TABLE_DOES_NOT_EXIST:
                throw new TableDoesNotExistException(tableID);
            case PERMISSION_DENIED:
                throw new PermissionDeniedException();
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String
                .format("InsertRowsResult [tableID=%s, rowIDToaggregateRowIdentifier=%s]",
                        tableID, rowIDToaggregateRowIdentifier);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof InsertRowsResult))
            return false;
        InsertRowsResult other = (InsertRowsResult) obj;
        if (rowIDToaggregateRowIdentifier == null)
        {
            if (other.rowIDToaggregateRowIdentifier != null)
                return false;
        } else if (!rowIDToaggregateRowIdentifier
                .equals(other.rowIDToaggregateRowIdentifier))
            return false;
        if (tableID == null)
        {
            if (other.tableID != null)
                return false;
        } else if (!tableID
                .equals(other.tableID))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((rowIDToaggregateRowIdentifier == null) ? 0
                        : rowIDToaggregateRowIdentifier.hashCode());
        result = prime
                * result
                + ((tableID == null) ? 0
                        : tableID.hashCode());
        return result;
    }

    /**
     * Returns a new, successful InsertRowsResult.
     * 
     * @param rowIDstoaggregateRowIdentifiers
     *            a map of successfully inserted rowIDs to their corresponding
     *            aggregateRowIdentifiers.
     * 
     * @return a new InsertRowsResult which represents the successful outcome of
     *         an insertRows command.
     */
    public static InsertRowsResult success(
            Map<String, String> rowIDstoaggregateRowIdentifiers)
    {
        return new InsertRowsResult(rowIDstoaggregateRowIdentifiers);
    }

    /**
     * Returns a new, failed InsertRowsResult
     * 
     * @param tableID
     *            the client's identifier for the table that the insertRows command
     *            dealt with. Must not be null or empty.
     * @param reason
     *            the reason the command failed. Must be either
     *            TABLE_DOES_NOT_EXIST or PERMISSION_DENIED.
     * @return a new InsertRowsResult which represents the failed outcome of an
     *         insertRows command.
     */
    public static InsertRowsResult failure(String tableID,
            FailureReason reason)
    {
        return new InsertRowsResult(tableID, reason);
    }
}
