package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.exception.ColumnDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.FilterValueTypeMismatchException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.synchronize.CloneSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.ermodel.simple.AttributeType;
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
        possibleFailureReasons.add(FailureReason.COLUMN_DOES_NOT_EXIST);
        possibleFailureReasons.add(FailureReason.FILTER_VALUE_TYPE_MISMATCH);
    }

    private final Modification modification;
    private final String tableID;
    private final String badColumnName;
    private final AttributeType type;
    private final String value;

    private CloneSynchronizedTableResult()
    {
        super(true, null);
        this.modification = null;
        this.tableID = null;
        this.badColumnName = null;
        this.type = null;
        this.value = null;
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
        this.badColumnName = null;
        this.type = null;
        this.value = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private CloneSynchronizedTableResult(String tableID, String badColumnName,
            AttributeType type, String value, FailureReason reason)
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
        this.badColumnName = badColumnName;
        this.type = type;
        this.value = value;
    }

    /**
     * Retrieve the results from the CloneSynchronizedTable command.
     * 
     * @throws TableDoesNotExistException
     * @throws TableAlreadyExistsException
     * @throws ColumnDoesNotExistException
     * @throws FilterValueTypeMismatchException
     */
    public Modification getModification() throws PermissionDeniedException,
            TableDoesNotExistException, TableAlreadyExistsException,
            ColumnDoesNotExistException, FilterValueTypeMismatchException
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
            case COLUMN_DOES_NOT_EXIST:
                throw new ColumnDoesNotExistException(tableID, badColumnName);
            case FILTER_VALUE_TYPE_MISMATCH:
                throw new FilterValueTypeMismatchException(type, value);
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

    public static CloneSynchronizedTableResult failureTableAlreadyExists(
            String tableID)
    {
        return new CloneSynchronizedTableResult(tableID, null, null, null,
                FailureReason.TABLE_ALREADY_EXISTS);
    }

    public static CloneSynchronizedTableResult failureTableDoesNotExist(
            String tableID)
    {
        return new CloneSynchronizedTableResult(tableID, null, null, null,
                FailureReason.TABLE_DOES_NOT_EXIST);
    }

    public static CloneSynchronizedTableResult failurePermissionDenied()
    {
        return new CloneSynchronizedTableResult(null, null, null, null,
                FailureReason.PERMISSION_DENIED);
    }

    public static CloneSynchronizedTableResult failureColumnDoesNotExist(
            String tableID, String badColumnName)
    {
        return new CloneSynchronizedTableResult(tableID, badColumnName, null,
                null, FailureReason.COLUMN_DOES_NOT_EXIST);
    }

    public static CloneSynchronizedTableResult failureFilterValueTypeMismatch(
            String tableID, AttributeType type, String value)
    {
        return new CloneSynchronizedTableResult(tableID, null, type, value,
                FailureReason.FILTER_VALUE_TYPE_MISMATCH);
    }

}
