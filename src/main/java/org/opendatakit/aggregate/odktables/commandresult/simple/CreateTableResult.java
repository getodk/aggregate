package org.opendatakit.aggregate.odktables.commandresult.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.result.CreateTableResult;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;

/**
 * A CreateTableResult represents the result of executing a CreateTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateTableResult extends CommandResult<CreateTable>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.TABLE_ALREADY_EXISTS);
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
    }

    private final String userId;
    private final String tableId;

    /**
     * For serialization by Gson we need a no-arg constructor.
     */
    private CreateTableResult()
    {
        super(true, null);
        this.userId = null;
        this.tableId = null;
    }

    /**
     * The success constructor. See {@link #success(String)} for param info.
     */
    private CreateTableResult(String userId, String tableId)
    {
        super(true, null);
        this.userId = userId;
        this.tableId = tableId;
    }

    /**
     * The failure constructor. See
     * {@link #failure(String, org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason)}
     * for param info.
     */
    private CreateTableResult(String userId, String tableId,
            FailureReason reason)
    {
        super(false, reason);

        if (tableId == null || tableId.length() == 0)
            throw new IllegalArgumentException("tableId '" + tableId
                    + "' was null or empty");
        if (!possibleFailureReasons.contains(getReason()))
        {
            throw new IllegalArgumentException("Not a valid FailureReason: "
                    + getReason());
        }

        this.userId = userId;
        this.tableId = tableId;
    }

    /**
     * Retrieve the results from the createTable Command.
     * 
     * @return the tableId of the table that was successfully created
     * @throws TableAlreadyExistsException
     *             if the table that the createTable Command tried to create
     *             already existed.
     * @throws UserDoesNotExistException
     *             if the user that would have owned the new table does not
     *             exist
     */
    public String getCreatedTableId() throws TableAlreadyExistsException,
            UserDoesNotExistException
    {
        if (successful())
        {
            return getTableId();
        } else
        {
            switch (getReason())
            {
            case TABLE_ALREADY_EXISTS:
                throw new TableAlreadyExistsException(getTableId());
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(getUserId());
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

    /**
     * @return the userId associated with this result.
     */
    public String getUserId()
    {
        return this.userId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("CreateTableResult [userId=%s, tableId=%s]",
                userId, tableId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
        if (!(obj instanceof CreateTableResult))
            return false;
        CreateTableResult other = (CreateTableResult) obj;
        if (tableId == null)
        {
            if (other.tableId != null)
                return false;
        } else if (!tableId.equals(other.tableId))
            return false;
        if (userId == null)
        {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }

    /**
     * @param userId
     *            the private unique identifier of the user who owns the table
     *            which was successfully created.
     * @param tableId
     *            the id of the table that was successfully created.
     * @return a new CreateTableResult representing the successful creation of a
     *         table.
     */
    public static CreateTableResult success(String userId, String tableId)
    {
        return new CreateTableResult(userId, tableId);
    }

    /**
     * @param userId
     *            the private unique identifier of the user who owns the table
     *            which was successfully created
     * @param tableId
     *            the id of the table that was not created.
     * @param reason
     *            the reason the table was not created. Must be one of the
     *            following: TABLE_ALREADY_EXISTS
     * @return a new CreateTableResult representing the failed creation of a
     *         table.
     */
    public static CreateTableResult failure(String userId, String tableId,
            FailureReason reason)
    {
        return new CreateTableResult(userId, tableId, reason);
    }
}
