package org.opendatakit.aggregate.odktables.command.simple;

import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.command.Command;

/**
 * InsertRows is a Command to insert rows into a table in ODK Aggregate.
 * InsertRows is immutable.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InsertRows implements Command
{
    private static final String path = "/odktables/insertRows";

    private final String userId;
    private final String tableId;
    private final List<Row> rows;

    /**
     * So that Gson can serialize this class.
     */
    @SuppressWarnings("unused")
    private InsertRows()
    {
        this.userId = null;
        this.tableId = null;
        this.rows = null;
    }

    /**
     * Creates a new InsertRows command.
     * 
     * @param userId
     *            the unique identifier of the user who owns the table. Must not
     *            be empty or null.
     * @param tableId
     *            the unique identifier of the table to insert rows into. Must
     *            not be null or empty.
     * @param rows
     *            a list of rows to insert into the table. Must not be null or
     *            empty, and each row must be a new row in the table (i.e. no
     *            row with a matching rowId can exist).
     */
    public InsertRows(String userId, String tableId, List<Row> rows)
    {

        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");
        if (tableId == null || tableId.length() == 0)
            throw new IllegalArgumentException("tableId '" + tableId
                    + "' was null or empty");
        if (rows == null || rows.isEmpty())
            throw new IllegalArgumentException("rows was null or empty");

        this.userId = userId;
        this.tableId = tableId;
        this.rows = rows;
    }

    /**
     * @return the userId
     */
    public String getUserId()
    {
        return this.userId;
    }

    /**
     * @return the tableId
     */
    public String getTableId()
    {
        return tableId;
    }

    /**
     * @return the rows
     */
    public List<Row> getRows()
    {
        return Collections.unmodifiableList(this.rows);
    }

    @Override
    public String toString()
    {
        return String.format("{User Id = %s, Table Id = %s, Rows = %s}",
                this.userId, this.tableId, this.rows);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof InsertRows))
            return false;
        InsertRows o = (InsertRows) obj;
        boolean userIdsEqual = o.userId.equals(this.userId);
        boolean tableIdsEqual = o.tableId.equals(this.tableId);
        boolean rowsEqual = o.rows.equals(this.rows);
        return userIdsEqual && tableIdsEqual && rowsEqual;
    }

    @Override
    public int hashCode()
    {
        return 27 * this.userId.hashCode() + 78 * this.tableId.hashCode() + 24
                * this.rows.hashCode();
    }

    @Override
    public String getMethodPath()
    {
        return methodPath();
    }

    /**
     * @return the path of this Command relative to the address of an Aggregate
     *         instance. For example, if the full path to a command is
     *         http://aggregate.opendatakit.org/odktables/createTable, then this
     *         method would return '/odktables/createTable'.
     */
    public static String methodPath()
    {
        return path;
    }
}
