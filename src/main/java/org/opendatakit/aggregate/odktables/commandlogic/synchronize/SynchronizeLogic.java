package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.common.Synchronize;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.SynchronizeResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SynchronizeLogic encapsulates the logic necessary to validate and execute a
 * Synchronize command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SynchronizeLogic extends CommandLogic<Synchronize>
{

    private final Synchronize synchronize;

    public SynchronizeLogic(Synchronize synchronize)
    {
        this.synchronize = synchronize;
    }

    @Override
    public SynchronizeResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        throw new RuntimeException("Not implemented");
    }
}