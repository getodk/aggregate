package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.common.RemoveTableSynchronization;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.RemoveTableSynchronizationResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * RemoveTableSynchronizationLogic encapsulates the logic necessary to validate and execute a
 * RemoveTableSynchronization command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class RemoveTableSynchronizationLogic extends CommandLogic<RemoveTableSynchronization>
{

    private final RemoveTableSynchronization removeTableSynchronization;

    public RemoveTableSynchronizationLogic(RemoveTableSynchronization removeTableSynchronization)
    {
        this.removeTableSynchronization = removeTableSynchronization;
    }

    @Override
    public RemoveTableSynchronizationResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        throw new RuntimeException("Not implemented");
    }
}