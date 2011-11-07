package org.opendatakit.aggregate.odktables.command.synchronize;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.common.utils.Check;

/**
 * Synchronize is immutable.
 * 
 * @author the.dylan.price@gmail.com
 */
public class Synchronize implements Command
{
    private static final String path = "/synchronize/synchronize";

    private final String requestingUserID;
    private final String tableID;
    private final int modificationNumber;

    /**
     * For serialization by Gson
     */
    @SuppressWarnings("unused")
    private Synchronize()
    {
        this.requestingUserID = null;
        this.tableID = null;
        this.modificationNumber = 0;

    }

    /**
     * Constructs a new Synchronize.
     */
    public Synchronize(String requestingUserID, String tableID,
            int modificationNumber)
    {

        Check.notNullOrEmpty(requestingUserID, "requestingUserID");
        Check.notNullOrEmpty(tableID, "tableID");
        Check.notNull(modificationNumber, "modificationNumber");

        this.requestingUserID = requestingUserID;
        this.tableID = tableID;
        this.modificationNumber = modificationNumber;
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
        return String.format("Synchronize: " + "requestingUserID=%s "
                + "tableID=%s " + "modificationNumber=%s " + "",
                requestingUserID, tableID, modificationNumber);
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
