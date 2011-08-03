package org.opendatakit.common.ermodel.simple.typedentity;

import java.util.Date;

import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;

public class TypedEntity
{

    protected Entity entity;

    public TypedEntity(Entity entity)
    {
        this.entity = entity;
    }

    public String getAggregateIdentifier()
    {
        return entity.getAggregateIdentifier();
    }

    public Date getLastUpdateDate()
    {
        return entity.getLastUpdateDate();
    }

    public Date getCreationDate()
    {
        return entity.getCreationDate();
    }

    public void save() throws ODKEntityPersistException
    {
        entity.save();
    }

    public void delete() throws ODKDatastoreException
    {
        entity.delete();
    }

}
