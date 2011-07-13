package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.common.CreateSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.CreateSynchronizedTableResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * CreateSynchronizedTableLogic encapsulates the logic necessary to validate and execute a
 * CreateSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CreateSynchronizedTableLogic extends CommandLogic<CreateSynchronizedTable>
{

    private final CreateSynchronizedTable createSynchronizedTable;

    public CreateSynchronizedTableLogic(CreateSynchronizedTable createSynchronizedTable)
    {
        this.createSynchronizedTable = createSynchronizedTable;
    }

    @Override
    public CreateSynchronizedTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        throw new RuntimeException("Not implemented");
    }
}