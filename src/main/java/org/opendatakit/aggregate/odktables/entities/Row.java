package org.opendatakit.aggregate.odktables.entities;

import org.opendatakit.aggregate.odktables.relation.Rows;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntity;

public class Row extends TypedEntity
{

    public Row(Rows rows, Entity entity)
    {
        super(rows, entity);
    }
}
