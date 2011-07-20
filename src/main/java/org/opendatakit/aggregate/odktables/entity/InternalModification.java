package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Modifications;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Modification is a (aggregateTableIdentifier, modificationNumber, aggregateRowIdentifier) tuple, where
 * <ul>
 * <li>aggregateTableIdentifier = the universally unique identifier of a table</li>
 * <li>modificationNumber = the number for this modification. modifiationNumbers
 * are incremental with each modification to a table.</li>
 * <li>aggregateRowIdentifier = the universally unique identifier of a row that was inserted or
 * updated in this modification</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalModification extends TypedEntity
{

    public InternalModification(String aggregateTableIdentifier, int modificationNumber,
            String aggregateRowIdentifier, CallingContext cc) throws ODKDatastoreException
    {
        super(Modifications.getInstance(cc));
        setAggregateTableIdentifier(aggregateTableIdentifier);
        setModificationNumber(modificationNumber);
        setAggregateRowIdentifier(aggregateRowIdentifier);
    }

    public InternalModification(Entity entity, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Modifications.getInstance(cc), entity);
    }

    public String getAggregateTableIdentifier()
    {
        DataField aggregateTableIdentifierField = getDataField(Modifications.AGGREGATE_TABLE_IDENTIFIER);
        return super.getEntity().getString(aggregateTableIdentifierField);
    }

    public void setAggregateTableIdentifier(String aggregateTableIdentifier)
    {
        super.getEntity().setField(Modifications.AGGREGATE_TABLE_IDENTIFIER, aggregateTableIdentifier);
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

    public String getAggregateRowIdentifier()
    {
        DataField aggregateRowIdentifierField = getDataField(Modifications.AGGREGATE_ROW_IDENTIFIER);
        return super.getEntity().getString(aggregateRowIdentifierField);
    }

    public void setAggregateRowIdentifier(String aggregateRowIdentifier)
    {
        super.getEntity().setField(Modifications.AGGREGATE_ROW_IDENTIFIER, aggregateRowIdentifier);
    }
}
