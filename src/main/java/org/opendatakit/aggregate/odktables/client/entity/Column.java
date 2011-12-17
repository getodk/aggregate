package org.opendatakit.aggregate.odktables.client.entity;

import org.opendatakit.common.ermodel.simple.AttributeType;

/**
 * <p>
 * Column represents a simple column in a table. A Column has four attributes:
 * <ul>
 * <li>name: a String which is the name of the column</li>
 * <li>type: the type of data that will be put in the column</li>
 * <li>nullable: true if values in the column are allowed to be null</li>
 * <li>properties: a string for the client to store arbitrary metadata</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Column is immutable.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public final class Column {

    private final String name;
    private final AttributeType type;
    private final boolean nullable;
    private final String properties;

    /**
     * So that Gson can serialize this class.
     */
    @SuppressWarnings("unused")
    private Column() {
	this.name = null;
	this.type = null;
	this.nullable = true;
	this.properties = null;
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
     * @param properties
     *            a string that can be any metadata the client wants to store on
     *            the column. May be null or empty.
     */
    public Column(String name, AttributeType type, boolean nullable,
	    String properties) {
	if (name == null || name.length() == 0) {
	    throw new IllegalArgumentException("name was null or empty");
	}
	this.name = name;
	this.type = type;
	this.nullable = nullable;
	this.properties = properties;
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
     * @param properties
     *            a string that can be any metadata the client wants to store on
     *            the column. May be null or empty.
     */
    public Column(String name, AttributeType type, boolean nullable) {
	if (name == null || name.length() == 0) {
	    throw new IllegalArgumentException("name was null or empty");
	}
	this.name = name;
	this.type = type;
	this.nullable = nullable;
	this.properties = null;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @return the type
     */
    public AttributeType getType() {
	return type;
    }

    /**
     * @return true if this column's values are allowed to be null
     */
    public boolean isNullable() {
	return this.nullable;
    }

    public String getProperties() {
	return this.properties;
    }

    @Override
    public String toString() {
	return String.format(
		"{Name = %s, Type = %s, Nullable = %s, Properties = %s}",
		this.name, this.type, this.nullable, this.properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + (nullable ? 1231 : 1237);
	result = prime * result
		+ ((properties == null) ? 0 : properties.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Column other = (Column) obj;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	if (nullable != other.nullable)
	    return false;
	if (properties == null) {
	    if (other.properties != null)
		return false;
	} else if (!properties.equals(other.properties))
	    return false;
	if (type != other.type)
	    return false;
	return true;
    }

}
