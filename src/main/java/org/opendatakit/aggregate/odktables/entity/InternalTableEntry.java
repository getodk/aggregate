package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A TableEntry is a (aggregateTableIdentifier, ownerAggregate Identifier,
 * tableName, modificationNumber, isSynchronized) tuple, where
 * <ul>
 * <li>aggregateTableIdentifier: the globally unique identifer of a table</li>
 * <li>ownerAggregate Identifier: the globally unique identifier of the user who
 * owns the table</li>
 * <li>tableName: the human readable name of the table</li>
 * <li>modificationNumber: the current modification number. This should be
 * incremented every time the table is edited.</li>
 * <li>isSynchronized: true if the table is a synchronized table</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalTableEntry extends TypedEntity
{

    public InternalTableEntry(String aggregateOwnerIdentifier, String tableName,
            CallingContext cc) throws ODKDatastoreException
    {
        super(TableEntries.getInstance(cc));
        setAggregateOwnerIdentifier(aggregateOwnerIdentifier);
        setName(tableName);
        setModificationNumber(0);
    }

    public InternalTableEntry(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(TableEntries.getInstance(cc), entity);
    }

    public String getAggregateOwnerIdentifier()
    {
        return super.getEntity().getField(
                TableEntries.AGGREGATE_OWNER_IDENTIFIER);
    }

    public void setAggregateOwnerIdentifier(String aggregateOwnerIdentifier)
    {
        super.getEntity().setField(TableEntries.AGGREGATE_OWNER_IDENTIFIER,
                aggregateOwnerIdentifier);
    }

    public String getName()
    {
        return super.getEntity().getField(TableEntries.TABLE_NAME);
    }

    public void setName(String name)
    {
        super.getEntity().setField(TableEntries.TABLE_NAME, name);
    }

    public int getModificationNumber()
    {
        DataField modificationNumberField = getDataField(TableEntries.MODIFICATION_NUMBER);
        return super.getEntity().getInteger(modificationNumberField);
    }

    public int incrementModificationNumber()
    {
        int modificationNumber = getModificationNumber();
        modificationNumber++;
        setModificationNumber(modificationNumber);
        return modificationNumber;
    }

    public boolean isSynchronized()
    {
        DataField isSynchronizedField = getDataField(TableEntries.IS_SYNCHRONIZED);
        return super.getEntity().getBoolean(isSynchronizedField);
    }

    public void setSynchronized(boolean isSynchronized)
    {
        DataField isSynchronizedField = getDataField(TableEntries.IS_SYNCHRONIZED);
        super.getEntity().setBoolean(isSynchronizedField, isSynchronized);
    }

    private void setModificationNumber(int modificationNumber)
    {
        DataField modificationNumberField = getDataField(TableEntries.MODIFICATION_NUMBER);
        super.getEntity().setInteger(modificationNumberField,
                modificationNumber);
    }
}
