package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A TableEntry is a (tableUUID, ownerUUID, tableName, modificationNumber) tuple,
 * where
 * <ul>
 * <li>tableUUID: the globally unique identifer of a table</li>
 * <li>ownerUUID: the globally unique identifier of the user who owns the table</li>
 * <li>tableName: the human readable name of the table</li>
 * <li>modificationNumber: the current modification number. This should be
 * incremented every time the table is edited.</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class TableEntry extends TypedEntity
{

    public TableEntry(String ownerUUID, String tableName, CallingContext cc)
            throws ODKDatastoreException
    {
        super(TableEntries.getInstance(cc));
        setOwnerUUID(ownerUUID);
        setName(tableName);
        setModificationNumber(0);
    }

    public TableEntry(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(TableEntries.getInstance(cc), entity);
    }

    public String getOwnerUUID()
    {
        return super.getEntity().getField(TableEntries.OWNER_UUID);
    }

    public void setOwnerUUID(String ownerUUID)
    {
        super.getEntity().setField(TableEntries.OWNER_UUID, ownerUUID);
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

    public void incrementModificationNumber()
    {
        int modificationNumber = getModificationNumber();
        modificationNumber++;
        setModificationNumber(modificationNumber);
    }

    private void setModificationNumber(int modificationNumber)
    {
        DataField modificationNumberField = getDataField(TableEntries.MODIFICATION_NUMBER);
        super.getEntity().setInteger(modificationNumberField,
                modificationNumber);
    }
}
