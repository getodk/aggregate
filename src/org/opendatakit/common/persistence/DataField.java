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
 * <ul><li>name</li><li>dataTye</li></ul>
 * The underlying layers handle treatments to adjust for the maximum 
 * character length that can be stored in the field.  Precision of 
 * retained values is handled by whatever rounding is enforced at the
 * database layer.  So if you put in a 10-significant-digit number but
 * only 8 digits of precision are configured, rounding will occur. 
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public final class DataField {
	
	public static enum DataType {
		BINARY, LONG_STRING, STRING, INTEGER, DECIMAL, BOOLEAN, DATETIME, URI
	}
	
	private String name;
	private DataType dataType;
	private boolean nullable;
	private String persistenceType = null;
	private Long maxCharLen;
	private Integer numericScale;
	private Integer numericPrecision;
	
	public DataField(String name, DataType dataType, boolean nullable) {
		this(name, dataType, nullable, null, null, null);
	}
	
	public DataField(String name, DataType dataType, boolean nullable, Long maxCharLen ) {
		this(name, dataType, nullable, maxCharLen, null, null);
	}
	
	public DataField(String name, DataType dataType, boolean nullable, Long maxCharLen, Integer numericScale, Integer numericPrecision ) {
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

	public String getPersistenceType() {
		return persistenceType;
	}

	public void setPersistenceType(String persistenceType) {
		this.persistenceType = persistenceType;
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
}
