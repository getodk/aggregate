package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * QueryForTables is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class QueryForTables implements Command
{
    private static final String path = "/common/listAllTables";

    private final String requestingUserID;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private QueryForTables()
    {
        this.requestingUserID = null;

    }

    /**
     * Constructs a new QueryForTables.
     */
    public QueryForTables(String requestingUserID)
    {

        Check.notNullOrEmpty(requestingUserID, "requestingUserID");

        this.requestingUserID = requestingUserID;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }

    @Override
    public String toString()
    {
        return String.format("QueryForTables: " + "requestingUserID=%s " + "",
                requestingUserID);
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
