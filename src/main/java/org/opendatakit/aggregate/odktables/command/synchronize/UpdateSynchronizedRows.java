package org.opendatakit.aggregate.odktables.command.synchronize;

import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * UpdateSynchronizedRows is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class UpdateSynchronizedRows implements Command
{
    private static final String path = "/odktables/synchronize/updateSynchronizedRows";

    private final List<SynchronizedRow> changedRows;
    private final String requestingUserID;
    private final String tableID;
    private final int modificationNumber;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private UpdateSynchronizedRows()
    {
        this.changedRows = null;
        this.requestingUserID = null;
        this.tableID = null;
        this.modificationNumber = 0;

    }

    /**
     * Constructs a new UpdateSynchronizedRows.
     */
    public UpdateSynchronizedRows(String requestingUserID,
            List<SynchronizedRow> changedRows, String tableID,
            int modificationNumber)
    {

        Check.notNull(changedRows, "changedRows");
        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber");

        this.changedRows = changedRows;
        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
    }

    /**
     * @return the changedRows
     */
    public List<SynchronizedRow> getChangedRows()
    {
        return this.changedRows;
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

    @Override
    public String toString()
    {
        return String.format("UpdateSynchronizedRows: " + "changedRows=%s "
                + "requestingUserID=%s " + "tableID=%s "
                + "modificationNumber=%s " + "", changedRows, requestingUserID,
                tableID, modificationNumber);
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
