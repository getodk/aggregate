package org.opendatakit.aggregate.odktables.command.result;

import org.opendatakit.aggregate.odktables.CommandResult;
import org.opendatakit.aggregate.odktables.command.DeleteTable;

/**
 * DeleteTableResult represents the result of the execution of a deleteTable
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteTableResult extends CommandResult<DeleteTable>
{

    private String tableId;

    /**
     * For serialization by Gson we need a no-arg constructor
     */
    private DeleteTableResult()
    {
        super(true, null);
        this.tableId = "0";
    }

    /**
     * The success constructor. See {@link #success(String)} for param info.
     */
    private DeleteTableResult(String tableId)
    {
        super(true, null);
        if (tableId == null || tableId.length() == 0)
        {
            throw new IllegalArgumentException(
                    "Cant not have a null or empty tableId");
        }
        this.tableId = tableId;
    }

    /**
     * @return the unique identifier of the table that was successfully deleted.
     */
    public String getDeletedTableId()
    {
        return getTableId();
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
        return String.format("{%s}", this.tableId);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DeleteTableResult))
            return false;
        if (!super.equals(obj))
            return false;

        DeleteTableResult o = (DeleteTableResult) obj;
        return o.tableId.equals(this.tableId);
    }

    @Override
    public int hashCode()
    {
        return 89 * this.tableId.hashCode() + super.hashCode();
    }

    /**
     * Returns a new, successful DeleteTableResult.
     * 
     * @param tableId
     *            the unique identifier of the table that was successfully
     *            deleted. Must not be null or empty.
     * @return a new DeleteTableResult representing the successful deletion of a
     *         table.
     */
    public static DeleteTableResult success(String tableId)
    {
        return new DeleteTableResult(tableId);
    }
}
