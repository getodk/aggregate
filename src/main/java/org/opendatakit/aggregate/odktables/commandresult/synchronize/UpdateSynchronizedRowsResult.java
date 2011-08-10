package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.exception.ColumnDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.OutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.RowOutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.synchronize.UpdateSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A UpdateSynchronizedRowsResult represents the result of executing a
 * UpdateSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class UpdateSynchronizedRowsResult extends
        CommandResult<UpdateSynchronizedRows>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.TABLE_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.PERMISSION_DENIED);
        possibleFailureReasons.add(FailureReason.OUT_OF_SYNCH);
        possibleFailureReasons.add(FailureReason.ROW_OUT_OF_SYNCH);
        possibleFailureReasons.add(FailureReason.COLUMN_DOES_NOT_EXIST);
    }

    private final Modification modification;
    private final String tableID;
    private final String aggregateRowIdentifier;
    private final String badColumnName;

    private UpdateSynchronizedRowsResult(boolean successful,
            FailureReason reason, Modification modification, String aggregateRowIdentifier,
            String tableID, String badColumnName)
    {
        super(successful, reason);
        this.modification = modification;
        this.tableID = tableID;
        this.aggregateRowIdentifier = aggregateRowIdentifier;
        this.badColumnName = badColumnName;
    }

    private UpdateSynchronizedRowsResult()
    {
        this(true, null, null, null, null, null);
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private UpdateSynchronizedRowsResult(Modification modification)
    {
        this(true, null, modification, null, null, null);
        Check.notNull(modification, "modification");
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private UpdateSynchronizedRowsResult(String aggregateRowIdentifier,
            String tableID, String badColumnName, FailureReason reason)
    {
        this(false, reason, null, aggregateRowIdentifier, tableID,
                badColumnName);
        Check.notNullOrEmpty(tableID, "tableID");
        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for UpdateSynchronizedRows.",
                            reason));
    }

    /**
     * Retrieve the results from the UpdateSynchronizedRows command.
     * 
     * @throws OutOfSynchException
     * @throws TableDoesNotExistException
     * @throws RowOutOfSynchException
     * @throws ColumnDoesNotExistException
     */
    public Modification getModification() throws PermissionDeniedException,
            OutOfSynchException, TableDoesNotExistException,
            RowOutOfSynchException, ColumnDoesNotExistException
    {
        if (successful())
        {
            return modification;
        } else
        {
            switch (getReason())
            {
            case ROW_OUT_OF_SYNCH:
                throw new RowOutOfSynchException(aggregateRowIdentifier);
            case OUT_OF_SYNCH:
                throw new OutOfSynchException();
            case TABLE_DOES_NOT_EXIST:
                throw new TableDoesNotExistException(tableID);
            case PERMISSION_DENIED:
                throw new PermissionDeniedException();
            case COLUMN_DOES_NOT_EXIST:
                throw new ColumnDoesNotExistException(tableID, badColumnName);
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format(
                "UpdateSynchronizedRowsResult [modification=%s, tableID=%s]",
                modification, tableID);
    }

    /**
     * @return a new UpdateSynchronizedRowsResult representing the successful
     *         completion of an UpdateSynchronizedRows command.
     * 
     */
    public static UpdateSynchronizedRowsResult success(Modification modification)
    {
        return new UpdateSynchronizedRowsResult(modification);
    }

    /**
     * @return a new UpdateSynchronizedRowsResult representing the failed
     *         completion of an UpdateSynchronizedRows command.
     */
    public static UpdateSynchronizedRowsResult failure(
            String aggregateRowIdentifier, String tableID,
            String badColumnName, FailureReason reason)
    {
        return new UpdateSynchronizedRowsResult(aggregateRowIdentifier,
                tableID, badColumnName, reason);
    }
}
