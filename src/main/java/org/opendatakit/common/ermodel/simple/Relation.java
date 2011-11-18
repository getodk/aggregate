package org.opendatakit.common.ermodel.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.ExtendedAbstractRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.utils.Check;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * A Relation represents a relation stored in Aggregate. Think of it like a
 * table.
 * </p>
 * 
 * <p>
 * You can create a new Relation using the constructor and retrieve an existing
 * Relation using {@link Relation#getRelation}.
 * </p>
 * 
 * Here's a 'quick start' on using Relations:
 * 
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
 * john.set("NAME", "John");
 * john.set("AGE", 50);
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

    /**
     * <p>
     * Creates a new Relation or, if the Relation already exists, retrieves it
     * from the datastore.
     * </p>
     * 
     * <p>
     * Rules for namespaces and names:
     * <ul>
     * <li>They must not be null or empty.</li>
     * <li>They must start with a capital letter, and</li>
     * <li>The rest of the characters may be numbers, underscores or capital
     * letters.</li>
     * </ul>
     * </p>
     *
     * <p>
     * For example: 'TABLE' and 'MY_5TH_TABLE' are fine, but 'table' and
     * '5TH_TABLE' are not.
     * </p>
     * 
     * @param namespace
     *            the namespace the relation should live under. See above for
     *            the constraints on namespaces.
     * @param name
     *            the name of the relation. See above for the constraints on
     *            names.
     * @param attributes
     *            the attributes the relation will have. Must not be null or
     *            empty.
     * @param cc
     *            the context. Must not be null.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public Relation(String namespace, String name, List<Attribute> attributes,
            CallingContext cc) throws ODKDatastoreException
    {
        Check.notNullOrEmpty(namespace, "namespace");
        Check.notNullOrEmpty(name, "name");
        Check.notNullOrEmpty(attributes, "attributes");
        Check.notNull(cc, "cc");

        // Add attributes to AttributeRelation
        // if we are constructing the AttributeRelation itself
        // or if this Relation already exists, then we skip this
        if (!name.equals(AttributeRelation.name()))
        {
            AttributeRelation attributeRelation = AttributeRelation
                    .getInstance(namespace, cc);
            boolean relationExists = attributeRelation.query("Relation.constructor")
                    .equal(AttributeRelation.RELATION_NAME, name).exists();

            if (!relationExists)
            {
                for (Attribute attribute : attributes)
                {
                    Entity attrEntity = attributeRelation.newEntity();

                    attrEntity.set(AttributeRelation.RELATION_NAME, name);
                    attrEntity.set(AttributeRelation.NAME, attribute.getName());
                    attrEntity.set(AttributeRelation.TYPE, attribute.getType()
                            .name());
                    attrEntity.set(AttributeRelation.NULLABLE,
                            attribute.isNullable());

                    attrEntity.save();
                }
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

    /**
     * Retrieves the Relation with the given namespace and name. See
     * {@link #Relation(String, String, List, CallingContext) the other
     * constructor} for constraints on namespaces and names.
     * 
     * @param namespace
     *            the namespace of the Relation. Must not be null or empty.
     * @param name
     *            the name of the Relation. Must not be null or empty.
     * @param cc
     * @throws ODKDatastoreException
     */
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

    /**
     * @return the namespace of this Relation.
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * @return the name of this Relation.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param attributeName
     *            the valid name of an Attribute on this Relation. Must not be
     *            null or empty.
     * @return
     */
    public Attribute getAttribute(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        return Attribute.fromDataField(relation.getDataField(attributeName));
    }

    /**
     * @return a list of all Attributes on this Relation.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore.
     */
    public List<Attribute> getAttributes() throws ODKDatastoreException
    {
        return getAttributes(namespace, name, relation.getCC());
    }

    /**
     * Retrieves a list of all Attributes on a relation.
     */
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

    /**
     * Retrieves a list of all entities in the AttributeRelation which are
     * defined for the given namespace and name.
     */
    private List<Entity> getAttributeEntities(String namespace, String name,
            CallingContext cc) throws ODKDatastoreException
    {
        AttributeRelation attributeRelation = AttributeRelation.getInstance(
                namespace, cc);
        List<Entity> attrEntities = attributeRelation.query("Relation.getAttributeEntities")
                .equal(AttributeRelation.RELATION_NAME, name).execute();
        return attrEntities;
    }

    /**
     * Retrieves an Entity in this Relation.
     * 
     * @param aggregateIdentifier
     *            the identifier of the entity (you can get this by calling
     *            {@link Entity#getAggregateIdentifier()}).
     * @return the Entity
     * @throws ODKOverQuotaException 
     * @throws ODKEntityNotFoundException
     *             if there is no Entity with the given identifier stored in
     *             this Relation.
     */
    public Entity getEntity(String aggregateIdentifier)
            throws ODKDatastoreException
    {
        Check.notNullOrEmpty(aggregateIdentifier, "aggregateIdentifier");
        return Entity.fromEntity(relation,
                relation.getEntity(aggregateIdentifier, relation.getCC()));
    }

    /**
     * Constructs a new Query over this Relation.
     */
    public Query query(String loggingContextTag)
    {
        return new Query(this.relation, loggingContextTag);
    }

    /**
     * @return a new Entity for this Relation.
     */
    public Entity newEntity()
    {
        return Entity
                .fromEntity(relation, relation.newEntity(relation.getCC()));
    }

    /**
     * Completely removes this Relation and all of its Entities from the
     * datastore.
     * 
     * @throws ODKDatastoreException
     *             if there was a problem communicating with the datastore.
     */
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

    /**
     * @return the CallingContext this Relation was constructed with.
     */
    public CallingContext getCC()
    {
        return relation.getCC();
    }

    /**
     * Retrieves an existing Relation from the datastore. See
     * {@link #Relation(String, String, List, CallingContext) Relation}
     * regarding constraints on namespaces and names.
     * 
     * @param namespace
     *            the namespace of the Relation.
     * @param name
     *            the name of the Relation.
     * @param cc
     *            the context.
     * @return the Relation specified by namespace and name.
     * @throws ODKDatastoreException
     *             if no such Relation exists or there is an unknown error
     *             communicating with the datastore.
     */
    public static Relation getRelation(String namespace, String name,
            CallingContext cc) throws ODKDatastoreException
    {
        return new Relation(namespace, name, cc);
    }
}
