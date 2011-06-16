package org.opendatakit.common.ermodel.typedentity;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;


/**
 * A RelationEntity defines an entity for a specific relation.
 * 
 * @author the.dylan.price@gmail.com
 *
 */
public abstract class TypedEntity
{
    private TypedEntityRelation<?> relation;
    private Entity entity;
    
    protected TypedEntity(TypedEntityRelation<?> relation)
    {
        this.relation = relation;
        this.entity = relation.newEntity(getCC());
    }
    
    protected TypedEntity(TypedEntityRelation<?> relation, Entity entity)
    {
       this.relation = relation;
       this.entity = entity;
    }
    
    protected Entity getEntity()
    {
        return this.entity;
    }
    
    protected CallingContext getCC()
    {
        return relation.getCC();
    }
    
    protected DataField getDataField(String fieldName)
    {
        return relation.getDataField(fieldName);
    }
    
    public String getUri()
    {
        return this.entity.getUri();
    }
    
    public void save() throws ODKEntityPersistException
    {
        this.entity.persist(getCC());
    }
    
    public void delete() throws ODKDatastoreException
    {
        try
        {
            this.entity.remove(getCC());
        }
        catch (ODKDatastoreException e)
        {
            // try one more time
            this.entity.remove(getCC());
        }
    }
}
