package org.opendatakit.common.ermodel.typedentity;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A TypedEntity is an entity which belongs to a specific relation.
 * </p>
 * 
 * <p>
 * TypedEntity is mutable and not threadsafe.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public abstract class TypedEntity
{
    /**
     * The relation this TypedEntity belongs to.
     */
    private TypedEntityRelation<?> relation;

    /**
     * The generic entity backing this TypedEntity.
     */
    private Entity entity;

    /**
     * Creates a new, empty TypedEntity from the given relation.
     * 
     * @param relation
     *            the relation to create an entity for.
     */
    protected TypedEntity(TypedEntityRelation<?> relation)
    {
        this.relation = relation;
        this.entity = relation.newEntity(getCC());
    }

    /**
     * Creates a TypedEntity which is backed by the given existing entity.
     * 
     * @param relation
     *            the relation the given entity belongs to.
     * @param entity
     *            the generic entity. This entity must be an entity of the given
     *            relation.
     */
    protected TypedEntity(TypedEntityRelation<?> relation, Entity entity)
    {
        this.relation = relation;
        this.entity = entity;
    }

    /**
     * @return the backing generic entity of this TypedEntity.
     */
    protected Entity getEntity()
    {
        return this.entity;
    }

    /**
     * @return the CallingContext saved by the relation this TypedEntity belongs to.
     */
    protected CallingContext getCC()
    {
        return relation.getCC();
    }

    /**
     * {@link org.opendatakit.common.ermodel.Relation#getDataField(String)}
     */
    protected DataField getDataField(String fieldName)
    {
        return relation.getDataField(fieldName);
    }

    /**
     * {@link org.opendatakit.common.ermodel.Entity#getUri()}
     */
    public String getUri()
    {
        return this.entity.getUri();
    }

    /**
     * Saves this TypedEntity to the datastore.
     * 
     * @throws ODKEntityPersistException
     */
    public void save() throws ODKEntityPersistException
    {
        this.entity.persist(getCC());
    }

    /**
     * Permanently deletes this TypedEntity from the datastore.
     * 
     * @throws ODKDatastoreException
     */
    public void delete() throws ODKDatastoreException
    {
        try
        {
            this.entity.remove(getCC());
        } catch (ODKDatastoreException e)
        {
            // try one more time
            this.entity.remove(getCC());
        }
    }
}
