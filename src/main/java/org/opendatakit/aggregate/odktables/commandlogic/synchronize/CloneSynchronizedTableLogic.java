package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.common.CloneSynchronizedTable;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.CloneSynchronizedTableResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * CloneSynchronizedTableLogic encapsulates the logic necessary to validate and execute a
 * CloneSynchronizedTable command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CloneSynchronizedTableLogic extends CommandLogic<CloneSynchronizedTable>
{

    private final CloneSynchronizedTable cloneSynchronizedTable;

    public CloneSynchronizedTableLogic(CloneSynchronizedTable cloneSynchronizedTable)
    {
        this.cloneSynchronizedTable = cloneSynchronizedTable;
    }

    @Override
    public CloneSynchronizedTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        throw new RuntimeException("Not implemented");
    }
}