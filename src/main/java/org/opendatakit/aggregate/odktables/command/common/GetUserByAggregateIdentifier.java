package org.opendatakit.aggregate.odktables.command.common;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * GetUserByAggregate Identifier is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetUserByAggregateIdentifier implements Command
{
    private static final String path = "/odktables/common/getUserByAggregate Identifier";

    private final String requestingUserID;
    private final String aggregateUserIdentifier;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private GetUserByAggregateIdentifier()
    {
        this.requestingUserID = null;
        this.aggregateUserIdentifier = null;

    }

    /**
     * Constructs a new GetUserByAggregate Identifier.
     */
    public GetUserByAggregateIdentifier(String requestingUserID,
            String aggregateUserIdentifier)
    {

        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(aggregateUserIdentifier, "aggregateUserIdentifier");

        this.requestingUserID = requestingUserID;
        this.aggregateUserIdentifier = aggregateUserIdentifier;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }

    /**
     * @return the aggregateUserIdentifier
     */
    public String getAggregateUserIdentifier()
    {
        return this.aggregateUserIdentifier;
    }

    @Override
    public String toString()
    {
        return String.format("GetUserByAggregate Identifier: "
                + "requestingUserID=%s " + "aggregateUserIdentifier=%s " + "",
                requestingUserID, aggregateUserIdentifier);
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
