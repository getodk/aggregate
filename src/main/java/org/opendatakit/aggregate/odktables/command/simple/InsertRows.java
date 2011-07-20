package org.opendatakit.aggregate.odktables.command.simple;

import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * InsertRows is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertRows implements Command
{
    private static final String path = "/odktables/simple/insertRows";

    private final List<Row> rows;
    private final String requestingUserID;
    private final String tableID;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private InsertRows()
    {
        this.rows = null;
        this.requestingUserID = null;
        this.tableID = null;

    }

    /**
     * Constructs a new InsertRows.
     */
    public InsertRows(String requestingUserID, List<Row> rows, String tableID)
    {

        Check.notNull(rows, "rows");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");

        this.rows = rows;
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
    }

    /**
     * @return the rows
     */
    public List<Row> getRows()
    {
        return this.rows;
    }

    /**
     * @return the requestingUserID
     */
    public String getRequestingUserID()
    {
        return this.requestingUserID;
    }

    /**
     * @return the tableID
     */
    public String getTableID()
    {
        return this.tableID;
    }

    @Override
    public String toString()
    {
        return String.format("InsertRows: " + "rows=%s "
                + "requestingUserID=%s " + "tableID=%s " + "", rows,
                requestingUserID, tableID);
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
