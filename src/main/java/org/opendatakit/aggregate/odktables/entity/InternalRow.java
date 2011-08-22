package org.opendatakit.aggregate.odktables.entity;

import java.util.UUID;

import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * An InternalRow is a set of values pertaining to the fields of a specific
 * table.
 * </p>
 * 
 * <p>
 * Every Row has a revisionTag, which is a randomly generated uuid that should
 * be updated every time the Row is changed.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class InternalRow extends TypedEntity
{

    public InternalRow(String aggregateTableIdentifier, CallingContext cc)
            throws ODKDatastoreException
    {
        super(Table.getInstance(aggregateTableIdentifier, cc).newEntity());
        updateRevisionTag();
    }

    public InternalRow(Entity entity)
    {
        super(entity);
    }

    public String getRevisionTag()
    {
        return entity.getString(Table.REVISION_TAG);
    }

    public void updateRevisionTag()
    {
        entity.set(Table.REVISION_TAG, UUID.randomUUID().toString());
    }

    public String getValue(String aggregateAttributeIdentifier)
    {
        return entity.getAsString(Table
                .convertIdentifier(aggregateAttributeIdentifier));
    }

    public void setValue(String aggregateAttributeIdentifier, String value)
    {
        entity.setAsString(
                Table.convertIdentifier(aggregateAttributeIdentifier), value);
    }

    @Override
    public String toString()
    {
        return String.format(
                "InternalRow[aggregateRowIdentifier=%s, revisionTag=%s",
                getAggregateIdentifier(), getRevisionTag());
    }

    public static InternalRow fromEntity(Entity entity)
    {
        return new InternalRow(entity);
    }
}
