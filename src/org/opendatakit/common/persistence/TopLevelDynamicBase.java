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
public abstract class TopLevelDynamicBase extends DynamicCommonFieldsBase {

	/* top level dynamic */

	/** (data model) version from submission */
	private static final DataField MODEL_VERSION = new DataField("_MODEL_VERSION", DataField.DataType.INTEGER, true);
	/** uiVersion from submission */
	private static final DataField UI_VERSION = new DataField("_UI_VERSION", DataField.DataType.INTEGER, true);
	/** whether or not the submission is complete */
	private static final DataField IS_COMPLETE = new DataField("_IS_COMPLETE", DataField.DataType.BOOLEAN, true);

	public final DataField modelVersion;
	public final DataField uiVersion;
	public final DataField isComplete;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	protected TopLevelDynamicBase(String databaseSchema, String tableName) {
		super(databaseSchema, tableName, BaseType.TOP_LEVEL_DYNAMIC);
		fieldList.add(modelVersion=new DataField(MODEL_VERSION));
		fieldList.add(uiVersion=new DataField(UI_VERSION));
		fieldList.add(isComplete=new DataField(IS_COMPLETE));
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	protected TopLevelDynamicBase(TopLevelDynamicBase ref, User user) {
		super(ref, user);
		modelVersion = ref.modelVersion;
		uiVersion = ref.uiVersion;
		isComplete = ref.isComplete;
	}

	public final Long getModelVersion() {
		if ( tableType != BaseType.TOP_LEVEL_DYNAMIC ) {
			throw new IllegalStateException("Attempting to get modelVersion of non-TOP-LEVEL-DYNAMIC table");
		}
		return getLongField(modelVersion);
	}

	public final void setModelVersion(Long value) {
		if ( tableType != BaseType.TOP_LEVEL_DYNAMIC ) {
			throw new IllegalStateException("Attempting to set modelVersion of non-TOP-LEVEL-DYNAMIC table");
		}
		setLongField(modelVersion, value);
	}

	public final Long getUiVersion() {
		if ( tableType != BaseType.TOP_LEVEL_DYNAMIC ) {
			throw new IllegalStateException("Attempting to get uiVersion of non-TOP-LEVEL-DYNAMIC table");
		}
		return getLongField(uiVersion);
	}

	public final void setUiVersion(Long value) {
		if ( tableType != BaseType.TOP_LEVEL_DYNAMIC ) {
			throw new IllegalStateException("Attempting to set uiVersion of non-TOP-LEVEL-DYNAMIC table");
		}
		setLongField(uiVersion, value);
	}

	public final Boolean getIsComplete() {
		if ( tableType != BaseType.TOP_LEVEL_DYNAMIC ) {
			throw new IllegalStateException("Attempting to get isComplete of non-TOP-LEVEL-DYNAMIC table");
		}
		return getBooleanField(isComplete);
	}

	public final void setIsComplete(Boolean value) {
		if ( tableType != BaseType.TOP_LEVEL_DYNAMIC ) {
			throw new IllegalStateException("Attempting to set isComplete of non-TOP-LEVEL-DYNAMIC table");
		}
		setBooleanField(isComplete, value);
	}
}
