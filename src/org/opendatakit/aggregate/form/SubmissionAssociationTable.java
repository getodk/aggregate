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

import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.StaticAssociationBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionAssociationTable extends StaticAssociationBase {
	private static final String TABLE_NAME = "_form_info_submission_association";

	private static final DataField SUBMISSION_FORM_ID = new DataField("SUBMISSION_FORM_ID",
			DataField.DataType.STRING, true, PersistConsts.MAX_SIMPLE_STRING_LEN);

	private static final DataField SUBMISSION_MODEL_VERSION = new DataField("SUBMISSION_MODEL_VERSION",
			DataField.DataType.INTEGER, true);

	private static final DataField SUBMISSION_UI_VERSION = new DataField("SUBMISSION_UI_VERSION",
			DataField.DataType.INTEGER, true);

	private static final DataField IS_PERSISTENCE_MODEL_COMPLETE = new DataField("IS_PERSISTENCE_MODEL_COMPLETE",
			DataField.DataType.BOOLEAN, true);

	private static final DataField IS_SUBMISSION_ALLOWED = new DataField("IS_SUBMISSION_ALLOWED",
			DataField.DataType.BOOLEAN, true);

	private static final DataField URI_SUBMISSION_DATA_MODEL = new DataField("URI_SUBMISSION_DATA_MODEL",
			DataField.DataType.URI, true);

	public final DataField submissionFormId;
	public final DataField submissionModelVersion;
	public final DataField submissionUiVersion;
	public final DataField isPersistenceModelComplete;
	public final DataField isSubmissionAllowed;
	public final DataField uriSubmissionDataModel;

	public static final String URI_FORM_ID_VALUE_FORM_INFO_SUBMISSION_ASSOCIATION = "aggregate.opendatakit.org:FormInfoSubmissionAssociation";

	/**
	 * DOM_AURI (md5 of submission form_id)
	 * SUB_AURI (URI_FORM_INFO)
	 */
	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 */
	private SubmissionAssociationTable(String databaseSchema) {
		super(databaseSchema, TABLE_NAME);
		fieldList.add(submissionFormId = new DataField(SUBMISSION_FORM_ID));
		fieldList.add(submissionModelVersion = new DataField(SUBMISSION_MODEL_VERSION));
		fieldList.add(submissionUiVersion = new DataField(SUBMISSION_UI_VERSION));
		fieldList.add(isPersistenceModelComplete = new DataField(IS_PERSISTENCE_MODEL_COMPLETE));
		fieldList.add(isSubmissionAllowed = new DataField(IS_SUBMISSION_ALLOWED));
		fieldList.add(uriSubmissionDataModel = new DataField(URI_SUBMISSION_DATA_MODEL));
		
		fieldValueMap.put(primaryKey, SubmissionAssociationTable.URI_FORM_ID_VALUE_FORM_INFO_SUBMISSION_ASSOCIATION);
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	private SubmissionAssociationTable(SubmissionAssociationTable ref, User user) {
		super(ref, user);
		submissionFormId = ref.submissionFormId;
		submissionModelVersion = ref.submissionModelVersion;
		submissionUiVersion = ref.submissionUiVersion;
		isPersistenceModelComplete = ref.isPersistenceModelComplete;
		isSubmissionAllowed = ref.isSubmissionAllowed;
		uriSubmissionDataModel = ref.uriSubmissionDataModel;
	}

	@Override
	public SubmissionAssociationTable getEmptyRow(User user) {
		return new SubmissionAssociationTable(this, user);
	}
	
	public XFormParameters getXFormParameters() {
		return new XFormParameters( getSubmissionFormId(), 
				getSubmissionModelVersion(), getSubmissionUiVersion());
	}
	
	public String getSubmissionFormId() {
		return getStringField(submissionFormId);
	}
	
	public void setSubmissionFormId(String value) {
		setStringField(submissionFormId, value);
	}

	public Long getSubmissionModelVersion() {
		return getLongField(submissionModelVersion);
	}

	public void setSubmissionModelVersion(Long value) {
		setLongField(submissionModelVersion, value);
	}

	public Long getSubmissionUiVersion() {
		return getLongField(submissionUiVersion);
	}

	public void setSubmissionUiVersion(Long value) {
		setLongField(submissionUiVersion, value);
	}

	public Boolean getIsPersistenceModelComplete() {
		return getBooleanField(isPersistenceModelComplete);
	}

	public void setIsPersistenceModelComplete(Boolean value) {
		setBooleanField(isPersistenceModelComplete, value);
	}

	public Boolean getIsSubmissionAllowed() {
		return getBooleanField(isSubmissionAllowed);
	}

	public void setIsSubmissionAllowed(Boolean value) {
		setBooleanField(isSubmissionAllowed, value);
	}

	public String getUriSubmissionDataModel() {
		return getStringField(uriSubmissionDataModel);
	}

	public void setUriSubmissionDataModel(String value) {
		setStringField(uriSubmissionDataModel, value);
	}

	private static SubmissionAssociationTable relation = null;
	
	public static synchronized final SubmissionAssociationTable createRelation(Datastore datastore, User user) throws ODKDatastoreException {
		if ( relation == null ) {
			SubmissionAssociationTable relationPrototype;
			relationPrototype = new SubmissionAssociationTable(datastore.getDefaultSchemaName());
		    datastore.assertRelation(relationPrototype, user); // may throw exception...
		    // at this point, the prototype has become fully populated
		    relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}
}
