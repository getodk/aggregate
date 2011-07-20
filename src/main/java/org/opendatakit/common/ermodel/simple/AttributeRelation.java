package org.opendatakit.common.ermodel.simple;

/**
 * @author the.dylan.price@gmail.com
 */
protected class AttributeRelation
{

    public static final String RELATION_NAMESPACE = "RELATION_NAMESPACE";
    public static final String RELATION_NAME = "RELATION_NAME";
    public static final String NAME = "NAME";
    public static final String TYPE = "TYPE";
    public static final String NULLABLE = "NULLABLE";

    private static final String RELATION_NAME = AttributeRelation.class.getCanonicalName() + "_5ff06c41-f50b-4420-8b75-f2b2470bc98f";

    private static final Attribute relationNamespace = new Attribute(RELATION_NAMESPACE, AttributeType.STRING, false);
    private static final Attribute relationName = new Attribute(RELATION_NAME, AttributeType.STRING, false);
    private static final Attribute attributeName = new Attribute(NAME, AttributeType.STRING, false);
    private static final Attribute attributeType = new Attrubte(TYPE, AttributeType.STRING, false);
    private static final Attribute nullable = new Attribute(NULLABLE, AttributeType.BOOLEAN, false);

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

    private AttrributeRelation(String namespace, CallingContext cc)
    {
        super(namespace, RELATION_NAME, attributes, cc);
    }

    public static AttributeRelation getInstance(String namespace, CallingContext cc)
    {
        if (instance == null)
        {
            instance = new AttributeRelation(namespace, cc);
        }
        return instance;
    }
}
