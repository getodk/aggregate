package org.opendatakit.aggregate.odktables.entity;

import java.util.UUID;

import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Row is a set of values pertaining to the fields of a specific table.
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
        super(Table.getInstance(aggregateTableIdentifier, cc));
        updateRevisionTag();
    }

    public InternalRow(Table rows, Entity entity)
    {
        super(rows, entity);
    }

    public String getRevisionTag()
    {
        return super.getEntity().getField(Table.REVISION_TAG);
    }

    public void updateRevisionTag()
    {
        super.getEntity().setField(Table.REVISION_TAG,
                UUID.randomUUID().toString());
    }

    public String getValue(String aggregateFieldIdentifier)
    {
        return super.getEntity().getField(
                Table.convertIdentifier(aggregateFieldIdentifier));
    }

    public void setValue(String aggregateFieldIdentifier, String value)
    {
        super.getEntity().setField(
                Table.convertIdentifier(aggregateFieldIdentifier), value);
    }

    @Override
    public String toString()
    {
        return String.format(
                "InternalRow[aggregateRowIdentifier=%s, revisionTag=%s",
                getAggregateIdentifier(), getRevisionTag());
    }
}
