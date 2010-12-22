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
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormInfoTable extends TopLevelDynamicBase {
	static final String TABLE_NAME = "_form_info";
	
	private static final String FORM_INFO_DEFINITION_URI = "aggregate.opendatakit.org:FormInfo-Definition";
	private static final String FORM_INFO_LONG_STRING_REF_TEXT_URI = "aggregate.opendatakit.org:FormInfoLongStringRefText";
	private static final String FORM_INFO_REF_TEXT_URI = "aggregate.opendatakit.org:FormInfoRefText";

	private static final DataField FORM_ID = new DataField("FORM_ID",
			DataField.DataType.STRING, false, PersistConsts.MAX_SIMPLE_STRING_LEN);

	public final DataField formId;

	// additional virtual DataField -- long string text
	
	private static final String FORM_INFO_REF_TEXT = "_form_info_string_txt";

	private static final String FORM_INFO_LONG_STRING_REF_TEXT = "_form_info_string_ref";

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 */
	private FormInfoTable(String databaseSchema) {
		super(databaseSchema, TABLE_NAME);
		fieldList.add(formId = new DataField(FORM_ID));

		fieldValueMap.put(primaryKey, CommonFieldsBase.newMD5HashUri(FormInfo.formInfoXFormParameters.formId));
		fieldValueMap.put(formId, FormInfo.formInfoXFormParameters.formId);
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	private FormInfoTable(FormInfoTable ref, User user) {
		super(ref, user);
		formId = ref.formId;
	}

	@Override
	public FormInfoTable getEmptyRow(User user) {
		return new FormInfoTable(this, user);
	}
	
	private static FormInfoTable relation = null;
	
	static synchronized final FormInfoTable createRelation(CallingContext cc) throws ODKDatastoreException {
		if ( relation == null ) {
			FormInfoTable relationPrototype;
			Datastore ds = cc.getDatastore();
			User user = cc.getUserService().getDaemonAccountUser();
			relationPrototype = new FormInfoTable(ds.getDefaultSchemaName());
		    ds.assertRelation(relationPrototype, user); // may throw exception...
		    // at this point, the prototype has become fully populated
		    relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}
	
	static synchronized final String createFormDataModel(List<FormDataModel> model, CallingContext cc) throws ODKDatastoreException {

		FormDataModel.createRelation(cc);
		SubmissionAssociationTable saRelation = SubmissionAssociationTable.createRelation(cc);
		FormInfoTable formInfoTableRelation = createRelation(cc);
		
		boolean asDaemon = cc.getAsDeamon();
		try {
			cc.setAsDaemon(true);
			Datastore ds = cc.getDatastore();
			User user = cc.getUserService().getDaemonAccountUser();
			FormInfoTable formInfoDefinition = ds.createEntityUsingRelation(formInfoTableRelation, user);
			formInfoDefinition.setStringField(formInfoTableRelation.primaryKey, FORM_INFO_DEFINITION_URI);
			
			Long lastOrdinal = 0L;
			
			lastOrdinal = FormDefinition.buildTableFormDataModel( model, 
					formInfoTableRelation, 
					formInfoDefinition, // top level table
					formInfoDefinition, // parent table...
					1L,
					cc );
			
			FormInfoDescriptionTable.createFormDataModel(model, 
					++lastOrdinal, 
					formInfoDefinition, // top level table
					formInfoTableRelation, 
					cc );
			
			FormInfoFilesetTable.createFormDataModel(model, 
					++lastOrdinal, 
					formInfoDefinition, // top level table
					formInfoTableRelation, 
					cc );
			
			FormInfoSubmissionTable.createFormDataModel(model, 
					++lastOrdinal, 
					formInfoDefinition, // top level table
					formInfoTableRelation, 
					cc );
			
			FormDefinition.buildLongStringFormDataModel(model, 
					FORM_INFO_LONG_STRING_REF_TEXT_URI,
					FORM_INFO_LONG_STRING_REF_TEXT, 
					FORM_INFO_REF_TEXT_URI,
					FORM_INFO_REF_TEXT, 
					formInfoDefinition, // top level and parent table
					2L, 
					cc );
	
			return formInfoTableRelation.getUri();
		} finally {
			cc.setAsDaemon(asDaemon);
		}
	}
}
