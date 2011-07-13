package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.SetUsersPermissions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A SetUsersPermissionsResult represents the result of executing a
 * SetUsersPermissions command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SetUsersPermissionsResult extends
        CommandResult<SetUsersPermissions>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String userUUID;

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private SetUsersPermissionsResult()
    {
        super(true, null);
        this.userUUID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private SetUsersPermissionsResult(String userUUID, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(userUUID, "userUUID");

        if (!this.possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for SetUsersPermissions.",
                            reason));

        this.userUUID = userUUID;
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
                throw new UserDoesNotExistException(null, this.userUUID);
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
        return String.format("SetUsersPermissionsResult [userUUID=%s]",
                userUUID);
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
                + ((userUUID == null) ? 0 : userUUID.hashCode());
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
        if (!(obj instanceof SetUsersPermissionsResult))
            return false;
        SetUsersPermissionsResult other = (SetUsersPermissionsResult) obj;
        if (userUUID == null)
        {
            if (other.userUUID != null)
                return false;
        } else if (!userUUID.equals(other.userUUID))
            return false;
        return true;
    }

    /**
     * @return a new SetUsersPermissionsResult representing the successful
     *         completion of a SetUsersPermissions command.
     * 
     */
    public static SetUsersPermissionsResult success()
    {
        return new SetUsersPermissionsResult();
    }

    /**
     * @param userUUID
     *            the UUID of the user involved in the command
     * @param reason
     *            the reason the command failed. Must be USER_DOES_NOT_EXIST or PERMISSION_DENIED.
     * @return a new SetUsersPermissionsResult representing the failed
     *         completion of a SetUserPermissions command
     */
    public static SetUsersPermissionsResult failure(String userUUID,
            FailureReason reason)
    {
        return new SetUsersPermissionsResult(userUUID, reason);
    }
}
