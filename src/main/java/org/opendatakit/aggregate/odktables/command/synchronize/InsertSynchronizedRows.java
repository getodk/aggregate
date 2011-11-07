package org.opendatakit.aggregate.odktables.command.synchronize;

import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * InsertSynchronizedRows is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertSynchronizedRows implements Command
{
    private static final String path = "/synchronize/insertSynchronizedRows";

    private final String requestingUserID;
    private final String tableID;
    private final int modificationNumber;
    private final List<SynchronizedRow> newRows;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private InsertSynchronizedRows()
    {
        this.requestingUserID = null;
        this.tableID = null;
        this.modificationNumber = 0;
        this.newRows = null;

    }

    /**
     * Constructs a new InsertSynchronizedRows.
     */
    public InsertSynchronizedRows(String requestingUserID, String tableID,
            int modificationNumber, List<SynchronizedRow> newRows)
    {

        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber");
        Check.notNull(newRows, "newRows");

        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
        this.newRows = newRows;
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

    /**
     * @return the modificationNumber
     */
    public int getModificationNumber()
    {
        return this.modificationNumber;
    }

    /**
     * @return the newRows
     */
    public List<SynchronizedRow> getNewRows()
    {
        return this.newRows;
    }

    @Override
    public String toString()
    {
        return String.format("InsertSynchronizedRows: "
                + "requestingUserID=%s " + "tableID=%s "
                + "modificationNumber=%s " + "newRows=%s " + "",
                requestingUserID, tableID, modificationNumber, newRows);
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
