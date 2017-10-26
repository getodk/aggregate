/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence;

/**
 * DataField bridges the layer between the persistence implementation and 
 * the upper layers of the Entity.  The upper layers assume immutable
 * properties of:
 * <ul><li>name</li><li>dataType</li></ul>
 * The underlying layers handle treatments to adjust for the maximum 
 * character length that can be stored in the field.  Precision of 
 * retained values is handled by whatever rounding is enforced at the
 * database layer.  So if you put in a 10-significant-digit number but
 * only 8 digits of precision are configured, rounding will occur. 
 * <p>Note that DATETIME values may be rounded to the nearest second.
 * See {@link PersistConsts} for limitations.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public final class DataField {
	
	public static enum DataType {
		BINARY /** blobs -- see BinaryContentManipulator */,
		LONG_STRING /** text -- i.e., utf-8 blob */,
		STRING, INTEGER, DECIMAL, BOOLEAN, DATETIME,
		URI /** URI is a string of length 80 characters */
	}
	public static enum IndexType {
		NONE, ORDERED, HASH
	}
	
	private String name;
	private DataType dataType;
	private boolean nullable;
	private Long maxCharLen;
	private Integer numericScale;
	private Integer numericPrecision;
	private IndexType indexable = IndexType.NONE; // clue for persistence layer to define index.
	private boolean isDoublePrecision = false;
	
	/**
	 * Constructor for most uses.
	 * <ul>
	 * <li>Strings default to 250 characters.</li>
	 * <li>Integers to 32-bit values.</li>
	 * <li>Decimals to double-precision values.</li></ul>
	 *  If you are looking for blobs or long text, see the BinaryContentManipulator class.
	 * 
	 * @param name The standard naming convention for database persistence layers 
	 * 			is underscore-delimited all-upper-case names.  E.g., 
	 * 			in Java, you might name a variable "descriptiveDataValue".
	 * 			in the perisistence layer, this would typically be named "DESCRIPTIVE_DATA_VALUE".
	 * @param dataType
	 * @param nullable
	 */
	public DataField(String name, DataType dataType, boolean nullable) {
		this(name, dataType, nullable, null, null, null);
	}
	
	/**
	 * Constructor for string fields that are longer than 250 characters.
	 * The underlying datastore does not allow any one entity to be larger than
	 * about 65000 bytes.  Documents should use binary content tables.
	 * 
	 * @param name The standard naming convention for database persistence layers 
	 * 			is underscore-delimited all-upper-case names.  E.g., 
	 * 			in Java, you might name a variable "descriptiveDataValue".
	 * 			in the perisistence layer, this would typically be named "DESCRIPTIVE_DATA_VALUE".
	 * @param dataType
	 * @param nullable
	 * @param maxCharLen
	 */
	public DataField(String name, DataType dataType, boolean nullable, Long maxCharLen ) {
		this(name, dataType, nullable, maxCharLen, null, null);
	}
	
	/**
	 * Constructor for extended-precision integer or numeric fields.
	 * The default corresponds to 32-bit integers and double-precision 
	 * floating point.  It is not useful to shorten these, as this 
	 * generally slows down your code.
	 * 
	 * @param name The standard naming convention for database persistence layers 
	 * 			is underscore-delimited all-upper-case names.  E.g., 
	 * 			in Java, you might name a variable "descriptiveDataValue".
	 * 			in the perisistence layer, this would typically be named "DESCRIPTIVE_DATA_VALUE".
	 * @param dataType
	 * @param nullable
	 * @param maxCharLen
	 */
	public DataField(String name, DataType dataType, boolean nullable, Integer numericScale, Integer numericPrecision ) {
		this(name, dataType, nullable, null, numericScale, numericPrecision);
	}
	
	private DataField(String name, DataType dataType, boolean nullable, Long maxCharLen, Integer numericScale, Integer numericPrecision ) {
		this.name = name;
		this.dataType = dataType;
		this.nullable = nullable;
		this.maxCharLen = maxCharLen;
		this.numericScale = numericScale;
		this.numericPrecision = numericPrecision;
	}
	
	public DataField(final DataField src) {
		this.name = src.name;
		this.dataType = src.dataType;
		this.nullable = src.nullable;
		this.maxCharLen = src.maxCharLen;
		this.numericScale = src.numericScale;
		this.numericPrecision = src.numericPrecision;
		this.isDoublePrecision = src.isDoublePrecision;
		this.indexable = src.indexable;
	}
	
	public String getName() {
		return name;
	}
	
	public DataType getDataType() {
		return dataType;
	}
	
	public boolean getNullable() {
		return nullable;
	}

	public boolean isDoublePrecision() {
		return isDoublePrecision;
	}

	public DataField asDoublePrecision(boolean isDoublePrecision) {
		this.isDoublePrecision = isDoublePrecision;
		return this;
	}

	public Long getMaxCharLen() {
		return maxCharLen;
	}

	public void setMaxCharLen(Long maxCharLen) {
		this.maxCharLen = maxCharLen;
	}
	
	public Integer getNumericScale() {
		return numericScale;
	}

	public void setNumericScale(Integer numericScale) {
		this.numericScale = numericScale;
	}

	public Integer getNumericPrecision() {
		return numericPrecision;
	}

	public void setNumericPrecision(Integer numericPrecision) {
		this.numericPrecision = numericPrecision;
	}
	
	public DataField setIndexable(IndexType type) {
		this.indexable = type;
		return this;
	}
	
	public IndexType getIndexable() {
		return indexable;
	}
}
