package org.opendatakit.aggregate.odktables.command.simple;

import org.opendatakit.aggregate.odktables.command.Command;


/**
 * DeleteTable is a Command to delete a table from ODK Aggregate. DeleteTable is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteTable implements Command
{
    private static final String path = "/odktables/deleteTable";

    private final String userId;
    private final String tableId;

    /**
     * So that Gson can serialize this class.
     */
    @SuppressWarnings("unused")
    private DeleteTable()
    {
        this.userId = null;
        this.tableId = null;
    }

    /**
     * Constructs a new DeleteTable command.
     * 
     * @param userId
     *            the unique identifier of the user who owns the table. Must not
     *            be empty or null.
     * @param tableId
     *            the unique identifier of the table to delete. Must be non-null
     *            and non-empty.
     */
    public DeleteTable(String userId, String tableId)
    {
        if (userId == null || userId.length() == 0)
            throw new IllegalArgumentException("userId '" + userId
                    + "' was null or empty");
        if (tableId == null || tableId.length() == 0)
            throw new IllegalArgumentException("tableId '" + tableId
                    + "' was null or empty");

        this.userId = userId;
        this.tableId = tableId;
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

    @Override
    public String toString()
    {
        return String.format("{User Id = %s, Table Id = %s}", userId, tableId);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DeleteTable))
            return false;
        DeleteTable o = (DeleteTable) obj;
        return o.userId.equals(this.userId) && o.tableId.equals(this.tableId);
    }

    @Override
    public int hashCode()
    {
        return 242 * this.userId.hashCode() + 365 * this.tableId.hashCode();
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
