/*
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.form;

import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.datamodel.DynamicBase;
import org.opendatakit.aggregate.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormInfoFilesetTable extends DynamicBase {
	static final String TABLE_NAME = "_form_info_fileset";

	private static final DataField ROOT_ELEMENT_MODEL_VERSION = new DataField("ROOT_ELEMENT_MODEL_VERSION",
			DataField.DataType.INTEGER, true);

	private static final DataField ROOT_ELEMENT_UI_VERSION = new DataField("ROOT_ELEMENT_UI_VERSION",
			DataField.DataType.INTEGER, true);

	private static final DataField IS_FILESET_COMPLETE = new DataField("IS_FILESET_COMPLETE",
			DataField.DataType.BOOLEAN, true);

	private static final DataField IS_DOWNLOAD_ALLOWED = new DataField("IS_DOWNLOAD_ALLOWED",
			DataField.DataType.BOOLEAN, true);

	// Additional DataField -- the xformDefinition (binary content)
	
	static final String ELEMENT_NAME_XFORM_DEFINITION = "xformDefinition";

	private static final String FORM_INFO_XFORM_REF_BLOB = "_form_info_xform_blb";

	private static final String FORM_INFO_XFORM_VERSIONED_BINARY_CONTENT_REF_BLOB = "_form_info_xform_ref";

	private static final String FORM_INFO_XFORM_VERSIONED_BINARY_CONTENT = "_form_info_xform_vbn";

	private static final String FORM_INFO_XFORM_BINARY_CONTENT = "_form_info_xform_bin";

	// Additional DataField -- the manifest fileset (multivalued binary content)
	
	static final String ELEMENT_NAME_MANIFEST_FILESET = "manifestFileset";

	private static final String FORM_INFO_MANIFEST_REF_BLOB = "_form_info_manifest_blb";

	private static final String FORM_INFO_MANIFEST_VERSIONED_BINARY_CONTENT_REF_BLOB = "_form_info_manifest_ref";

	private static final String FORM_INFO_MANIFEST_VERSIONED_BINARY_CONTENT = "_form_info_manifest_vbn";

	private static final String FORM_INFO_MANIFEST_BINARY_CONTENT = "_form_info_manifest_bin";
	
	// the relation fields...
	public final DataField rootElementModelVersion;
	public final DataField rootElementUiVersion;
	public final DataField isFilesetComplete;
	public final DataField isDownloadAllowed;

	public static final String URI_FORM_ID_VALUE_FORM_INFO_FILESET = "aggregate.opendatakit.org:FormInfoFileset";

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 */
	private FormInfoFilesetTable(String databaseSchema) {
		super(databaseSchema, TABLE_NAME);
		fieldList.add(rootElementModelVersion = new DataField(ROOT_ELEMENT_MODEL_VERSION));
		fieldList.add(rootElementUiVersion = new DataField(ROOT_ELEMENT_UI_VERSION));
		fieldList.add(isFilesetComplete = new DataField(IS_FILESET_COMPLETE));
		fieldList.add(isDownloadAllowed = new DataField(IS_DOWNLOAD_ALLOWED));
		
		fieldValueMap.put(primaryKey, FormInfoFilesetTable.URI_FORM_ID_VALUE_FORM_INFO_FILESET);
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	private FormInfoFilesetTable(FormInfoFilesetTable ref, User user) {
		super(ref, user);
		rootElementModelVersion = ref.rootElementModelVersion;
		rootElementUiVersion = ref.rootElementUiVersion;
		isFilesetComplete = ref.isFilesetComplete;
		isDownloadAllowed = ref.isDownloadAllowed;
	}

	@Override
	public FormInfoFilesetTable getEmptyRow(User user) {
		return new FormInfoFilesetTable(this, user);
	}
	
	private static FormInfoFilesetTable relation = null;
	
	static synchronized final FormInfoFilesetTable createRelation(CallingContext cc) throws ODKDatastoreException {
		if ( relation == null ) {
			FormInfoFilesetTable relationPrototype;
			Datastore ds = cc.getDatastore();
			User user = cc.getUserService().getDaemonAccountUser();
			relationPrototype = new FormInfoFilesetTable(ds.getDefaultSchemaName());
		    ds.assertRelation(relationPrototype, user); // may throw exception...
		    // at this point, the prototype has become fully populated
		    relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}
	
	static final void createFormDataModel(List<FormDataModel> model, Long ordinal, 
			TopLevelDynamicBase formInfoDefinitionRelation, 
			DynamicCommonFieldsBase formInfoTableRelation, 
			CallingContext cc) throws ODKDatastoreException {
		
		FormInfoFilesetTable filesetRelation = createRelation(cc);
		
		boolean asDaemon = cc.getAsDeamon();
		try {
			cc.setAsDaemon(true);
			Long lastOrdinal = 0L;
			
			lastOrdinal = FormDefinition.buildTableFormDataModel( model, 
				filesetRelation, 
				formInfoDefinitionRelation, // top level table
				formInfoTableRelation, // also the parent table
				ordinal,
				cc );

			String uriPrefix = Form.URI_FORM_ID_VALUE_FORM_INFO;
			FormDefinition.buildBinaryContentFormDataModel(model, 
				ELEMENT_NAME_XFORM_DEFINITION, 
				uriPrefix + FORM_INFO_XFORM_BINARY_CONTENT,
				FORM_INFO_XFORM_BINARY_CONTENT, 
				uriPrefix + FORM_INFO_XFORM_VERSIONED_BINARY_CONTENT, 
				FORM_INFO_XFORM_VERSIONED_BINARY_CONTENT, 
				uriPrefix + FORM_INFO_XFORM_VERSIONED_BINARY_CONTENT_REF_BLOB, 
				FORM_INFO_XFORM_VERSIONED_BINARY_CONTENT_REF_BLOB, 
				uriPrefix + FORM_INFO_XFORM_REF_BLOB, 
				FORM_INFO_XFORM_REF_BLOB, 
				formInfoDefinitionRelation, // top level table
				filesetRelation, // parent table
				++lastOrdinal, 
				cc );
		
			FormDefinition.buildBinaryContentFormDataModel(model, 
				ELEMENT_NAME_MANIFEST_FILESET, 
				uriPrefix + FORM_INFO_MANIFEST_BINARY_CONTENT, 
				FORM_INFO_MANIFEST_BINARY_CONTENT, 
				uriPrefix + FORM_INFO_MANIFEST_VERSIONED_BINARY_CONTENT, 
				FORM_INFO_MANIFEST_VERSIONED_BINARY_CONTENT, 
				uriPrefix + FORM_INFO_MANIFEST_VERSIONED_BINARY_CONTENT_REF_BLOB, 
				FORM_INFO_MANIFEST_VERSIONED_BINARY_CONTENT_REF_BLOB, 
				uriPrefix + FORM_INFO_MANIFEST_REF_BLOB, 
				FORM_INFO_MANIFEST_REF_BLOB, 
				formInfoDefinitionRelation, // top level table
				filesetRelation, // parent table
				++lastOrdinal, 
				cc );
		} finally {
			cc.setAsDaemon(asDaemon);
		}
		
	}
}
