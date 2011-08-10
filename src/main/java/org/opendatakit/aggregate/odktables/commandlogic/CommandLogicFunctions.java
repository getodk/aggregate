package org.opendatakit.aggregate.odktables.commandlogic;

import java.util.UUID;

import org.opendatakit.aggregate.odktables.command.ODKTablesTaskLockType;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Common utility functions for CommandLogic implementations.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CommandLogicFunctions
{
    public static int updateModificationNumber(InternalTableEntry entry,
            String aggregateTableIdentifier, int newModificationNumber,
            CallingContext cc) throws ODKTaskLockException,
            ODKEntityPersistException
    {
        Datastore ds = cc.getDatastore();
        User user = cc.getCurrentUser();
        TaskLock taskLock = ds.createTaskLock(user);
        String lockId = UUID.randomUUID().toString();
        try
        {
            if (taskLock.obtainLock(lockId, aggregateTableIdentifier,
                    ODKTablesTaskLockType.INCREMENT_MODIFICATION_NUMBER))
            {
                taskLock = null;
                entry.setModificationNumber(newModificationNumber);
                entry.save();
            }
        } finally
        {
            taskLock = ds.createTaskLock(user);
            for (int i = 0; i < 10; i++)
            {
                if (taskLock.releaseLock(lockId, aggregateTableIdentifier,
                        ODKTablesTaskLockType.INCREMENT_MODIFICATION_NUMBER))
                    break;
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e)
                {
                    // just move on, this retry mechanism 
                    // is to make things nice
                }
            }
        }
        return newModificationNumber;
    }
}
