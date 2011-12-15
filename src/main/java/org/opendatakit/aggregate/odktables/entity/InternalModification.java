package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.odktables.relation.Modifications;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * An InternalModification is a (aggregateTableIdentifier, modificationNumber,
 * aggregateRowIdentifier) tuple, where
 * <ul>
 * <li>aggregateTableIdentifier = the universally unique identifier of a table</li>
 * <li>modificationNumber = the number for this modification. modifiationNumbers
 * are incremental with each modification to a table.</li>
 * <li>aggregateRowIdentifier = the universally unique identifier of a row that
 * was inserted or updated in this modification</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalModification extends TypedEntity {

    public InternalModification(String aggregateTableIdentifier,
	    int modificationNumber, String aggregateRowIdentifier,
	    CallingContext cc) throws ODKDatastoreException {
	super(Modifications.getInstance(cc).newEntity());
	setAggregateTableIdentifier(aggregateTableIdentifier);
	setModificationNumber(modificationNumber);
	setAggregateRowIdentifier(aggregateRowIdentifier);
    }

    private InternalModification(Entity entity) {
	super(entity);
    }

    public String getAggregateTableIdentifier() {
	return entity.getString(Modifications.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String value) {
	entity.set(Modifications.AGGREGATE_TABLE_IDENTIFIER, value);
    }

    public int getModificationNumber() {
	return entity.getInteger(Modifications.MODIFICATION_NUMBER);
    }

    public void setModificationNumber(int value) {
	entity.set(Modifications.MODIFICATION_NUMBER, value);
    }

    public String getAggregateRowIdentifier() {
	return entity.getString(Modifications.AGGREGATE_ROW_IDENTIFIER);
    }

    public void setAggregateRowIdentifier(String value) {
	entity.set(Modifications.AGGREGATE_ROW_IDENTIFIER, value);
    }

    @Override
    public String toString() {
	return String
		.format("InternalModification[aggregateTableIdentifier=%s, modificationNumber=%s, aggregateRowIdentifier=%s",
			getAggregateTableIdentifier(), getModificationNumber(),
			getAggregateRowIdentifier());
    }

    public static InternalModification fromEntity(Entity entity) {
	return new InternalModification(entity);
    }
}
