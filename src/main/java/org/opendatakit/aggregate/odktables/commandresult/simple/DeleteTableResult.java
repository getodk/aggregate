package org.opendatakit.aggregate.odktables.commandresult.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A DeleteTableResult represents the result of executing a DeleteTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteTableResult extends CommandResult<DeleteTable>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String aggregateTableIdentifier;

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private DeleteTableResult()
    {
        super(true, null);
        this.aggregateTableIdentifier = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private DeleteTableResult(String aggregateTableIdentifier, FailureReason reason)
    {
        super(false, reason);

        if (!possibleFailureReasons.contains(reason))
        {
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for DeleteTable.",
                            reason));
        }

        this.aggregateTableIdentifier = aggregateTableIdentifier;
    }

    /**
     * Retrieve the results from the DeleteTable command.
     * 
     * @throws TableDoesNotExistException
     * @throws PermissionDeniedException
     */
    public void checkResults() throws PermissionDeniedException,
            TableDoesNotExistException
    {
        if (!successful())
        {
            switch (getReason())
            {
            case TABLE_DOES_NOT_EXIST:
                throw new TableDoesNotExistException(aggregateTableIdentifier);
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
        return String.format("DeleteTableResult [aggregateTableIdentifier=%s]", aggregateTableIdentifier);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((aggregateTableIdentifier == null) ? 0 : aggregateTableIdentifier.hashCode());
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
        if (!(obj instanceof DeleteTableResult))
            return false;
        DeleteTableResult other = (DeleteTableResult) obj;
        if (aggregateTableIdentifier == null)
        {
            if (other.aggregateTableIdentifier != null)
                return false;
        } else if (!aggregateTableIdentifier.equals(other.aggregateTableIdentifier))
            return false;
        return true;
    }

    /**
     * @return a new DeleteTableResult representing the successful completion of
     *         a DeleteTable command.
     */
    public static DeleteTableResult success()
    {
        return new DeleteTableResult();
    }

    /**
     * @param aggregateTableIdentifier
     *            the Aggregate Identifier of the table involved in the command
     * @param reason
     *            the reason the command failed. Must be either
     *            TABLE_DOES_NOT_EXIST or PERMISSION_DENIED.
     * @return a new DeleteTableResult representing the failed completion of a
     *         DeleteTable command.
     */
    public static DeleteTableResult failure(String aggregateTableIdentifier,
            FailureReason reason)
    {
        return new DeleteTableResult(aggregateTableIdentifier, reason);
    }
}
