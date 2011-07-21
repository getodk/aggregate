package org.opendatakit.common.ermodel.simple;

import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.utils.Check;

public class Attribute
{
    private final String name;
    private final AttributeType type;
    private final boolean nullable;

    public Attribute(String name, AttributeType type, boolean nullable)
    {
        Check.notNullOrEmpty(name, "name");
        Check.notNull(type, "type");

        this.name = name;
        this.type = type;
        this.nullable = nullable;
    }

    public String getName()
    {
        return this.name;
    }

    public AttributeType getType()
    {
        return this.type;
    }

    public boolean isNullable()
    {
        return this.nullable;
    }

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
}
