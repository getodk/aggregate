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

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.security.User;

/**
 * Base class defining the audit fields for a table.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public abstract class CommonFieldsBase {

	public static enum BaseType {
		STATIC,
		STATIC_ASSOCIATION,
		DYNAMIC,
		DYNAMIC_DOCUMENT,
		DYNAMIC_ASSOCIATION
	}
	/** standard audit fields */
	
	/** creator */
	private static final DataField CREATOR_URI_USER = new DataField("_CREATOR_URI_USER",DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN);
	/** creation date */
	private static final DataField CREATION_DATE = new DataField("_CREATION_DATE", DataField.DataType.DATETIME, false);
	/** last user to update record */
	private static final DataField LAST_UPDATE_URI_USER = new DataField("_LAST_UPDATE_URI_USER", DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN);
	/** last update date */
	private static final DataField LAST_UPDATE_DATE = new DataField("_LAST_UPDATE_DATE", DataField.DataType.DATETIME, false);

	/** primary key for all tables */
	private static final DataField URI = new DataField("_URI", DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN);

	/** containment within all dynamic_* tables */
	
	/** key into the dynamic table that is our top level (colocation) container */
	private static final DataField TOP_LEVEL_AURI = new DataField("_TOP_LEVEL_AURI", DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN);
	
	/* dynamic */
	
	/** key into the dynamic table that is our parent container */
	private static final DataField PARENT_AURI = new DataField("_PARENT_AURI", DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN);
	/** ordinal (1st, 2nd, ... ) of this item in the form element */
	private static final DataField ORDINAL_NUMBER = new DataField("_ORDINAL_NUMBER", DataField.DataType.INTEGER, false);
	
	/** association
	 * <p>
	 * The tables to which the DOM (dominant) and SUB (subordinate) AURIs point
	 * can be determined by the model information for this table (what is the 
	 * enclosing form element for this table name; what is the nested element).
	 * If types are ambiguous, then the table should include information to
	 * resolve the ambiguity. 
	 */
	
	/** key into the dynamic table for the dominant relation */
	private static final DataField DOM_AURI = new DataField("_DOM_AURI", DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN );
	/** key into the dynamic table for the subordinate relation */
	private static final DataField SUB_AURI = new DataField("_SUB_AURI", DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN );

	/** member variables */
	protected final String schemaName;
	protected final String tableName;
	protected final BaseType tableType;
	private boolean fromDatabase = false;
	private Object opaquePersistenceData = null;
	protected final List<DataField> fieldList = new ArrayList<DataField>();
	protected final Map<DataField, Object> fieldValueMap = new HashMap<DataField, Object>();
	
	public final DataField primaryKey;
	public final DataField topLevelAuri;
	public final DataField parentAuri;
	public final DataField ordinalNumber;
	public final DataField domAuri;
	public final DataField subAuri;
	public final DataField creatorUriUser;
	public final DataField creationDate;
	public final DataField lastUpdateUriUser;
	public final DataField lastUpdateDate;
	
	/** 
	 * Copy constructor to be invoked through getEmptyRow().
	 * This does NOT copy the fieldValueMap.
	 *  
	 * @param ref
	 */
	protected CommonFieldsBase(CommonFieldsBase ref) {
		schemaName = ref.schemaName;
		tableName = ref.tableName;
		tableType = ref.tableType;
		
		primaryKey = ref.primaryKey;
		topLevelAuri = ref.topLevelAuri;
		parentAuri = ref.parentAuri;
		ordinalNumber = ref.ordinalNumber;
		domAuri = ref.domAuri;
		subAuri = ref.subAuri;
		creatorUriUser = ref.creatorUriUser;
		creationDate = ref.creationDate;
		lastUpdateUriUser = ref.lastUpdateUriUser;
		lastUpdateDate = ref.lastUpdateDate;

		fieldList.addAll(ref.fieldList);
	}

	protected CommonFieldsBase(String schemaName, String tableName, BaseType tableType) {
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.tableType = tableType;
		// always primary key with the same name...
		fieldList.add(primaryKey = new DataField(URI));
		// top level auri is only non-null for non-static tables 
		if ((tableType == BaseType.STATIC) || (tableType == BaseType.STATIC_ASSOCIATION)) {
			topLevelAuri = null;
		} else {
			fieldList.add(topLevelAuri=new DataField(TOP_LEVEL_AURI));
		}
		
		// add fields specific to the table type...
		switch ( tableType ) {
		case STATIC:
		case DYNAMIC_DOCUMENT:
			domAuri = null;
			subAuri = null;
			parentAuri = null;
			ordinalNumber = null;
			break;
		case STATIC_ASSOCIATION:
		case DYNAMIC_ASSOCIATION:
			fieldList.add(domAuri=new DataField(DOM_AURI));
			fieldList.add(subAuri=new DataField(SUB_AURI));
			parentAuri = null;
			ordinalNumber = null;
			break;
		case DYNAMIC:
			domAuri = null;
			subAuri = null;
			fieldList.add(parentAuri=new DataField(PARENT_AURI));
			fieldList.add(ordinalNumber=new DataField(ORDINAL_NUMBER));
			break;
		default:
			throw new IllegalArgumentException("Unrecognized BaseType");
		}
		
		// and add audit fields everywhere...
		fieldList.add(creatorUriUser=new DataField(CREATOR_URI_USER));
		fieldList.add(creationDate=new DataField(CREATION_DATE));
		fieldList.add(lastUpdateUriUser=new DataField(LAST_UPDATE_URI_USER));
		fieldList.add(lastUpdateDate=new DataField(LAST_UPDATE_DATE));
	}

	public final String getSchemaName() {
		return schemaName;
	}

	public final String getTableName() {
		return tableName;
	}

	/**
	 * @return the primary key value for this row
	 */
	public final String getUri() {
		return getStringField(primaryKey);
	}
	
	public final String getTopLevelAuri() {
		if (( tableType == BaseType.STATIC) || (tableType == BaseType.STATIC_ASSOCIATION)) {
			throw new IllegalStateException("Attempting to get topLevelAuri of non-DYNAMIC table");
		}
		return getStringField(topLevelAuri);
	}
	
	public final void setTopLevelAuri(String value) {
		if (( tableType == BaseType.STATIC) || (tableType == BaseType.STATIC_ASSOCIATION)) {
			throw new IllegalStateException("Attempting to set topLevelAuri of non-DYNAMIC table");
		}
		if ( ! setStringField(topLevelAuri, value) ) {
			throw new IllegalStateException("overflow on topLevelAuri");
		}
	}
	
	public final String getParentAuri() {
		if ( tableType != BaseType.DYNAMIC ) {
			throw new IllegalStateException("Attempting to get parentAuri of non-DYNAMIC table");
		}
		return getStringField(parentAuri);
	}
	
	public final void setParentAuri(String value) {
		if ( tableType != BaseType.DYNAMIC ) {
			throw new IllegalStateException("Attempting to set parentAuri of non-DYNAMIC table");
		}
		if ( ! setStringField(parentAuri, value) ) {
			throw new IllegalStateException("overflow on parentAuri");
		}
	}

	public final Long getOrdinalNumber() {
		if ( tableType != BaseType.DYNAMIC ) {
			throw new IllegalStateException("Attempting to get ordinalNumber of non-DYNAMIC table");
		}
		return getLongField(ordinalNumber);
	}

	public final void setOrdinalNumber(Long value) {
		if ( tableType != BaseType.DYNAMIC ) {
			throw new IllegalStateException("Attempting to set ordinalNumber of non-DYNAMIC table");
		}
		setLongField(ordinalNumber, value);
	}
	
	public final String getDomAuri() {
		if ( !((tableType == BaseType.STATIC_ASSOCIATION) || (tableType == BaseType.DYNAMIC_ASSOCIATION))) {
			throw new IllegalStateException("Attempting to get domAuri of non-ASSOCIATION table");
		}
		return getStringField(domAuri);
	}
	
	public final void setDomAuri(String value) {
		if ( !((tableType == BaseType.STATIC_ASSOCIATION) || (tableType == BaseType.DYNAMIC_ASSOCIATION))) {
			throw new IllegalStateException("Attempting to set domAuri of non-ASSOCIATION table");
		}
		if ( !setStringField(domAuri, value) ) {
			throw new IllegalStateException("overflow on domAuri");
		}
	}

	public final String getSubAuri() {
		if ( !((tableType == BaseType.STATIC_ASSOCIATION) || (tableType == BaseType.DYNAMIC_ASSOCIATION))) {
			throw new IllegalStateException("Attempting to get subAuri of non-ASSOCIATION table");
		}
		return getStringField(subAuri);
	}

	public final void setSubAuri(String value) {
		if ( !((tableType == BaseType.STATIC_ASSOCIATION) || (tableType == BaseType.DYNAMIC_ASSOCIATION))) {
			throw new IllegalStateException("Attempting to set subAuri of non-ASSOCIATION table");
		}
		if ( !setStringField(subAuri, value) ) {
			throw new IllegalStateException("overflow on subAuri");
		}
	}

	public final String getCreatorUriUser() {
		return getStringField(creatorUriUser);
	}

	public final Date getCreationDate() {
		return getDateField(creationDate);
	}

	public final String getLastUpdateUriUser() {
		return getStringField(lastUpdateUriUser);
	}

	public final Date getLastUpdateDate() {
		return getDateField(lastUpdateDate);
	}

	public final BaseType getTableType() {
		return tableType;
	}
	
	public final DataField getPrimaryKey() {
		return primaryKey;
	}

	public final List<DataField> getFieldList() {
		return fieldList;
	}

	public final String getStringField(DataField f) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		Object o = fieldValueMap.get(f);
		if ( o == null ) return null;
		return (String) o;
	}
	
	/**
	 * Set the given field to the given value.  If the value is too long,
	 * the prefix is stored and false is returned.
	 * 
	 * @param f field to set
	 * @param value string value for field
	 * @return false if the value had to be truncated.
	 */
	public final boolean setStringField(DataField f, String value) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		if (!(( f.getDataType() == DataType.STRING ) || 
		      ( f.getDataType() == DataType.LONG_STRING ) ||
		      ( f.getDataType() == DataType.URI )) ) {
			throw new IllegalArgumentException("Attempting to set non-string field with a String object");
		}
		boolean noOverflow = true;
		if ( value == null ) {
			if ( !f.getNullable() ) {
				throw new IllegalStateException("Attempting to set null value in non-null field");
			}
			fieldValueMap.remove(f);
			return true;
		} else if ( value.length() > f.getMaxCharLen() ) {
			if ( f.getDataType() == DataType.LONG_STRING ) {
				throw new IllegalArgumentException("overflowing a LONG_STRING!!");
			} else if ( f.getDataType() == DataType.URI ) {
				throw new IllegalArgumentException("overflowing a URI!!");
			}
			noOverflow = false;
			value = value.substring(0, f.getMaxCharLen().intValue());
		}
		fieldValueMap.put(f, value);
		return noOverflow;
	}

	public final Long getLongField(DataField f) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		Object o = fieldValueMap.get(f);
		if ( o == null ) return null;
		return (Long) o;
	}
	
	public final void setLongField(DataField f, Long value) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		if ( f.getDataType() != DataType.INTEGER ) {
			throw new IllegalArgumentException("Attempting to set non-integer field with a Long object");
		}
		if ( value == null ) {
			if ( !f.getNullable() ) {
				throw new IllegalStateException("Attempting to set null value in non-null field");
			}
			fieldValueMap.remove(f);
			return;
		}
		fieldValueMap.put(f, value);
	}
	
	public final BigDecimal getNumericField(DataField f) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		Object o = fieldValueMap.get(f);
		if ( o == null ) return null;
		return (BigDecimal) o;
	}
	
	public final void setNumericField(DataField f, BigDecimal value) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		if ( f.getDataType() != DataType.DECIMAL ) {
			throw new IllegalArgumentException("Attempting to set non-decimal field with a BigDecimal object");
		}
		if ( value == null ) {
			if ( !f.getNullable() ) {
				throw new IllegalStateException("Attempting to set null value in non-null field");
			}
			fieldValueMap.remove(f);
			return;
		}
		fieldValueMap.put(f, value);
	}

	public final Date getDateField(DataField f) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		Object o = fieldValueMap.get(f);
		if ( o == null ) return null;
		return (Date) o;
	}
	
	public final void setDateField(DataField f, Date value) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		if ( f.getDataType() != DataType.DATETIME ) {
			throw new IllegalArgumentException("Attempting to set non-datetime field with a Date object");
		}
		if ( value == null ) {
			if ( !f.getNullable() ) {
				throw new IllegalStateException("Attempting to set null value in non-null field");
			}
			fieldValueMap.remove(f);
			return;
		}
		fieldValueMap.put(f, value);
	}

	public final Boolean getBooleanField(DataField f) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		Object o = fieldValueMap.get(f);
		if ( o == null ) return null;
		return (Boolean) o;
	}
	
	public final void setBooleanField(DataField f, Boolean value) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		if ( f.getDataType() != DataType.BOOLEAN ) {
			throw new IllegalArgumentException("Attempting to set non-boolean field with a Boolean object");
		}
		if ( value == null ) {
			if ( !f.getNullable() ) {
				throw new IllegalStateException("Attempting to set null value in non-null field");
			}
			fieldValueMap.remove(f);
			return;
		}
		fieldValueMap.put(f, value);
	}
	
	public final byte[] getBlobField(DataField f) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		Object o = fieldValueMap.get(f);
		if ( o == null ) return null;
		return (byte[]) o;
	}
	
	public final void setBlobField(DataField f, byte[] value) {
		if ( f == null ) {
			throw new IllegalArgumentException("Field value is null!");
		}
		if ( !fieldList.contains(f) ) {
			throw new IllegalArgumentException("Attempting to set a field not belonging to this object");
		}
		if ( f.getDataType() != DataType.BINARY ) {
			throw new IllegalArgumentException("Attempting to set non-blob field with byte-array object");
		}
		if ( value == null ) {
			if ( !f.getNullable() ) {
				throw new IllegalStateException("Attempting to set null value in non-null field");
			}
			fieldValueMap.remove(f);
			return;
		}
		fieldValueMap.put(f,value);
	}
	
	public final static String newUri() {
		String s = "uuid:" + UUID.randomUUID().toString().toLowerCase();
		return s;
	}

	/**********************************************************************************
	 * APIs that should only be used by the persistence layer
	 */

	public final <T extends CommonFieldsBase> T getEmptyRow(Class<T> clazz, User user) {
		if (! this.getClass().equals(clazz) ) {
			throw new IllegalArgumentException("Not requesting most derived class!");
		}
		
		try {
			Class<?> paramTypes[] = new Class<?>[1];
			paramTypes[0] = clazz;
			Constructor<T> c = clazz.getConstructor(paramTypes);
			if ( c == null ) {
				throw new IllegalStateException("Copy constructor for " + clazz.getCanonicalName() + " not defined!");
			}
			Object argList[] = new Object[1];
			argList[0] = this;
			T obj = c.newInstance(argList);
			Date now = new Date();
			obj.fieldValueMap.put(creationDate, now);
			obj.fieldValueMap.put(lastUpdateDate, now);
			obj.fieldValueMap.put(creatorUriUser,  user.getUriUser());
			obj.fieldValueMap.put(primaryKey, CommonFieldsBase.newUri());
			return obj;
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return true if the row contains data that originated from the persistent store.
	 */
	public final boolean isFromDatabase() {
		return fromDatabase;
	}

	/**
	 *	Set whether or not the row contains data that originated from the persistent store.
	 *  This should only be called from within the persistence layer implementation.  Used
	 *  to determine whether to INSERT or UPDATE a record in the persistent store.
	 *   
	 * @param fromDatabase
	 */
	public final void setFromDatabase(boolean fromDatabase) {
		this.fromDatabase = fromDatabase;
	}

	/**
	 * @return the opaque object linked to this row by the persistence layer.
	 */
	public Object getOpaquePersistenceData() {
		return opaquePersistenceData;
	}

	/**
	 * Associate an opaque object with this row.  This should only be called from within 
	 * the persistence layer implementation.  Used by some persistence layers to 
	 * associated private information to a retrieved object that will be needed if
	 * updates to the row are requested.
	 * 
	 * @param opaquePersistenceData
	 */
	public void setOpaquePersistenceData(Object opaquePersistenceData) {
		this.opaquePersistenceData = opaquePersistenceData;
	}

	public final boolean isNull(DataField f) {
		return (fieldValueMap.get(f) == null);
	}
	
	public boolean sameTable(CommonFieldsBase ref) {
		return getSchemaName().equals(ref.getSchemaName()) &&
				getTableName().equals(ref.getTableName());
	}
}
