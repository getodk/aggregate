package org.opendatakit.common.ermodel.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.ExtendedAbstractRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.utils.Check;
import org.opendatakit.common.web.CallingContext;

/**
 * <pre>
 * CallingContext cc;
 * 
 * // create attributes
 * List<Attribute> attributes = new ArrayList<Attribute>();
 * 
 * Attribute name = new Attribute("NAME", AttributeType.STRING, false);
 * Attribute age = new Attribute("AGE", AttributeType.INTEGER, false);
 * 
 * attributes.add(name);
 * attributes.add(age);
 * 
 * // create a relation
 * Relation person = new Relation("MY_NAMESPACE", "PERSON", attributes, cc);
 * 
 * // create and save an entity
 * Entity john = person.newEntity();
 * john.setString("NAME", "John");
 * john.setInteger("AGE", 50);
 * john.save();
 * String johnsIdentifier = john.getAggregateIdentifier();
 * 
 * // retrieve the relation later
 * person = Relation.getRelation("MY_NAMESPACE", "PERSON", cc);
 * 
 * // retrieve the entity later 
 * john = person.getEntity(johnsIdentifier);
 * 
 * // query for entities
 * john = person.query().equal("NAME", "John").get();
 * List<Entity> = person.query().lessThanOrEqual("AGE", 50).execute();
 * </pre>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Relation
{

    private final ExtendedAbstractRelation relation;
    private final String namespace;
    private final String name;

    public Relation(String namespace, String name, List<Attribute> attributes,
            CallingContext cc) throws ODKDatastoreException
    {
        Check.notNullOrEmpty(namespace, "namespace");
        Check.notNullOrEmpty(name, "name");
        Check.notNullOrEmpty(attributes, "attributes");
        Check.notNull(cc, "cc");

        // Add attributes to AttributeRelation
        // Of course, if we are constructing the AttributeRelation itself then we skip this
        if (!name.equals(AttributeRelation.name()))
        {
            for (Attribute attribute : attributes)
            {
                AttributeRelation attributeRelation = AttributeRelation
                        .getInstance(namespace, cc);
                Entity attrEntity = attributeRelation.newEntity();

                attrEntity.set(AttributeRelation.RELATION_NAMESPACE, namespace);
                attrEntity.set(AttributeRelation.RELATION_NAME, name);
                attrEntity.set(AttributeRelation.NAME, attribute.getName());
                attrEntity.set(AttributeRelation.TYPE, attribute.getType()
                        .name());
                attrEntity.set(AttributeRelation.NULLABLE,
                        attribute.isNullable());

                attrEntity.save();
            }
        }

        // Convert to DataFields
        List<DataField> fields = new ArrayList<DataField>();
        for (Attribute attribute : attributes)
        {
            fields.add(attribute.toDataField());
        }

        // Create relation
        this.relation = new ExtendedAbstractRelation(namespace, name, fields,
                cc);
        this.namespace = namespace;
        this.name = name;
    }

    protected Relation(String namespace, String name, CallingContext cc)
            throws ODKDatastoreException
    {
        Check.notNullOrEmpty(namespace, "namespace");
        Check.notNullOrEmpty(name, "name");
        Check.notNull(cc, "cc");

        // Get attributes from AttributeRelation
        List<Attribute> attributes = getAttributes(namespace, name, cc);
        if (attributes.isEmpty())
        {
            throw new ODKDatastoreException(String.format(
                    "Relation '%s' in namespace '%s' does not exist.", name,
                    namespace));
        }
        List<DataField> fields = new ArrayList<DataField>();
        for (Attribute attribute : attributes)
        {
            fields.add(attribute.toDataField());
        }

        // Get relation
        this.relation = new ExtendedAbstractRelation(namespace, name, fields,
                cc);
        this.namespace = namespace;
        this.name = name;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public String getName()
    {
        return name;
    }

    public Attribute getAttribute(String attributeName)
    {
        return Attribute.fromDataField(relation.getDataField(attributeName));
    }

    public List<Attribute> getAttributes() throws ODKDatastoreException
    {
        return getAttributes(namespace, name, relation.getCC());
    }

    private List<Attribute> getAttributes(String namespace, String name,
            CallingContext cc) throws ODKDatastoreException
    {
        List<Attribute> attributes;
        if (name.equals(AttributeRelation.name()))
        {
            attributes = AttributeRelation.attributes();
        } else
        {
            // get attribute entities
            List<Entity> attrEntities = getAttributeEntities(namespace, name,
                    cc);

            // convert Entities to Attributes
            attributes = new ArrayList<Attribute>();
            for (Entity attrEntity : attrEntities)
            {
                String attributeName = attrEntity
                        .getString(AttributeRelation.NAME);
                AttributeType attributeType = AttributeType.valueOf(attrEntity
                        .getString(AttributeRelation.TYPE));
                boolean attributeNullable = attrEntity
                        .getBoolean(AttributeRelation.NULLABLE);
                Attribute attribute = new Attribute(attributeName,
                        attributeType, attributeNullable);
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    private List<Entity> getAttributeEntities(String namespace, String name,
            CallingContext cc) throws ODKDatastoreException
    {
        AttributeRelation attributeRelation = AttributeRelation.getInstance(
                namespace, cc);
        List<Entity> attrEntities = attributeRelation.query()
                .equal(AttributeRelation.RELATION_NAMESPACE, namespace)
                .equal(AttributeRelation.RELATION_NAME, name).execute();
        return attrEntities;
    }

    public Entity getEntity(String aggregateIdentifier)
            throws ODKEntityNotFoundException
    {
        Check.notNullOrEmpty(aggregateIdentifier, "aggregateIdentifier");
        return Entity.fromEntity(relation,
                relation.getEntity(aggregateIdentifier, relation.getCC()));
    }

    public Query query()
    {
        return new Query(this.relation);
    }

    public Entity newEntity()
    {
        return Entity
                .fromEntity(relation, relation.newEntity(relation.getCC()));
    }

    public void dropRelation() throws ODKDatastoreException
    {
        for (Entity attribute : getAttributeEntities(namespace, name,
                relation.getCC()))
        {
            attribute.delete();
        }
        try
        {
            relation.dropRelation(relation.getCC());
        } catch (NullPointerException e)
        {
            throw new ODKDatastoreException(
                    "Relation does not exist in datastore.", e);
        }
    }

    public CallingContext getCC()
    {
        return getCC();
    }

    public static Relation getRelation(String namespace, String name,
            CallingContext cc) throws ODKDatastoreException
    {
        return new Relation(namespace, name, cc);
    }
}
