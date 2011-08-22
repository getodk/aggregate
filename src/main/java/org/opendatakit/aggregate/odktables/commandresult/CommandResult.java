package org.opendatakit.aggregate.odktables.commandresult;

import org.opendatakit.aggregate.odktables.command.Command;

/**
 * <p>
 * A CommandResult represents the response from executing a {@link Command}. A
 * CommandResult will provide a method call to obtain the results of the
 * Command. This method will either return the results of the Command or throw a
 * subclass of ODKTablesClientException to indicate failure.
 * </p>
 * 
 * <p>
 * More details:
 * </p>
 * 
 * <p>
 * There are two general states a CommandResult can be in: one for if the
 * Command was successful and one for if it was not successful.
 * </p>
 * 
 * <p>
 * If the execution of a Command was successful, then the CommandResult will
 * contain the expected data from running the Command. For example, if a
 * CreateTable is successful, then the CreateTableResponse will contain the
 * tableId of the newly created table. Furthermore, if a InsertTables is
 * successful, then the InsertTablesResponse will contain a list of rowIds which
 * correspond to the new rows inserted.
 * </p>
 * 
 * <p>
 * On the other hand, if the execution of a Command was unsuccessful, then the
 * CommandResult will contain the reason it was unsuccessful in the form of an
 * ODKTablesClientException. This exception will be triggered upon attempted
 * retrieval of the results.
 * </p>
 * 
 * <p>
 * Requirements for subclasses of CommandResult:
 * <ul>
 * <li>A private static final list of all possible FailureReasons that could
 * make sense in your result. Make sure you check that incoming FailureReasons
 * are in this list when you write your failure constructor.</li>
 * <li>Three constructors. Make sure to call the super constructor from all
 * three of them.</li>
 * <ul>
 * <li>A private no-arg constructor to allow the class to easily be serialized.</li>
 * <li>A private constructor for a 'successful' result.</li>
 * <li>A private constructor for a 'failure' result.</li>
 * </ul>
 * <li>A getter method which returns the data of the result, or throws an
 * exception(s) to indicate the failure. These exceptions should be subclasses
 * of ODKTablesClientException.</li>
 * <li>A static 'successful' method which creates and returns an instance of
 * your class, using the private 'successful' constructor.</li>
 * <li>A static 'failure' method which creates and resturns an instance of your
 * class, using the private 'failure' constructor.</li> </ul>
 * 
 * @author the.dylan.price@gmail.com
 */
public abstract class CommandResult<T extends Command>
{
    /**
     * FailureReason enumerates all the possible reasons the execution of a
     * command could fail.
     */
    public enum FailureReason
    {
        TABLE_ALREADY_EXISTS,
        TABLE_DOES_NOT_EXIST,
        ROW_ALREADY_EXISTS,
        COLUMN_DOES_NOT_EXIST,
        USER_ALREADY_EXISTS,
        USER_DOES_NOT_EXIST,
        PERMISSION_DENIED,
        CANNOT_DELETE,
        OUT_OF_SYNCH,
        ROW_OUT_OF_SYNCH;
    }

    private final boolean successful;
    private final FailureReason reason;

    /**
     * Constructs a new CommandResult. All subclasses should call this
     * constructor.
     * 
     * @param successful
     *            whether the command was successful or not
     * @param reason
     *            If successful == true, then this should be null. If successful
     *            == false, then this must not be null.
     */
    protected CommandResult(boolean successful, FailureReason reason)
    {
        if (successful && reason != null)
        {
            throw new IllegalArgumentException(
                    "Can not be successful and have a non-null FailureReason");
        }
        if (!successful && reason == null)
        {
            throw new IllegalArgumentException(
                    "Can not be unsuccessful and have a null FailureReason");
        }

        this.successful = successful;
        this.reason = reason;
    }

    /**
     * @return true if the command was successful, false otherwise.
     */
    public boolean successful()
    {
        return this.successful;
    }

    /**
     * @return if the command failed, then returns the reason it failed.
     *         Otherwise returns null.
     */
    public FailureReason getReason()
    {
        if (!successful())
            return this.reason;
        else
            return null;
    }

    @Override
    public String toString()
    {
        return String.format("Success = %s, Reason = %s", this.successful,
                this.reason);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof CommandResult))
            return false;
        CommandResult<?> o = (CommandResult<?>) obj;
        boolean successEquals = o.successful == this.successful;
        boolean reasonsEqual = (o.reason == null && this.reason == null)
                || (o.reason.equals(this.reason));
        return successEquals && reasonsEqual;
    }

    @Override
    public int hashCode()
    {
        int success = (this.successful) ? 1 : 0;
        int reasonHash = (this.reason == null) ? 1 : this.reason.hashCode();
        return success + 72 * reasonHash;
    }
}
