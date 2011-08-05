package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.SetUserManagementPermissions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A SetUsersPermissionsResult represents the result of executing a
 * SetUsersPermissions command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SetUserManagementPermissionsResult extends
        CommandResult<SetUserManagementPermissions>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String aggregateUserIdentifier;

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private SetUserManagementPermissionsResult()
    {
        super(true, null);
        this.aggregateUserIdentifier = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private SetUserManagementPermissionsResult(String aggregateUserIdentifier,
            FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(aggregateUserIdentifier, "aggregateUserIdentifier");

        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for SetUsersPermissions.",
                            reason));

        this.aggregateUserIdentifier = aggregateUserIdentifier;
    }

    /**
     * Retrieve the results from the SetUsersPermissions command.
     * 
     * @throws UserDoesNotExistException
     * @throws PermissionDeniedException
     */
    public void checkResult() throws PermissionDeniedException,
            UserDoesNotExistException
    {
        if (!successful())
        {
            switch (getReason())
            {
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(
                        this.aggregateUserIdentifier);
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
        return String.format(
                "SetUsersPermissionsResult [aggregateUserIdentifier=%s]",
                aggregateUserIdentifier);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
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
        if (!(obj instanceof SetUserManagementPermissionsResult))
            return false;
        SetUserManagementPermissionsResult other = (SetUserManagementPermissionsResult) obj;
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
     * @return a new SetUsersPermissionsResult representing the successful
     *         completion of a SetUsersPermissions command.
     * 
     */
    public static SetUserManagementPermissionsResult success()
    {
        return new SetUserManagementPermissionsResult();
    }

    /**
     * @param aggregateUserIdentifier
     *            the Aggregate Identifier of the user involved in the command
     * @param reason
     *            the reason the command failed. Must be USER_DOES_NOT_EXIST or
     *            PERMISSION_DENIED.
     * @return a new SetUsersPermissionsResult representing the failed
     *         completion of a SetUserPermissions command
     */
    public static SetUserManagementPermissionsResult failure(
            String aggregateUserIdentifier, FailureReason reason)
    {
        return new SetUserManagementPermissionsResult(aggregateUserIdentifier,
                reason);
    }
}
