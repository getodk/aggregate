package org.opendatakit.aggregate.odktables.client.exception;

import org.opendatakit.common.ermodel.simple.AttributeType;

public class FilterValueTypeMismatchException extends ODKTablesClientException {
    private static final long serialVersionUID = -5242544319592702552L;

    private final AttributeType type;
    private final String value;

    public FilterValueTypeMismatchException(AttributeType type, String value) {
	super(String.format("Value %s can not be interpreted as type %s",
		value, type.toString()));
	this.type = type;
	this.value = value;
    }

    /**
     * @return the type
     */
    public AttributeType getType() {
	return type;
    }

    /**
     * @return the value
     */
    public String getValue() {
	return value;
    }
}
