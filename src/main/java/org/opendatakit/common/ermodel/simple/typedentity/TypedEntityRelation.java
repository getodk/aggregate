package org.opendatakit.common.ermodel.simple.typedentity;

import java.util.List;

import org.opendatakit.common.ermodel.simple.Attribute;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

/**
 * @author the.dylan.price@gmail.com
 * 
 * @param <T>
 *            the type of entity which will be stored in this relation.
 */
public abstract class TypedEntityRelation<T extends TypedEntity>
{

    private Relation relation;

    protected TypedEntityRelation(String namespace, String name,
            List<Attribute> attributes, CallingContext cc)
            throws ODKDatastoreException
    {
        this.relation = new Relation(namespace, name, attributes, cc);
    }

    protected TypedEntityRelation(String namespace, String name,
            CallingContext cc) throws ODKDatastoreException
    {
        this.relation = Relation.getRelation(namespace, name, cc);
    }

    /**
     * Initializes a typed entity using the given generic entity.
     * 
     * @param entity
     *            a generic entity from this relation
     * @return a typed entity based on the given generic entity
     * @throws ODKDatastoreException
     */
    protected abstract T initialize(Entity entity) throws ODKDatastoreException;

    public String getNamespace()
    {
        return relation.getNamespace();
    }

    public String getName()
    {
        return relation.getName();
    }

    public List<Attribute> getAttributes() throws ODKDatastoreException
    {
        return relation.getAttributes();
    }

    /**
     * Retrieve a typed entity by it's aggregate identifier.
     * 
     * @param aggregateIdentifier
     *            the global unique identifier of an entity in this relation.
     *            Must be a valid aggregate identifier and an entity with the
     *            identifier must exist in this relation.
     * @return the entity with the given aggregate identifier
     * @throws ODKDatastoreException
     * @throws ODKEntityNotFoundException
     *             if no such entity exists in the datastore
     */
    public T getEntity(String aggregateIdentifier) throws ODKDatastoreException
    {
        return initialize(this.relation.getEntity(aggregateIdentifier));
    }

    /**
     * Creates a new empty query.
     * 
     * @return a new empty query over this relation, that is a query with no
     *         filter or sort criteria.
     */
    public TypedEntityQuery<T> query(String loggingContextTag)
    {
        return new TypedEntityQuery<T>(this, relation.query(loggingContextTag));
    }

    public Entity newEntity() throws ODKDatastoreException
    {
        return relation.newEntity();
    }

    public void dropRelation() throws ODKDatastoreException
    {
        relation.dropRelation();
    }

    public CallingContext getCC()
    {
        return relation.getCC();
    }
}