package org.opendatakit.common.ermodel.simple;

import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.utils.Check;

/**
 * <p>
 * An Attribute represents an attribute on a Relation. Think of it like the
 * column of a table.
 * </p>
 * 
 * <p>
 * Attributes are immutable.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Attribute
{
    private final String name;
    private final AttributeType type;
    private final boolean nullable;

    /**
     * <p>
     * Constructs a new Attribute.
     * </p>
     * 
     * <p>
     * The constraints on Attribute names are the same as those of namespaces
     * and names. See
     * {@link Relation#Relation(String, String, java.util.List, org.opendatakit.common.web.CallingContext)
     * Relation's constructor} for more info.
     * </p>
     * 
     * @param name
     *            the name of the Attribute. Must not be emtpy or null.
     * @param type
     *            the type of the Attribute. Must not be null.
     * @param nullable
     *            true if the Attribute is allowed to be null.
     */
    public Attribute(String name, AttributeType type, boolean nullable)
    {
        Check.notNullOrEmpty(name, "name");
        Check.notNull(type, "type");

        this.name = name;
        this.type = type;
        this.nullable = nullable;
    }

    /**
     * @return the name of this Attribute.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the type of this Attribute.
     */
    public AttributeType getType()
    {
        return this.type;
    }

    /**
     * @return true if this Attribute is allowed to be null.
     */
    public boolean isNullable()
    {
        return this.nullable;
    }

    /**
     * @return this Attribute converted to a DataField.
     */
    protected DataField toDataField()
    {
        return new DataField(this.name, DataType.valueOf(this.type.name()),
                this.nullable);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("Attribute [name=%s, type=%s, nullable=%s]", name,
                type, nullable);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nullable ? 1231 : 1237);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Attribute))
            return false;
        Attribute other = (Attribute) obj;
        if (name == null)
        {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (nullable != other.nullable)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    /**
     * @return a new Attribute which is converted from the given DataField.
     */
    protected static Attribute fromDataField(DataField field)
    {
        String name = field.getName();
        AttributeType type = AttributeType.valueOf(field.getDataType().name());
        boolean nullable = field.getNullable();
        return new Attribute(name, type, nullable);
    }
}
