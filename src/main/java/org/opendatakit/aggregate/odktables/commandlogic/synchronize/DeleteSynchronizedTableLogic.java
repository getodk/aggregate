package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.common.DeleteSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.DeleteSynchronizedTableResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * DeleteSynchronizedTableLogic encapsulates the logic necessary to validate and execute a
 * DeleteSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class DeleteSynchronizedTableLogic extends CommandLogic<DeleteSynchronizedTable>
{

    private final DeleteSynchronizedTable deleteSynchronizedTable;

    public DeleteSynchronizedTableLogic(DeleteSynchronizedTable deleteSynchronizedTable)
    {
        this.deleteSynchronizedTable = deleteSynchronizedTable;
    }

    @Override
    public DeleteSynchronizedTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        throw new RuntimeException("Not implemented");
    }
}