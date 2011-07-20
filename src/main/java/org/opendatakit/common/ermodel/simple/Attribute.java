package org.opendatakit.common.ermodel.simple;

public class Attribute
{
    private final String name;
    private final AttributeType type;
    private final boolean nullable;

    public Attribute(String name, AttributeType type, boolean nullable)
    {
        Check.notNullOrEmpty(name, "name");
        Check.notNull(type);

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
}

