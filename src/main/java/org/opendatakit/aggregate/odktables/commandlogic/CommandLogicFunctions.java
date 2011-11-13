package org.opendatakit.aggregate.odktables.commandlogic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.command.ODKTablesTaskLockType;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.common.ermodel.simple.AttributeType;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.Check;
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
     * Converts value to the appropriate type and returns it as an object.
     * 
     * @param attributeType
     *            the type to convert the value to. Must not be null.
     * @param value
     *            the value to convert. Must not be null.
     * @return @value, converted to the appropriate type for the given relation
     *         and attribute
     */
    public static Object convert(AttributeType type, String value)
    {
        Check.notNull(type, "type");
        Check.notNull(value, "value");

        Object realValue = null;
        switch (type)
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

    /**
     * Converts the given list of InternalRow objects to a list of
     * SynchronizedRow objects.
     * 
     * @param rows
     *            the rows to convert. Must not be null.
     * @param cols
     *            the columns of the table. Must not be null or empty.
     * @return a new List containing the converted rows
     */
    public static List<SynchronizedRow> convert(List<InternalRow> rows,
            List<InternalColumn> cols)
    {
        Check.notNull(rows, "rows");
        Check.notNullOrEmpty(cols, "cols");

        List<SynchronizedRow> synchRows = new ArrayList<SynchronizedRow>();
        for (InternalRow row : rows)
        {
            synchRows.add(convert(row, cols));
        }
        return synchRows;
    }

    /**
     * Converts the given InternalRow into a SynchronizedRow.
     * 
     * @param row
     *            the row to convert. Must not be null.
     * @param cols
     *            the columns of the table. Must not be null or empty.
     * @return the SynchronizedRow.
     */
    public static SynchronizedRow convert(InternalRow row,
            List<InternalColumn> cols)
    {
        SynchronizedRow latestRow = new SynchronizedRow();
        latestRow.setAggregateRowIdentifier(row.getAggregateIdentifier());
        latestRow.setRevisionTag(row.getRevisionTag());
        for (InternalColumn col : cols)
        {
            String value = row.getValue(col.getAggregateIdentifier());
            latestRow.setValue(col.getName(), value);
        }
        return latestRow;
    }
}
