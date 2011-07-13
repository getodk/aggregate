package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.common.UpdateSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.UpdateSynchronizedRowsResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * UpdateSynchronizedRowsLogic encapsulates the logic necessary to validate and execute a
 * UpdateSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class UpdateSynchronizedRowsLogic extends CommandLogic<UpdateSynchronizedRows>
{

    private final UpdateSynchronizedRows updateSynchronizedRows;

    public UpdateSynchronizedRowsLogic(UpdateSynchronizedRows updateSynchronizedRows)
    {
        this.updateSynchronizedRows = updateSynchronizedRows;
    }

    @Override
    public UpdateSynchronizedRowsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        throw new RuntimeException("Not implemented");
    }
}