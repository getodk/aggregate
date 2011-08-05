package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.synchronize.CloneSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A CloneSynchronizedTableResult represents the result of executing a
 * CloneSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CloneSynchronizedTableResult extends
        CommandResult<CloneSynchronizedTable>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.TABLE_ALREADY_EXISTS);
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
    }

    private final Modification modification;
    private final String tableID;

    private CloneSynchronizedTableResult()
    {
        super(true, null);
        this.modification = null;
        this.tableID = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private CloneSynchronizedTableResult(Modification modification)
    {
        super(true, null);

        Check.notNull(modification, "modification");

        this.modification = modification;
        this.tableID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private CloneSynchronizedTableResult(String tableID, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(tableID, "tableID");
        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for CloneSynchronizedTable.",
                            reason));

        this.modification = null;
        this.tableID = tableID;
    }

    /**
     * Retrieve the results from the CloneSynchronizedTable command.
     * 
     * @throws TableDoesNotExistException
     * @throws TableAlreadyExistsException
     */
    public Modification getModification() throws PermissionDeniedException,
            TableDoesNotExistException, TableAlreadyExistsException
    {
        if (successful())
        {
            return this.modification;
        } else
        {
            switch (getReason())
            {
            case TABLE_ALREADY_EXISTS:
                throw new TableAlreadyExistsException(tableID);
            case TABLE_DOES_NOT_EXIST:
                throw new TableDoesNotExistException(tableID);
            case PERMISSION_DENIED:
                throw new PermissionDeniedException();
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /**
     * @param modification
     *            the latest modification of the table, with a a list of all the
     *            rows in the table
     * @return a new CloneSynchronizedTableResult representing the successful
     *         completion of a CloneSynchronizedTable command.
     * 
     */
    public static CloneSynchronizedTableResult success(Modification modification)
    {
        return new CloneSynchronizedTableResult(modification);
    }

    /**
     * @param tableID
     *            the tableID which was involved in the command
     * @param aggregateTableIdentifier
     *            the Aggregate Identifier which was involved in the command
     * @param reason
     *            the reason the command failed. Must be one of
     *            TABLE_ALREADY_EXISTS, TABLE_DOES_NOT_EXIST, PERMISSION_DENIED.
     * @return a new CloneSynchronizedTableResult representing the failed
     *         completion of a CloneSynchronizedTable command.
     */
    public static CloneSynchronizedTableResult failure(String tableID,
            FailureReason reason)
    {
        return new CloneSynchronizedTableResult(tableID, reason);
    }
}
