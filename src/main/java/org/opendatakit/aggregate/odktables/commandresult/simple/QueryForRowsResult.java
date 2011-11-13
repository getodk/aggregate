package org.opendatakit.aggregate.odktables.commandresult.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.simple.QueryForRows;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A QueryForRowsResult represents the result of executing a QueryForRows
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class QueryForRowsResult extends CommandResult<QueryForRows>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final List<Row> rows;
    private final String tableID;

    /**
     * Need a no-arg constructor for serialization by Gson.
     */
    private QueryForRowsResult()
    {
        super(true, null);
        this.rows = null;
        this.tableID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private QueryForRowsResult(List<Row> rows)
    {
        super(true, null);
        Check.notNull(rows, "rows");
        this.rows = rows;
        this.tableID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private QueryForRowsResult(String tableID, FailureReason reason)
    {
        super(false, reason);
        Check.notNullOrEmpty(tableID, "tableID");
        if (!possibleFailureReasons.contains(getReason()))
        {
            throw new IllegalArgumentException("Not a valid FailureReason: "
                    + getReason());
        }
        this.rows = null;
        this.tableID = tableID;
    }

    /**
     * Retrieves the result of the QueryForRows command that this is a result
     * for.
     * 
     * @return a list of populated rows which are the data from the QueryForRows
     * @throws TableDoesNotExistException
     *             if the table which the command tried to query does not exist.
     * @throws PermissionDeniedException
     */
    public List<Row> getRows() throws TableDoesNotExistException,
            UserDoesNotExistException, PermissionDeniedException
    {
        if (successful())
        {
            return this.rows;
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

    /**
     * @return the aggregateTableIdentifier
     */
    public String getAggregateTableIdentifier()
    {
        return tableID;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("QueryForRowsResult [rows=%s, tableID=%s]", rows,
                tableID);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((rows == null) ? 0 : rows.hashCode());
        result = prime * result + ((tableID == null) ? 0 : tableID.hashCode());
        return result;
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
        if (!(obj instanceof QueryForRowsResult))
            return false;
        QueryForRowsResult other = (QueryForRowsResult) obj;
        if (rows == null)
        {
            if (other.rows != null)
                return false;
        } else if (!rows.equals(other.rows))
            return false;
        if (tableID == null)
        {
            if (other.tableID != null)
                return false;
        } else if (!tableID.equals(other.tableID))
            return false;
        return true;
    }

    /**
     * @param rows
     *            the list of populated rows which are the result of the query
     * @return a new QueryForRowsResult representing the successful execution of
     *         a QueryForRows command.
     */
    public static QueryForRowsResult success(List<Row> rows)
    {
        return new QueryForRowsResult(rows);
    }

    /**
     * @param tableID
     *            the client's identifier for the table which failed to be
     *            queried
     * @param reason
     *            the reason that the query failed. Must be either
     *            TABLE_DOES_NOT_EXIST, or PERMISSION_DENIED.
     * @return a new QueryForRowsResult representing the failed execution of a
     *         QueryForRows command.
     */
    public static QueryForRowsResult failure(String tableID,
            FailureReason reason)
    {
        return new QueryForRowsResult(tableID, reason);
    }
}
