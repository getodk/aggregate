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
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormInfoDescriptionTable extends DynamicBase {
	static final String TABLE_NAME = "_form_info_description";

	private static final DataField LANGUAGE_CODE = new DataField("LANGUAGE_CODE",
			DataField.DataType.STRING, true, 8L);

	private static final DataField FORM_NAME = new DataField("FORM_NAME",
			DataField.DataType.STRING, true, PersistConsts.MAX_SIMPLE_STRING_LEN);

	private static final DataField DESCRIPTION = new DataField("DESCRIPTION",
			DataField.DataType.STRING, true, 8192L);

	private static final DataField DESCRIPTION_URL = new DataField("DESCRIPTION_URL",
			DataField.DataType.STRING, true, 2048L);

	public final DataField languageCode;
	public final DataField formName;
	public final DataField description;
	public final DataField descriptionUrl;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 */
	private FormInfoDescriptionTable(String databaseSchema) {
		super(databaseSchema, TABLE_NAME);
		fieldList.add(languageCode = new DataField(LANGUAGE_CODE));
		fieldList.add(formName = new DataField(FORM_NAME));
		fieldList.add(description = new DataField(DESCRIPTION));
		fieldList.add(descriptionUrl = new DataField(DESCRIPTION_URL));
		
		fieldValueMap.put(primaryKey, FormDataModel.URI_FORM_ID_VALUE_FORM_INFO_DESCRIPTION);
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	private FormInfoDescriptionTable(FormInfoDescriptionTable ref, User user) {
		super(ref, user);
		languageCode = ref.languageCode;
		formName = ref.formName;
		description = ref.description;
		descriptionUrl = ref.descriptionUrl;
	}

	@Override
	public FormInfoDescriptionTable getEmptyRow(User user) {
		return new FormInfoDescriptionTable(this, user);
	}
	
	private static FormInfoDescriptionTable relation = null;
	
	static final FormInfoDescriptionTable createRelation(Datastore datastore, User user) throws ODKDatastoreException {
		if ( relation == null ) {
			FormInfoDescriptionTable relationPrototype;
			relationPrototype = new FormInfoDescriptionTable(datastore.getDefaultSchemaName());
		    datastore.createRelation(relationPrototype, user); // may throw exception...
		    // at this point, the prototype has become fully populated
		    relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}
	
	static final void createFormDataModel(List<FormDataModel> model, Long ordinal,
				TopLevelDynamicBase formInfoDefinitionRelation, 
				DynamicCommonFieldsBase formInfoTableRelation, 
				Datastore datastore, User user) throws ODKDatastoreException {
		
		FormInfoDescriptionTable descriptionRelation = createRelation(datastore, user);
		
		FormDefinition.buildTableFormDataModel( model, 
				descriptionRelation, 
				formInfoDefinitionRelation, // top level table
				formInfoTableRelation, // also the parent table
				ordinal,
				datastore, user );
	}
}
