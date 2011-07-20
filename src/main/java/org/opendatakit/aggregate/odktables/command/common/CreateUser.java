package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * CreateUser is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateUser implements Command
{
    private static final String path = "/odktables/common/createUser";

    private final String userName;
    private final String requestingUserID;
    private final String userID;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private CreateUser()
    {
        this.userName = null;
        this.requestingUserID = null;
        this.userID = null;

    }

    /**
     * Constructs a new CreateUser.
     */
    public CreateUser(String requestingUserID, String userName, String userID)
    {

        Check.notNullOrEmpty(userName, "userName");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(userID, "userID");

        this.userName = userName;
        this.requestingUserID = requestingUserID;
        this.userID = userID;
    }

    /**
     * @return the userName
     */
    public String getUserName()
    {
        return this.userName;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
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
        return String.format("CreateUser: " + "userName=%s "
                + "requestingUserID=%s " + "userID=%s " + "", userName,
                requestingUserID, userID);
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
