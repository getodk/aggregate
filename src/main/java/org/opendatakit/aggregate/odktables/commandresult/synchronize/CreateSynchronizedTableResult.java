package org.opendatakit.aggregate.odktables.commandresult.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.command.synchronize.CreateSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A CreateSynchronizedTableResult represents the result of executing a
 * CreateSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateSynchronizedTableResult extends
        CommandResult<CreateSynchronizedTable>
{
    private static final List<FailureReason> possibleFailureReasons;
    static
    {
        possibleFailureReasons = new ArrayList<FailureReason>();
        possibleFailureReasons.add(FailureReason.TABLE_ALREADY_EXISTS);
    }

    private final Modification modification;
    private final String tableID;

    private CreateSynchronizedTableResult()
    {
        super(true, null);
        this.modification = null;
        this.tableID = null;

    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private CreateSynchronizedTableResult(Modification modification)
    {
        super(true, null);

        Check.notNull(modification, "modification");

        this.modification = modification;
        this.tableID = null;
    }

    /**
     * The failure constructor. See {@link #failure} for param info.
     */
    private CreateSynchronizedTableResult(String tableID, FailureReason reason)
    {
        super(false, reason);

        Check.notNullOrEmpty(tableID, "tableID");
        if (!possibleFailureReasons.contains(reason))
            throw new IllegalArgumentException(
                    String.format(
                            "Failure reason %s not a valid failure reason for CreateSynchronizedTable.",
                            reason));

        this.modification = null;
        this.tableID = tableID;
    }

    /**
     * Retrieve the results from the CreateSynchronizedTable command.
     * 
     * @throws TableAlreadyExistsException
     */
    public Modification getModification() throws TableAlreadyExistsException
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
            default:
                throw new RuntimeException("An unknown error occured.");
            }
        }
    }

    /**
     * @param modification
     *            the initial modification of the table
     * @return a new CreateSynchronizedTableResult representing the successful
     *         completion of a CreateSynchronizedTable command.
     * 
     */
    public static CreateSynchronizedTableResult success(
            Modification modification)
    {
        return new CreateSynchronizedTableResult(modification);
    }

    /**
     * @param tableID
     *            the caller's ID for the table which failed to be created
     * @param reason
     *            the reason the command failed. Must be TABLE_ALREADY_EXISTS.
     * @return a new CreateSynchronizedTableResult representing the failed
     *         completion of a CreateSynchronizedTable command.
     */
    public static CreateSynchronizedTableResult failure(String tableID,
            FailureReason reason)
    {
        return new CreateSynchronizedTableResult(tableID, reason);
    }
}
