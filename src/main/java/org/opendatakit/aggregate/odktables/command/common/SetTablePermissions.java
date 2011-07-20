package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * SetTablePermissions is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SetTablePermissions implements Command
{
    private static final String path = "/odktables/common/setTablePermissions";

    private final String aggregateTableIdentifier;
    private final String aggregateUserIdentifier;
    private final boolean read;
    private final boolean write;
    private final String requestingUserID;
    private final boolean delete;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private SetTablePermissions()
    {
        this.aggregateTableIdentifier = null;
        this.aggregateUserIdentifier = null;
        this.read = false;
        this.write = false;
        this.requestingUserID = null;
        this.delete = false;

    }

    /**
     * Constructs a new SetTablePermissions.
     */
    public SetTablePermissions(String aggregateTableIdentifier,
            String aggregateUserIdentifier, boolean read, boolean write,
            String requestingUserID, boolean delete)
    {

        Check.notNullOrEmpty(aggregateTableIdentifier,
                "aggregateTableIdentifier");
        Check.notNullOrEmpty(aggregateUserIdentifier, "aggregateUserIdentifier");
        Check.notNull(read, "read");
        Check.notNull(write, "write");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNull(delete, "delete");

        this.aggregateTableIdentifier = aggregateTableIdentifier;
        this.aggregateUserIdentifier = aggregateUserIdentifier;
        this.read = read;
        this.write = write;
        this.requestingUserID = requestingUserID;
        this.delete = delete;
    }

    /**
     * @return the aggregateTableIdentifier
     */
    public String getAggregateTableIdentifier()
    {
        return this.aggregateTableIdentifier;
    }

    /**
     * @return the aggregateUserIdentifier
     */
    public String getAggregateUserIdentifier()
    {
        return this.aggregateUserIdentifier;
    }

    /**
     * @return the read
     */
    public boolean getRead()
    {
        return this.read;
    }

    /**
     * @return the write
     */
    public boolean getWrite()
    {
        return this.write;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }

    /**
     * @return the delete
     */
    public boolean getDelete()
    {
        return this.delete;
    }

    @Override
    public String toString()
    {
        return String.format("SetTablePermissions: "
                + "aggregateTableIdentifier=%s "
                + "aggregateUserIdentifier=%s " + "read=%s " + "write=%s "
                + "requestingUserID=%s " + "delete=%s " + "",
                aggregateTableIdentifier, aggregateUserIdentifier, read, write,
                requestingUserID, delete);
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
