package org.opendatakit.aggregate.odktables.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.Command;
import org.opendatakit.aggregate.odktables.client.Column;

/**
 * CreateTable is a Command to create a new table in ODK Aggregate. CreateTable
 * is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateTable implements Command
{
    private static final String path = "/odktables/createTable";

    private final String userId;
    private final String tableId;
    private final String tableName;
    private final List<Column> columns;

    /**
     * So that Gson can serialize this class.
     */
    @SuppressWarnings("unused")
    private CreateTable()
    {
        this.userId = null;
        this.tableId = null;
        this.tableName = null;
        this.columns = null;
    }

    /**
     * Constructs a new CreateTable command.
     * 
     * @param userId
     *            the unique identifier of the user who will own the table. Must
     *            not be null or empty.
     * @param tableId
     *            the unique identifier of the table to create. This must
     *            consist of only letters, numbers, and underscores. Must not be
     *            null or empty.
     * @param tableName
     *            the name of the table to create. The only restrictions are
     *            that it must not be empty or null.
     * @param columns
     *            a list of the columns the new table will have. Must not be
     *            empty or null.
     */
    public CreateTable(String userId, String tableId, String tableName,
            List<Column> columns)
    {
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");
        if (tableId == null || tableId.length() == 0)
            throw new IllegalArgumentException("tableId '" + tableId
                    + "' was null or empty");
        if (tableName == null || tableName.length() == 0)
            throw new IllegalArgumentException("tableName '" + tableName
                    + "' was null or empty");
        if (columns == null || columns.size() == 0)
            throw new IllegalArgumentException("columns '" + columns
                    + "' was null or empty");

        this.userId = userId;
        this.tableId = tableId;
        this.tableName = tableName;
        this.columns = new ArrayList<Column>(columns);
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
        return this.tableId;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @return the columns as an unmodifiable list
     */
    public List<Column> getColumns()
    {
        return Collections.unmodifiableList(columns);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "CreateTable [userId=" + userId + ", tableId=" + tableId
                + ", tableName=" + tableName + ", columns=" + columns + "]";
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof CreateTable))
            return false;
        CreateTable o = (CreateTable) other;
        boolean userIdEquals = o.userId.equals(this.userId);
        boolean tableIdEquals = o.tableId.equals(this.tableId);
        boolean tableNameEquals = o.tableName.equals(this.tableName);
        boolean columnsEqual = o.columns.equals(this.columns);
        return userIdEquals && tableIdEquals && tableNameEquals && columnsEqual;
    }

    @Override
    public int hashCode()
    {
        return 6 * this.userId.hashCode() + 3 * this.tableId.hashCode() + 32
                * this.tableName.hashCode() + 5 * this.columns.hashCode();
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
