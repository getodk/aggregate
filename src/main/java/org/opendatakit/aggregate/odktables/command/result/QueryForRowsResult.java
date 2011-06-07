package org.opendatakit.aggregate.odktables.command.result;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.CommandResult;
import org.opendatakit.aggregate.odktables.client.Row;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.QueryForRows;

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
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
    }

    private final List<Row> rows;
    private final String tableId;
    private final String userUri;

    /**
     * Need a no-arg constructor for serialization by Gson.
     */
    private QueryForRowsResult()
    {
        super(true, null);
        this.rows = null;
        this.userUri = null;
        this.tableId = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private QueryForRowsResult(String userUri, String tableId, List<Row> rows)
    {
        super(true, null);
        if (rows == null)
            throw new IllegalArgumentException("rows was null");
        if (userUri == null || userUri.length() == 0)
            throw new IllegalArgumentException("userUri was null or empty");
        if (tableId == null || tableId.length() == 0)
            throw new IllegalArgumentException("tableId was null or empty");

        this.rows = rows;
        this.userUri = userUri;
        this.tableId = tableId;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private QueryForRowsResult(String userUri, String tableId,
            FailureReason reason)
    {
        super(false, reason);
        if (userUri == null || userUri.length() == 0)
            throw new IllegalArgumentException("userUri was null or empty");
        if (tableId == null || tableId.length() == 0)
            throw new IllegalArgumentException("tableId was null or empty");
        if (!possibleFailureReasons.contains(getReason()))
        {
            throw new IllegalArgumentException("Not a valid FailureReason: "
                    + getReason());
        }
        this.rows = null;
        this.userUri = userUri;
        this.tableId = tableId;
    }

    /**
     * Retrieves the result of the QueryForRows command that this is a result
     * for.
     * 
     * @return a list of populated rows which are the data from the QueryForRows
     * @throws TableDoesNotExistException
     *             if the table which the command tried to query does not exist.
     * @throws UserDoesNotExistException
     *             if the user owning the table which the command tried to query
     *             does not exist.
     */
    public List<Row> getRows() throws TableDoesNotExistException,
            UserDoesNotExistException
    {
        if (successful())
        {
            return this.rows;
        } else
        {
            switch (getReason())
            {
            case TABLE_DOES_NOT_EXIST:
                throw new TableDoesNotExistException(tableId);
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(userUri);
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /**
     * @return the tableId
     */
    public String getTableId()
    {
        return tableId;
    }

    /**
     * @return the userUri
     */
    public String getUserUri()
    {
        return userUri;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format(
                "QueryForRowsResult [rows=%s, tableId=%s, userUri=%s]", rows,
                tableId, userUri);
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
        result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
        result = prime * result + ((userUri == null) ? 0 : userUri.hashCode());
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
        if (tableId == null)
        {
            if (other.tableId != null)
                return false;
        } else if (!tableId.equals(other.tableId))
            return false;
        if (userUri == null)
        {
            if (other.userUri != null)
                return false;
        } else if (!userUri.equals(other.userUri))
            return false;
        return true;
    }

    /**
     * @param userUri
     *            the public, unique identifier of the user who owns the table
     *            with tableId
     * @param tableId
     *            the unique identifier of the table which was successfully
     *            queried
     * @param rows
     *            the list of populated rows which are the result of the query
     * @return a new QueryForRowsResult representing the successful execution of
     *         a QueryForRows command.
     */
    public static QueryForRowsResult success(String userUri, String tableId,
            List<Row> rows)
    {
        return new QueryForRowsResult(userUri, tableId, rows);
    }

    /**
     * @param userUri
     *            the public, unique identifier of the user who owns the table
     *            with tableId
     * @param tableId
     *            the unique identifier of the table which failed to be queried
     * @param reason
     *            the reason that the query failed. Must be either
     *            TABLE_DOES_NOT_EXIST, or USER_DOES_NOT_EXIST.
     * @return a new QueryForRowsResult representing the failed execution of a
     *         QueryForRows command.
     */
    public static QueryForRowsResult failure(String userUri, String tableId,
            FailureReason reason)
    {
        return new QueryForRowsResult(userUri, tableId, reason);
    }
}
