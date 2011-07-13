package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import org.opendatakit.aggregate.odktables.command.common.InsertSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.InsertSynchronizedRowsResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * InsertSynchronizedRowsLogic encapsulates the logic necessary to validate and execute a
 * InsertSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InsertSynchronizedRowsLogic extends CommandLogic<InsertSynchronizedRows>
{

    private final InsertSynchronizedRows insertSynchronizedRows;

    public InsertSynchronizedRowsLogic(InsertSynchronizedRows insertSynchronizedRows)
    {
        this.insertSynchronizedRows = insertSynchronizedRows;
    }

    @Override
    public InsertSynchronizedRowsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        throw new RuntimeException("Not implemented");
    }
}