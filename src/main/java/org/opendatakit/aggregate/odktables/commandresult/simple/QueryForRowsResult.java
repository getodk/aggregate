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
    private final String tableUUID;

    /**
     * Need a no-arg constructor for serialization by Gson.
     */
    private QueryForRowsResult()
    {
        super(true, null);
        this.rows = null;
        this.tableUUID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private QueryForRowsResult(List<Row> rows)
    {
        super(true, null);
        Check.notNull(rows, "rows");
        this.rows = rows;
        this.tableUUID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private QueryForRowsResult(String tableUUID, FailureReason reason)
    {
        super(false, reason);
        Check.notNullOrEmpty(tableUUID, "tableUUID");
        if (!possibleFailureReasons.contains(getReason()))
        {
            throw new IllegalArgumentException("Not a valid FailureReason: "
                    + getReason());
        }
        this.rows = null;
        this.tableUUID = tableUUID;
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
                throw new TableDoesNotExistException(null, tableUUID);
            case PERMISSION_DENIED:
                throw new PermissionDeniedException();
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /**
     * @return the tableUUID
     */
    public String getTableUUID()
    {
        return tableUUID;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("QueryForRowsResult [rows=%s, tableUUID=%s]",
                rows, tableUUID);
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
        result = prime * result
                + ((tableUUID == null) ? 0 : tableUUID.hashCode());
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
        if (tableUUID == null)
        {
            if (other.tableUUID != null)
                return false;
        } else if (!tableUUID.equals(other.tableUUID))
            return false;
        return true;
    }

    /**
     * @param tableUUID
     *            the unique identifier of the table which was successfully
     *            queried
     * @param rows
     *            the list of populated rows which are the result of the query
     * @return a new QueryForRowsResult representing the successful execution of
     *         a QueryForRows command.
     */
    public static QueryForRowsResult success(String tableUUID, List<Row> rows)
    {
        return new QueryForRowsResult(rows);
    }

    /**
     * @param tableUUID
     *            the unique identifier of the table which failed to be queried
     * @param reason
     *            the reason that the query failed. Must be either
     *            TABLE_DOES_NOT_EXIST, or PERMISSION_DENIED.
     * @return a new QueryForRowsResult representing the failed execution of a
     *         QueryForRows command.
     */
    public static QueryForRowsResult failure(String tableUUID,
            FailureReason reason)
    {
        return new QueryForRowsResult(tableUUID, reason);
    }
}
