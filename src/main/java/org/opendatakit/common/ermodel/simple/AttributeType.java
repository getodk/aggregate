package org.opendatakit.common.ermodel.simple;

public enum AttributeType
{
		BINARY, /** blobs -- see BinaryContentManipulator */
		LONG_STRING, /** text -- only used in Aggregate */
		STRING,
        INTEGER, 
        DECIMAL, 
        BOOLEAN, 
        DATETIME,
		URI /** URI is a string of length 80 characters */
}

