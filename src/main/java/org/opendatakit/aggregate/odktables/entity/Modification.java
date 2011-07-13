package org.opendatakit.aggregate.odktables.entity;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Modification is a (tableUUID, modificationNumber, rowUUID) tuple, where
 * <ul>
 * <li>tableUUID = the universally unique identifier of a table</li>
 * <li>modificationNumber = the number for this modification. modifiationNumbers are incremental with each modification to a table.</li>
 * <li>rowUUID = the universally unique identifier of a row that was inserted or updated in this modification</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class Modification extends TypedEntity
{

    public Modification(String tableUUID, int modificationNumber, String rowUUID, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Modifications.getInstance(cc));
        setTableUUID(tableUUID);
        setModificationNumber(modificationNumber);
        setRowUUID(rowUUID);
    }

    public Modification(Entity entity, CallingContext cc) throws ODKDatastoreException
    {
        super(Modifications.getInstance(cc), entity);
    }

    public String getTableUUID()
    {
        DataField tableUUIDField = getDataField(Modifications.TABLE_UUID);
        return super.getEntity().getString(tableUUIDField);
    }

    public void setTableUUID(String tableUUID)
    {
        super.getEntity().setField(Modifications.TABLE_UUID, tableUUID);
    }

    public int getModificationNumber()
    {
        DataField modificationNumField = getDataField(Modifications.MODIFICATION_NUMBER);
        return super.getEntity().getInteger(modificationNumField);
    }

    public void setModificationNumber(int modificationNumber)
    {
        DataField modificationNumField = getDataField(Modifications.MODIFICATION_NUMBER);
        super.getEntity().setInteger(modificationNumField, modificationNumber);
    }

    public String getRowUUID()
    {
        DataField rowUUIDField = getDataField(Modifications.ROW_UUID);
        return super.getEntity().getString(rowUUIDField);
    }

    public void setRowUUID(String rowUUID)
    {
        super.getEntity().setField(Modifications.ROW_UUID, rowUUID);
    }
}
