package org.opendatakit.common.ermodel.simple;

import java.math.BigDecimal;
import java.util.Date;

import org.opendatakit.common.ermodel.ExtendedAbstractRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.utils.Check;

/**
 * Entity represents an entity in a Relation. Think of it like a row in a table.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class Entity
{
    private ExtendedAbstractRelation relation;
    private org.opendatakit.common.ermodel.Entity entity;

    private Entity(ExtendedAbstractRelation relation,
            org.opendatakit.common.ermodel.Entity entity)
    {
        Check.notNull(relation, "relation");
        Check.notNull(entity, "entity");
        this.relation = relation;
        this.entity = entity;
    }

    /**
     * @return the unique identifier of this Entity. You can later retrieve this
     *         Entity using {@link Relation#getEntity(String)}.
     */
    public String getAggregateIdentifier()
    {
        return this.entity.getUri();
    }

    /**
     * @return the Date of the last time this Entity was saved to the datastore.
     */
    public Date getLastUpdateDate()
    {
        return this.entity.getLastUpdateDate();
    }

    /**
     * @return the Date that this Entity was first saved to the datastore.
     */
    public Date getCreationDate()
    {
        return this.entity.getCreationDate();
    }

    public Boolean getBoolean(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getBoolean(field);
    }

    public Date getDate(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getDate(field);
    }

    public Double getDouble(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getDouble(field);
    }

    public BigDecimal getNumeric(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getNumeric(field);
    }

    public Integer getInteger(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getInteger(field);
    }

    public Long getLong(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getLong(field);
    }

    public String getString(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getString(field);
    }

    public void set(String attributeName, Boolean value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setBoolean(field, value);
    }

    public void set(String attributeName, Date value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setDate(field, value);
    }

    public void set(String attributeName, Double value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setDouble(field, value);
    }

    public void set(String attributeName, BigDecimal value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setNumeric(field, value);
    }

    public void set(String attributeName, Integer value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setInteger(field, value);
    }

    public void set(String attributeName, Long value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setLong(field, value);
    }

    public void set(String attributeName, String value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setString(field, value);
    }

    /**
     * Saves this Entity to the datastore.
     * 
     * @throws ODKEntityPersistException
     *             if there was a problem saving the Entity.
     */
    public void save() throws ODKEntityPersistException
    {
        this.entity.persist(this.relation.getCC());
    }

    /**
     * Deletes this Entity from the datastore.
     * 
     * @throws ODKDatastoreException
     *             if there was a problem deleting this Entity.
     */
    public void delete() throws ODKDatastoreException
    {
        this.entity.remove(this.relation.getCC());
    }

    /**
     * Creates an Entity from an org.opendatakit.common.ermodel.Entity
     */
    protected static Entity fromEntity(ExtendedAbstractRelation relation,
            org.opendatakit.common.ermodel.Entity entity)
    {
        return new Entity(relation, entity);
    }
}
