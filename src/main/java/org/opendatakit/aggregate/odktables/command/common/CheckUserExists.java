package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * CheckUserExists is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CheckUserExists implements Command
{
    private static final String path = "/odktables/common/checkUserExists";

    private final String userID;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private CheckUserExists()
    {
        this.userID = null;

    }

    /**
     * Constructs a new CheckUserExists.
     */
    public CheckUserExists(String userID)
    {
        Check.notNullOrEmpty(userID, "userID");
        this.userID = userID;
    }

    /**
     * @return the userID
     */
    public String getUserID()
    {
        return this.userID;
    }

    @Override
    public String toString()
    {
        return String.format("CheckUserExists: " + "userID=%s ", userID);
    }

    @Override
    public String getMethodPath()
    {
        return methodPath();
    }

    /**
     * @return the path of this Command relative to the address of an Aggregate
     *         instance. For example, if the full path to a command is
     *         http://aggregate.opendatakit.org/odktables/createTable, then this
     *         method would return '/odktables/createTable'.
     */
    public static String methodPath()
    {
        return path;
    }
}
