package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.CannotDeleteException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A DeleteUserResult represents the result of executing a DeleteUser command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteUserResult extends CommandResult<DeleteUser>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
        possibleFailureReasons.add(FailureReason.CANNOT_DELETE);
    }

    private final String aggregateUserIdentifier;

    private DeleteUserResult()
    {
        super(true, null);
        this.aggregateUserIdentifier = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private DeleteUserResult(String aggregateUserIdentifier)
    {
        super(true, null);

        Check.notNullOrEmpty(aggregateUserIdentifier, "aggregateUserIdentifier");

        this.aggregateUserIdentifier = aggregateUserIdentifier;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private DeleteUserResult(String aggregateUserIdentifier,
            FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(aggregateUserIdentifier, "aggregateUserIdentifier");
        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for DeleteUser.",
                            reason));

        this.aggregateUserIdentifier = aggregateUserIdentifier;
    }

    /**
     * Retrieve the results from the DeleteUser command.
     * 
     * @return the Aggregate Identifier of the successfully deleted user
     * @throws PermissionDeniedException
     * @throws UserDoesNotExistException
     * @throws CannotDeleteException
     */
    public String getDeletedAggregateUserIdentifier()
            throws PermissionDeniedException, UserDoesNotExistException,
            CannotDeleteException
    {
        if (successful())
        {
            return this.aggregateUserIdentifier;
        } else
        {
            switch (getReason())
            {
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(
                        this.aggregateUserIdentifier);
            case PERMISSION_DENIED:
                throw new PermissionDeniedException();
            case CANNOT_DELETE:
                throw new CannotDeleteException(this.aggregateUserIdentifier);
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
        return String.format("DeleteUserResult [aggregateUserIdentifier=%s]",
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
        if (!(obj instanceof DeleteUserResult))
            return false;
        DeleteUserResult other = (DeleteUserResult) obj;
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
     * @param aggregateUserIdentifier
     *            the Aggregate Identifier of the successfully deleted user
     * @return a new DeleteUserResult representing the successful deletion of
     *         the user
     */
    public static DeleteUserResult success(String aggregateUserIdentifier)
    {
        return new DeleteUserResult(aggregateUserIdentifier);
    }

    /**
     * @param aggregateUserIdentifier
     *            the Aggregate Identifier of the unsuccessfully deleted user
     * @param reason
     *            the reason the user was unable to be deleted
     * @return a new DeleteUserResult representing the failed deletion of the
     *         user
     */
    public static DeleteUserResult failure(String aggregateUserIdentifier,
            FailureReason reason)
    {
        return new DeleteUserResult(aggregateUserIdentifier, reason);
    }
}
