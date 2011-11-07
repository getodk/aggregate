package org.opendatakit.aggregate.odktables.commandlogic;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.command.ODKTablesTaskLockType;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.common.ermodel.simple.Attribute;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

/**
 * Common utility functions for CommandLogic implementations.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class CommandLogicFunctions
{
    private static Log logger = LogFactory.getLog(CommandLogicFunctions.class);

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
                    ODKTablesTaskLockType.UPDATE_MODIFICATION_NUMBER))
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
                        ODKTablesTaskLockType.UPDATE_MODIFICATION_NUMBER))
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

    public static boolean deleteEntitiesAndRelation(
            Collection<TypedEntity> entities,
            TypedEntityRelation<? extends TypedEntity> relation)
    {
        boolean deletedEntities = deleteEntities(entities);
        if (deletedEntities)
        {
            try
            {
                relation.dropRelation();
            } catch (ODKDatastoreException e)
            {
                try
                {
                    // try again
                    relation.dropRelation();
                } catch (ODKDatastoreException e1)
                {
                    logger.error("Was not able to drop relation: " + relation);
                    return false;
                }
            }
            return true;
        } else
        {
            return false;
        }
    }

    public static boolean deleteEntities(Collection<TypedEntity> entities)
    {
        for (TypedEntity entity : entities)
        {
            if (!deleteEntity(entity))
                return false;
        }
        return true;
    }

    private static boolean deleteEntity(TypedEntity entity)
    {
        try
        {
            entity.delete();
        } catch (ODKDatastoreException e)
        {
            try
            {
                // try again
                entity.delete();
            } catch (ODKDatastoreException e2)
            {
                logger.error("Was not able to delete entity: " + entity);
                return false;
            }
        }
        return true;
    }

    public static boolean saveEntities(Collection<TypedEntity> entities)
    {
        for (TypedEntity entity : entities)
        {
            if (!saveEntity(entity))
                return false;
        }
        return true;
    }

    private static boolean saveEntity(TypedEntity entity)
    {
        try
        {
            entity.save();
        } catch (ODKEntityPersistException e)
        {
            try
            {
                // try again
                entity.save();
            } catch (ODKEntityPersistException e1)
            {
                logger.error("Was not able to save entity: " + entity);
                return false;
            }
        }
        return true;
    }

    /**
     * Converts @value to the appropriate type and returns it as an object.
     * 
     * @param relation
     *            the relation which value belongs to
     * @param attributeName
     *            the attribute which value belongs to
     * @param value
     *            the value to convert
     * @return @value, converted to the appropriate type for the given relation
     *         and attribute
     */
    public static Object convert(TypedEntityRelation<?> relation,
            String attributeName, String value)
    {
        Attribute attribute = relation.getAttribute(attributeName);
        Object realValue = null;
        switch (attribute.getType())
        {
        case BOOLEAN:
            realValue = Boolean.parseBoolean(value);
            break;
        case DATETIME:
            realValue = WebUtils.parseDate(value);
            break;
        case DECIMAL:
            realValue = new BigDecimal(value);
            break;
        case INTEGER:
            realValue = Integer.parseInt(value);
            break;
        case STRING:
            realValue = value;
            break;
        default:
            throw new IllegalArgumentException("Invalid attributeName");
        }
        return realValue;
    }
}
