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
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class PersistentResults extends TopLevelDynamicBase {
	static final String TABLE_NAME = "_persistent_result";

	private static final String FORM_ID_PERSISTENT_RESULT = "aggregate.opendatakit.org:PersistentResults";
	
	private static final String PERSISTENT_RESULT_DEFINITION_URI = "aggregate.opendatakit.org:PersistentResults-Definition";

	private static final DataField REQUESTING_USER = new DataField("REQUESTING_USER",
			DataField.DataType.STRING, true);

	private static final DataField REQUEST_DATE = new DataField("REQUEST_DATE",
			DataField.DataType.DATETIME, true);

	private static final DataField STATUS = new DataField("STATUS",
			DataField.DataType.STRING, true);

	private static final DataField COMPLETION_DATE = new DataField("COMPLETION_DATE",
			DataField.DataType.DATETIME, true);

	public final DataField requestingUser;
	public final DataField requestDate;
	public final DataField status;
	public final DataField completionDate;

	// additional virtual DataField -- binary content
	
	static final String ELEMENT_NAME_RESULT_FILE_DEFINITION = "resultFile";

	private static final String PERSISTENT_RESULT_FILE_REF_BLOB = "_persistent_result_file_blb";

	private static final String PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT_REF_BLOB = "_persistent_result_file_ref";

	private static final String PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT = "_persistent_result_file_vbn";

	private static final String PERSISTENT_RESULT_FILE_BINARY_CONTENT = "_persistent_result_file_bin";

	// additional virtual DataField -- long string text
	
	private static final String FORM_INFO_REF_TEXT = "_form_info_string_txt";

	private static final String FORM_INFO_LONG_STRING_REF_TEXT = "_form_info_string_ref";

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 */
	private PersistentResults(String databaseSchema) {
		super(databaseSchema, TABLE_NAME);
		fieldList.add(requestingUser = new DataField(REQUESTING_USER));
		fieldList.add(requestDate = new DataField(REQUEST_DATE));
		fieldList.add(status = new DataField(STATUS));
		fieldList.add(completionDate = new DataField(COMPLETION_DATE));

		fieldValueMap.put(primaryKey, CommonFieldsBase.newMD5HashUri(FORM_ID_PERSISTENT_RESULT));
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	private PersistentResults(PersistentResults ref, User user) {
		super(ref, user);
		requestingUser = ref.requestingUser;
		requestDate = ref.requestDate;
		status = ref.status;
		completionDate = ref.completionDate;
	}

	@Override
	public PersistentResults getEmptyRow(User user) {
		return new PersistentResults(this, user);
	}
	
	private static PersistentResults relation = null;
	
	static final PersistentResults createRelation(Datastore datastore, User user) throws ODKDatastoreException {
		if ( relation == null ) {
			PersistentResults relationPrototype;
			relationPrototype = new PersistentResults(datastore.getDefaultSchemaName());
		    datastore.createRelation(relationPrototype, user); // may throw exception...
		    // at this point, the prototype has become fully populated
		    relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}
	
	static final String createFormDataModel(List<FormDataModel> model, EntityKey definitionKey, Datastore datastore, User user) throws ODKDatastoreException {

		FormDataModel.createRelation(datastore, user);
		SubmissionAssociationTable.createRelation(datastore, user);
		
		PersistentResults persistentResultsRelation = createRelation(datastore, user);
		PersistentResults persistentResultsDefinition = datastore.createEntityUsingRelation(persistentResultsRelation, null, user);
		persistentResultsDefinition.setStringField(persistentResultsRelation.primaryKey, PERSISTENT_RESULT_DEFINITION_URI);
		
		Long lastOrdinal = 0L;
		
		lastOrdinal = FormDefinition.buildTableFormDataModel( model, 
				definitionKey,
				persistentResultsRelation, 
				persistentResultsDefinition, // top level table
				persistentResultsDefinition, // parent table...
				1L,
				datastore, user );

		FormDefinition.buildBinaryContentFormDataModel(model, definitionKey, 
				ELEMENT_NAME_RESULT_FILE_DEFINITION, 
				PERSISTENT_RESULT_FILE_BINARY_CONTENT, 
				PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT, 
				PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT_REF_BLOB, 
				PERSISTENT_RESULT_FILE_REF_BLOB, 
				persistentResultsDefinition, // top level table
				persistentResultsRelation, // parent table
				++lastOrdinal, 
				datastore, user);
		
		FormDefinition.buildLongStringFormDataModel(model, 
				definitionKey, 
				FORM_INFO_LONG_STRING_REF_TEXT, 
				FORM_INFO_REF_TEXT, 
				persistentResultsDefinition, // top level and parent table
				2L, 
				datastore, 
				user);
		
		return persistentResultsRelation.getUri();
	}
}
