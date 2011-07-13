package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Permission;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.GetTablePermissions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A GetTablePermissonsResult represents the result of executing a
 * GetTablePermissons command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetTablePermissionsResult extends CommandResult<GetTablePermissions>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final List<Permission> permissions;
    private final String tableUUID;

    private GetTablePermissionsResult()
    {
        super(true, null);
        this.permissions = null;
        this.tableUUID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private GetTablePermissionsResult(List<Permission> permissions)
    {
        super(true, null);

        Check.notNull(permissions, "permissions");

        this.permissions = permissions;
        this.tableUUID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private GetTablePermissionsResult(String tableUUID, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(tableUUID, "tableID");

        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for GetTablePermissons.",
                            reason));

        this.permissions = null;
        this.tableUUID = tableUUID;
    }

    /**
     * Retrieve the results from the GetTablePermissons command.
     * 
     * @throws TableDoesNotExistException
     * @throws PermissionDeniedException
     */
    public List<Permission> getPermissions() throws PermissionDeniedException,
            TableDoesNotExistException
    {
        if (successful())
        {
            return this.permissions;
        } else
        {
            switch (getReason())
            {
            case TABLE_DOES_NOT_EXIST:
                throw new TableDoesNotExistException(null, tableUUID);
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
                "GetTablePermissonsResult [permissions=%s, tableID=%s]",
                permissions, tableUUID);
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
        result = prime * result + ((tableUUID == null) ? 0 : tableUUID.hashCode());
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
        if (!(obj instanceof GetTablePermissionsResult))
            return false;
        GetTablePermissionsResult other = (GetTablePermissionsResult) obj;
        if (permissions == null)
        {
            if (other.permissions != null)
                return false;
        } else if (!permissions.equals(other.permissions))
            return false;
        if (tableUUID == null)
        {
            if (other.tableUUID != null)
                return false;
        } else if (!tableUUID.equals(other.tableUUID))
            return false;
        return true;
    }

    /**
     * @param permissions
     *            the requested permissions
     * @return a new GetTablePermissonsResult representing the successful
     *         completion of a GetTablePermissions command.
     * 
     */
    public static GetTablePermissionsResult success(List<Permission> permissions)
    {
        return new GetTablePermissionsResult(permissions);
    }

    /**
     * @param tableUUID
     *            the ID of the table which was involved in the command
     * @param reason
     *            the reason the command failed. Must be either
     *            TABLE_DOES_NOT_EXIST or PERMISSION_DENIED
     * @return a new GetTablePermissonsResult representing the failed completion
     *         of a GetTablePermissions command.
     */
    public static GetTablePermissionsResult failure(String tableUUID,
            FailureReason reason)
    {
        return new GetTablePermissionsResult(tableUUID, reason);
    }
}
