package org.opendatakit.common.ermodel.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * @author the.dylan.price@gmail.com
 */
public final class AttributeRelation extends Relation
{

    public static final String RELATION_NAMESPACE = "RELATION_NAMESPACE";
    public static final String RELATION_NAME = "RELATION_NAME";
    public static final String NAME = "NAME";
    public static final String TYPE = "TYPE";
    public static final String NULLABLE = "NULLABLE";

    private static final String NAME_OF_ATTRIBUTE_RELATION = "ATTB_5FF06C41_F50B_4420_8B75_F2B2470BC98F";

    private static final Attribute relationNamespace = new Attribute(
            RELATION_NAMESPACE, AttributeType.STRING, false);
    private static final Attribute relationName = new Attribute(RELATION_NAME,
            AttributeType.STRING, false);
    private static final Attribute attributeName = new Attribute(NAME,
            AttributeType.STRING, false);
    private static final Attribute attributeType = new Attribute(TYPE,
            AttributeType.STRING, false);
    private static final Attribute nullable = new Attribute(NULLABLE,
            AttributeType.BOOLEAN, false);

    private static final List<Attribute> attributes;
    static
    {
        attributes = new ArrayList<Attribute>();
        attributes.add(relationNamespace);
        attributes.add(relationName);
        attributes.add(attributeName);
        attributes.add(attributeType);
        attributes.add(nullable);
    }

    private static AttributeRelation instance;

    private AttributeRelation(String namespace, CallingContext cc)
            throws ODKDatastoreException
    {
        super(namespace, NAME_OF_ATTRIBUTE_RELATION, attributes, cc);
    }

    public static String name()
    {
        return NAME_OF_ATTRIBUTE_RELATION;
    }

    public static List<Attribute> attributes()
    {
        return Collections.unmodifiableList(attributes);
    }

    public static AttributeRelation getInstance(String namespace,
            CallingContext cc) throws ODKDatastoreException
    {
        if (instance == null)
        {
            instance = new AttributeRelation(namespace, cc);
        }
        return instance;
    }
}
