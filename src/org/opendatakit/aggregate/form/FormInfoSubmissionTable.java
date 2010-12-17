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

import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DynamicBase;
import org.opendatakit.common.persistence.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * Holds the submission form ids associated with an XForm.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public class FormInfoSubmissionTable extends DynamicBase {
	static final String TABLE_NAME = "_form_info_submission";

	private static final DataField SUBMISSION_FORM_ID = new DataField("SUBMISSION_FORM_ID",
			DataField.DataType.STRING, true, PersistConsts.MAX_SIMPLE_STRING_LEN);

	private static final DataField SUBMISSION_MODEL_VERSION = new DataField("SUBMISSION_MODEL_VERSION",
			DataField.DataType.INTEGER, true);

	private static final DataField SUBMISSION_UI_VERSION = new DataField("SUBMISSION_UI_VERSION",
			DataField.DataType.INTEGER, true);

	public final DataField submissionFormId;
	public final DataField submissionModelVersion;
	public final DataField submissionUiVersion;

	public static final String URI_FORM_ID_VALUE_FORM_INFO_SUBMISSION = "aggregate.opendatakit.org:FormInfoSubmission";

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 */
	private FormInfoSubmissionTable(String databaseSchema) {
		super(databaseSchema, TABLE_NAME);
		fieldList.add(submissionFormId = new DataField(SUBMISSION_FORM_ID));
		fieldList.add(submissionModelVersion = new DataField(SUBMISSION_MODEL_VERSION));
		fieldList.add(submissionUiVersion = new DataField(SUBMISSION_UI_VERSION));
		
		fieldValueMap.put(primaryKey, FormInfoSubmissionTable.URI_FORM_ID_VALUE_FORM_INFO_SUBMISSION);
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	private FormInfoSubmissionTable(FormInfoSubmissionTable ref, User user) {
		super(ref, user);
		submissionFormId = ref.submissionFormId;
		submissionModelVersion = ref.submissionModelVersion;
		submissionUiVersion = ref.submissionUiVersion;
	}

	@Override
	public FormInfoSubmissionTable getEmptyRow(User user) {
		return new FormInfoSubmissionTable(this, user);
	}
	
	private static FormInfoSubmissionTable relation = null;
	
	static synchronized final FormInfoSubmissionTable createRelation(Datastore datastore, User user) throws ODKDatastoreException {
		if ( relation == null ) {
			FormInfoSubmissionTable relationPrototype;
			relationPrototype = new FormInfoSubmissionTable(datastore.getDefaultSchemaName());
		    datastore.assertRelation(relationPrototype, user); // may throw exception...
		    // at this point, the prototype has become fully populated
		    relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}
	
	static final void createFormDataModel(List<FormDataModel> model, Long ordinal, 
			TopLevelDynamicBase formInfoDefinitionRelation, 
			DynamicCommonFieldsBase formInfoTableRelation, 
			Datastore datastore, User user) throws ODKDatastoreException {
		
		FormInfoSubmissionTable submissionRelation = createRelation(datastore, user);
		
		FormDefinition.buildTableFormDataModel( model, 
				submissionRelation, 
				formInfoDefinitionRelation, // top level table
				formInfoTableRelation, // also the parent table
				ordinal,
				datastore, user );
	}

}
