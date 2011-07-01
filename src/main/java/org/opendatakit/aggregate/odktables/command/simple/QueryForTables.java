package org.opendatakit.aggregate.odktables.command.simple;

import org.opendatakit.aggregate.odktables.command.Command;


/**
 * QueryForTables is a Command to query ODK Aggregate for information about what
 * tables are stored in it. QueryForTables is immutable.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class QueryForTables implements Command
{
    private static final String path = "/odktables/queryForTables";

    /**
     * Construct a new QueryForTables which will initially query for all tables
     * stored in Aggregate.
     */
    public QueryForTables()
    {

    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("QueryForTables []");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals()
     */
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof QueryForTables))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return 31;
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
