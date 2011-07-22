package org.opendatakit.aggregate.odktables.client.entity;

import org.opendatakit.common.ermodel.simple.AttributeType;

/**
 * Column represents a simple column in a table. A Column has three attributes:
 * <ul>
 * <li>name: a String which is the name of the column</li>
 * <li>type: the type of data that will be put in the column</li>
 * <li>nullable: true if values in the column are allowed to be null</li>
 * </ul>
 * 
 * Column is immutable and therefore threadsafe.
 * 
 * @author the.dylan.price@gmail.com
 */
public final class Column
{

    private final String name;
    private final AttributeType type;
    private final boolean nullable;

    /**
     * So that Gson can serialize this class.
     */
    @SuppressWarnings("unused")
    private Column()
    {
        this.name = null;
        this.type = null;
        this.nullable = true;
    }

    /**
     * Constructs a new Column.
     * 
     * @param name
     *            the name of the Column. This must not be null or empty.
     * @param type
     *            the type of data that the new Column will hold.
     * @param nullable
     *            whether the values in this column are allowed to be null
     */
    public Column(String name, AttributeType type, boolean nullable)
    {
        if (name == null || name.length() == 0)
        {
            throw new IllegalArgumentException("name was null or empty");
        }
        this.name = name;
        this.type = type;
        this.nullable = nullable;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the type
     */
    public AttributeType getType()
    {
        return type;
    }

    /**
     * @return true if this column's values are allowed to be null
     */
    public boolean isNullable()
    {
        return this.nullable;
    }

    @Override
    public String toString()
    {
        return String.format("{Name = %s, Type = %s, Nullable = %s}",
                this.name, this.type, this.nullable);
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Column))
            return false;
        Column o = (Column) other;
        return o.name.equalsIgnoreCase(this.name) && o.type.equals(this.type)
                && o.nullable == this.nullable;
    }

    @Override
    public int hashCode()
    {
        int isNullable = (this.nullable == true) ? 1 : 0;
        return 4 * this.name.hashCode() + 23 * this.type.hashCode()
                + isNullable;
    }
}
