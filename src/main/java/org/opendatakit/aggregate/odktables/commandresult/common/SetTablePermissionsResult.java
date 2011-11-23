package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.SetTablePermissions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A SetPermissionsResult represents the result of executing a SetPermissions
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SetTablePermissionsResult extends
        CommandResult<SetTablePermissions>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String tableID;
    private final String aggregateUserIdentifier;

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private SetTablePermissionsResult()
    {
        super(true, null);
        this.tableID = null;
        this.aggregateUserIdentifier = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private SetTablePermissionsResult(String tableID,
            String aggregateUserIdentifier, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNullOrEmpty(aggregateUserIdentifier, "aggregateUserIdentifier");
        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for SetPermissions.",
                            reason));

        this.tableID = tableID;
        this.aggregateUserIdentifier = aggregateUserIdentifier;
    }

    /**
     * Retrieve the results from the SetPermissions command.
     * 
     * @throws PermissionDeniedException
     * @throws UserDoesNotExistException
     * @throws TableDoesNotExistException
     */
    public void checkResult() throws PermissionDeniedException,
            UserDoesNotExistException, TableDoesNotExistException
    {
        if (!successful())
        {
            switch (getReason())
            {
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(aggregateUserIdentifier);
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
                .format("SetPermissionsResult [aggregateTableIdentifier=%s, aggregateUserIdentifier=%s]",
                        tableID, aggregateUserIdentifier);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((tableID == null) ? 0 : tableID.hashCode());
        result = prime
                * result
                + ((aggregateUserIdentifier == null) ? 0
                        : aggregateUserIdentifier.hashCode());
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
        if (!(obj instanceof SetTablePermissionsResult))
            return false;
        SetTablePermissionsResult other = (SetTablePermissionsResult) obj;
        if (tableID == null)
        {
            if (other.tableID != null)
                return false;
        } else if (!tableID.equals(other.tableID))
            return false;
        if (aggregateUserIdentifier == null)
        {
            if (other.aggregateUserIdentifier != null)
                return false;
        } else if (!aggregateUserIdentifier
                .equals(other.aggregateUserIdentifier))
            return false;
        return true;
    }

    /**
     * @return a new SetPermissionsResult representing the successful completion
     *         of a SetPermissions command.
     */
    public static SetTablePermissionsResult success()
    {
        return new SetTablePermissionsResult();
    }

    /**
     * @param tableID
     *            the ID of the table which was in the command
     * @param aggregateUserIdentifier
     *            the Aggregate Identifier of the user who was in the command
     * @param reason
     *            the reason the command failed. Must be one of
     *            USER_DOES_NOT_EXIST, TABLE_DOES_NOT_EXIST, or
     *            PERMISSION_DENIED.
     * @return a new SetPermissionsResult representing the failed completion of
     *         a SetPermissions command.
     */
    public static SetTablePermissionsResult failure(String tableID,
            String aggregateUserIdentifier, FailureReason reason)
    {
        return new SetTablePermissionsResult(tableID, aggregateUserIdentifier,
                reason);
    }
}
