package org.opendatakit.common.ermodel.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * If you are using the simple data layer API to create and store Relations then
 * you don't need to worry about this class.
 * </p>
 * 
 * <p>
 * AttributeRelation is a statically defined Relation that stores the Attributes
 * of each Relation so that clients don't have to. There is one
 * AttributeRelation per namespace.
 * </p>
 * 
 * <p>
 * AttributeRelation has the following Attributes:
 * <ul>
 * <li>relationName: the name of the Relation the Attribute is on</li>
 * <li>name: the name of the Attribute</li>
 * <li>type: the AttributeType of the Attribute</li>
 * <li>nullable: whether the Attribute is allowed to be null</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public final class AttributeRelation extends Relation
{

    // names of the Attributes
    public static final String RELATION_NAME = "RELATION_NAME";
    public static final String NAME = "NAME";
    public static final String TYPE = "TYPE";
    public static final String NULLABLE = "NULLABLE";

    // name of the AttributeRelation in the datastore
    private static final String NAME_OF_ATTRIBUTE_RELATION = "ATTB_5FF06C41_F50B_4420_8B75_F2B2470BC98F";

    // static definition of Attributes
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
        attributes.add(relationName);
        attributes.add(attributeName);
        attributes.add(attributeType);
        attributes.add(nullable);
    }

    // the singleton instance
    private static AttributeRelation instance;

    /**
     * Retrieves the AttributeRelation.
     */
    private AttributeRelation(String namespace, CallingContext cc)
            throws ODKDatastoreException
    {
        super(namespace, NAME_OF_ATTRIBUTE_RELATION, attributes, cc);
    }

    /**
     * @return the name of the AttributeRelation in the datastore.
     */
    public static String name()
    {
        return NAME_OF_ATTRIBUTE_RELATION;
    }

    /**
     * @return the Attributes of the AttributeRelation
     */
    public static List<Attribute> attributes()
    {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Retrieves the AttributeRelation under the given namespace.
     */
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
