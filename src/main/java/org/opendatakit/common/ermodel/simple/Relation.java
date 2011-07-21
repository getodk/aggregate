package org.opendatakit.common.ermodel.simple;

import java.util.List;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.ExtendedAbstractRelation;
import org.opendatakit.common.ermodel.TableNamespace;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * @author the.dylan.price@gmail.com
 */
public class Relation
{

    private ExtendedAbstractRelation relation;

    public Relation(String namespace, String name,
            List<Attribute> attributes, CallingContext cc)
            throws ODKDatastoreException
    {
        Check.notNullOrEmpty(namespace, "namespace");
        Check.notNullOrEmpty(name, "name");
        Check.notNullOrEmpty(attributes, "attributes");
        Check.notNull(cc, "cc");

        // Add attributes to AttributeRelation
        AttributeRelation attributeRelation = AttributeRelation.getInstance(namespace, cc);
        List<DataField> fields = new ArrayList<DataField>();
        for (Attribute attribute : attributes)
        {
            fields.add(attribute.toDataField());
            Entity attribute = attributeRelation.newEntity();
            attribute.setString(AttributeRelation.NAME, attribute.getName());
            attribute.setString(AttributeRelation.TYPE, attribute.getType().name());
            attribute.setString(AttributeRelation.NULLABLE, attribute.isNullable());
            attribute.save();
        }

        // Create relation
        this.relation = new ExtendedAbstractRelation(namespace, name,
                fields, cc);
    }

    private Relation(String namespace, String name, CallingContext cc)
    {
        Check.notNullOrEmpty(namespace, "namespace");
        Check.notNullOrEmpty(name, "name");
        Check.notNull(cc, "cc");

        // Get attributes from AttributeRelation
        AttributeRelation attributeRelation = AttributeRelation.getInstance(namespace, cc);
        List<Entity> attributes = attributeRelation.query().equal(AttributeRelation.RELATION_NAMESPACE, namespace).equal(AttributeRelation.RELATION_NAME, name).execute();
        List<DataField> fields = new ArrayList<DataField>();
        for (Entity attribute : attributes)
        {
            String attributeName = attribute.getString(AttributeRelation.NAME);
            String attributeType = DataType.valueOf(attribute.getString(AttributeRelation.TYPE));
            boolean attributeNullable = attribute.getBoolean(AttributeRelation.NULLABLE);
            DataField field = new DataField(attributeName, attributeType, attributeNullable);
            fields.add(field);
        }

        // Get relation
        this.relation = new ExtendedAbstractRelation(namespace, name, fields, cc);
    }

    public Entity getEntity(String aggregateIdentifier)
    {
        Check.notNullOrEmpty(aggregateIdentifier, "aggregateIdentifier");
        return Entity.fromEntity(this.relation.getEntity(aggregateIdentifier, this.relation.getCC()));
    }

    public Query query()
    {
        return new Query(this.relation);
    }

    public Entity newEntity()
    {
        return Entity.fromEntity(this.relation.newEntity());
    }

    public void dropRelation()
    {
        this.relation.dropRelation();
    }

    public static Relation getRelation(String namespace, String name, CallingContext cc)
    {
        return new Relation(namespace, name, cc);
    }
}

