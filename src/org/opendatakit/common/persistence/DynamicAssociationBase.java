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
public abstract class DynamicAssociationBase extends DynamicCommonFieldsBase {
	
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

	/* dynamic and dynamic association tables */
	
	/** key into the top level dynamic table that is our ancestor */
	private static final DataField TOP_LEVEL_AURI = new DataField("_TOP_LEVEL_AURI", DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN);

	public final DataField domAuri;
	public final DataField subAuri;
	public final DataField topLevelAuri;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	protected DynamicAssociationBase(String databaseSchema, String tableName) {
		super(databaseSchema, tableName, BaseType.DYNAMIC_ASSOCIATION);
		fieldList.add(domAuri=new DataField(DOM_AURI));
		fieldList.add(subAuri=new DataField(SUB_AURI));
		fieldList.add(topLevelAuri=new DataField(TOP_LEVEL_AURI));
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	protected DynamicAssociationBase(DynamicAssociationBase ref, User user) {
		super(ref, user);
		domAuri = ref.domAuri;
		subAuri = ref.subAuri;
		topLevelAuri = ref.topLevelAuri;
	}

	
	public final String getTopLevelAuri() {
		if (( tableType == BaseType.STATIC) ||
			(tableType == BaseType.STATIC_ASSOCIATION) ||
			(tableType == BaseType.TOP_LEVEL_DYNAMIC)) {
			throw new IllegalStateException("Attempting to get topLevelAuri of non-DYNAMIC table");
		}
		return getStringField(topLevelAuri);
	}
	
	public final void setTopLevelAuri(String value) {
		if (( tableType == BaseType.STATIC) ||
			(tableType == BaseType.STATIC_ASSOCIATION) ||
			(tableType == BaseType.TOP_LEVEL_DYNAMIC)) {
			throw new IllegalStateException("Attempting to set topLevelAuri of non-DYNAMIC table");
		}
		if ( ! setStringField(topLevelAuri, value) ) {
			throw new IllegalStateException("overflow on topLevelAuri");
		}
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
}
