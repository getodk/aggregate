package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Permission;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.GetUserPermissions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A GetUserPermissionsResult represents the result of executing a
 * GetUserPermissions command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetUserPermissionsResult extends CommandResult<GetUserPermissions>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final List<Permission> permissions;
    private final String userUUID;

    private GetUserPermissionsResult()
    {
        super(true, null);
        this.permissions = null;
        this.userUUID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private GetUserPermissionsResult(List<Permission> permissions)
    {
        super(true, null);

        Check.notNull(permissions, "permissions");

        this.permissions = permissions;
        this.userUUID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private GetUserPermissionsResult(String userUUID, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(userUUID, "userUUID");

        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for GetUserPermissions.",
                            reason));

        this.permissions = null;
        this.userUUID = userUUID;
    }

    /**
     * Retrieve the results from the GetUserPermissions command.
     * 
     * @throws UserDoesNotExistException
     * @throws PermissionDeniedException
     */
    public List<Permission> getPermissions() throws PermissionDeniedException,
            UserDoesNotExistException
    {
        if (successful())
        {
            return this.permissions;
        } else
        {
            switch (getReason())
            {
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(null, userUUID);
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
                "GetUserPermissionsResult [permissions=%s, userUUID=%s]",
                permissions, userUUID);
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
                + ((permissions == null) ? 0 : permissions.hashCode());
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
        if (!(obj instanceof GetUserPermissionsResult))
            return false;
        GetUserPermissionsResult other = (GetUserPermissionsResult) obj;
        if (permissions == null)
        {
            if (other.permissions != null)
                return false;
        } else if (!permissions.equals(other.permissions))
            return false;
        if (userUUID == null)
        {
            if (other.userUUID != null)
                return false;
        } else if (!userUUID.equals(other.userUUID))
            return false;
        return true;
    }

    /**
     * @param permissions
     *            the permissions retrieved
     * @return a new GetUserPermissionsResult representing the successful
     *         completion of a GetUserPermissions command.
     * 
     */
    public static GetUserPermissionsResult success(List<Permission> permissions)
    {
        return new GetUserPermissionsResult(permissions);
    }

    /**
     * @param userUUID
     *            the UUID of the user involved in the command
     * @param reason
     *            the reason the command failed. Must be either
     *            USER_DOES_NOT_EXIST or PERMISSION_DENIED.
     * @return a new GetUserPermissionsResult representing the failed
     *         completiong of a GetUserPermissions command.
     */
    public static GetUserPermissionsResult failure(String userUUID,
            FailureReason reason)
    {
        return new GetUserPermissionsResult(userUUID, reason);
    }
}
