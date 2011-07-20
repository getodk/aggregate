package org.opendatakit.aggregate.odktables.command.result;

import org.opendatakit.aggregate.odktables.CommandResult;
import org.opendatakit.aggregate.odktables.client.TableList;
import org.opendatakit.aggregate.odktables.command.QueryForTables;

/**
 * A QueryForTablesResult represents the result of executing a QueryForTables
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class QueryForTablesResult extends CommandResult<QueryForTables>
{

    private final TableList tableList;

    /**
     * Need a no-arg constructor for serialization by Gson.
     */
    private QueryForTablesResult()
    {
        super(true, null);
        this.tableList = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private QueryForTablesResult(TableList tableList)
    {
        super(true, null);
        if (tableList == null)
            throw new IllegalArgumentException("tableList was null");

        this.tableList = tableList;
    }

    /**
     * @return the TableList representing the results of the QueryForTables
     *         command.
     */
    public TableList getTableList()
    {
        return this.tableList;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("QueryForTablesResult [tableList=%s]", tableList);
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
                + ((tableList == null) ? 0 : tableList.hashCode());
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
        if (!(obj instanceof QueryForTablesResult))
            return false;
        QueryForTablesResult other = (QueryForTablesResult) obj;
        if (tableList == null)
        {
            if (other.tableList != null)
                return false;
        } else if (!tableList.equals(other.tableList))
            return false;
        return true;
    }

    /**
     * @param tableList
     *            a TableList containing an entry for each table in the results
     *            of the QueryForTables command.
     * @return a new QueryForTablesResult representing the successful completion
     *         of a QueryForTables command.
     */
    public static QueryForTablesResult success(TableList tableList)
    {
        return new QueryForTablesResult(tableList);
    }
}
