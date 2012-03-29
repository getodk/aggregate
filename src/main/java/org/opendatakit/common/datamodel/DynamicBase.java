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
package org.opendatakit.common.datamodel;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.security.User;


/**
 * All instance data for an xform is stored in tables derived from 
 * InstanceDataBase or TopLevelInstanceDataBase tables.  The 
 * TopLevelInstanceDataBase table holds the metadata about the 
 * submission, whereas the repeat groups (the InstanceDataBase tables)
 * do not.
 * <p>
 * This common base class can be used by internal tables that should
 * be xform-like to share much of the processing of xform tables.
 * E.g., Xform tables can be viewed through the web interface.
 * <p>
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public abstract class DynamicBase extends DynamicCommonFieldsBase {

	/* dynamic */
	public static final int ADDITIONAL_COLUMN_COUNT = 3 + CommonFieldsBase.AUDIT_COLUMN_COUNT;
	
	/** key into the dynamic table that is our parent container */
	private static final DataField PARENT_AURI = new DataField("_PARENT_AURI", DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);
	/** ordinal (1st, 2nd, ... ) of this item in the form element */
	private static final DataField ORDINAL_NUMBER = new DataField("_ORDINAL_NUMBER", DataField.DataType.INTEGER, false);
	/** key into the top level dynamic table that is our ancestor */
	private static final DataField TOP_LEVEL_AURI = new DataField("_TOP_LEVEL_AURI", DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN);

	public final DataField parentAuri;
	public final DataField ordinalNumber;
	public final DataField topLevelAuri;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	protected DynamicBase(String databaseSchema, String tableName) {
		super(databaseSchema, tableName);
		fieldList.add(parentAuri=new DataField(PARENT_AURI));
		fieldList.add(ordinalNumber=new DataField(ORDINAL_NUMBER));
		fieldList.add(topLevelAuri=new DataField(TOP_LEVEL_AURI));
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	protected DynamicBase(DynamicBase ref, User user) {
		super(ref, user);
		parentAuri = ref.parentAuri;
		ordinalNumber = ref.ordinalNumber;
		topLevelAuri = ref.topLevelAuri;
	}
	
	public final String getTopLevelAuri() {
		return getStringField(topLevelAuri);
	}
	
	public final void setTopLevelAuri(String value) {
		if ( ! setStringField(topLevelAuri, value) ) {
			throw new IllegalStateException("overflow on topLevelAuri");
		}
	}
	
	public final String getParentAuri() {
		return getStringField(parentAuri);
	}
	
	public final void setParentAuri(String value) {
		if ( ! setStringField(parentAuri, value) ) {
			throw new IllegalStateException("overflow on parentAuri");
		}
	}

	public final Long getOrdinalNumber() {
		return getLongField(ordinalNumber);
	}

	public final void setOrdinalNumber(Long value) {
		setLongField(ordinalNumber, value);
	}

}
