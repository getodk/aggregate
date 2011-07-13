package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.SetPermissions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A SetPermissionsResult represents the result of executing a SetPermissions
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SetPermissionsResult extends CommandResult<SetPermissions>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final String tableUUID;
    private final String userUUID;

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private SetPermissionsResult()
    {
        super(true, null);
        this.tableUUID = null;
        this.userUUID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private SetPermissionsResult(String tableUUID, String userUUID,
            FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(tableUUID, "tableUUID");
        Check.notNullOrEmpty(userUUID, "userUUID");
        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for SetPermissions.",
                            reason));

        this.tableUUID = tableUUID;
        this.userUUID = userUUID;
    }

    /**
     * Retrieve the results from the SetPermissions command.
     * @throws PermissionDeniedException
     * @throws UserDoesNotExistException 
     * @throws TableDoesNotExistException 
     */
    public void checkResult() throws PermissionDeniedException, UserDoesNotExistException, TableDoesNotExistException
    {
        if (!successful())
        {
            switch (getReason())
            {
            case USER_DOES_NOT_EXIST:
                throw new UserDoesNotExistException(null, userUUID);
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
        return String.format("SetPermissionsResult [tableUUID=%s, userUUID=%s]",
                tableUUID, userUUID);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((tableUUID == null) ? 0 : tableUUID.hashCode());
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
        if (!(obj instanceof SetPermissionsResult))
            return false;
        SetPermissionsResult other = (SetPermissionsResult) obj;
        if (tableUUID == null)
        {
            if (other.tableUUID != null)
                return false;
        } else if (!tableUUID.equals(other.tableUUID))
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
     * @return a new SetPermissionsResult representing the successful completion
     *         of a SetPermissions command.
     */
    public static SetPermissionsResult success()
    {
        return new SetPermissionsResult();
    }

    /**
     * @param tableUUID
     *            the UUID of the table which was in the command
     * @param userUUID
     *            the UUID of the user who was in the command
     * @param reason
     *            the reason the command failed. Must be one of
     *            USER_DOES_NOT_EXIST, TABLE_DOES_NOT_EXIST, or
     *            PERMISSION_DENIED.
     * @return a new SetPermissionsResult representing the failed completion of
     *         a SetPermissions command.
     */
    public static SetPermissionsResult failure(String tableUUID,
            String userUUID, FailureReason reason)
    {
        return new SetPermissionsResult(tableUUID, userUUID, reason);
    }
}
