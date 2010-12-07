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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.DateSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class PersistentResults {
	
	public static final String FORM_ID_PERSISTENT_RESULT = "aggregate.opendatakit.org:PersistentResults";

	public static final XFormParameters xformPersistentResultsParameters = 
		new XFormParameters( FORM_ID_PERSISTENT_RESULT, 1L, 0L);

	private static FormElementModel requestingUser;
	private static FormElementModel requestDate;
	private static FormElementModel status;
	private static FormElementModel completionDate;
	private static FormElementModel resultFile;

	public static FormElementModel getRequestingUserKey() {
		return requestingUser;
	}

	public static FormElementModel getRequestDateKey() {
		return requestDate;
	}

	public static FormElementModel getStatusKey() {
		return status;
	}

	public static FormElementModel getCompletionDateKey() {
		return completionDate;
	}

	public static FormElementModel getResultFileKey() {
		return resultFile;
	}

	public Submission objectEntity;
	
	/**
	 * After you have a submission (e.g., from a query), create a 
	 * PersistentResults object to wrap it 
	 * and make access to values easier.
	 * 
	 * @param s
	 */
	public PersistentResults(Submission s) {
		objectEntity = s;
	}

	/**
	 * Constructor helper for the common case.  Note that the form (objectEntity) 
	 * is not yet persisted.  To persist it, you must call objectEntity.persist(datastore, user)
	 * @param requestingUser
	 * @param requestDate
	 * @param status
	 * @param datastore
	 * @param user
	 * @throws ODKDatastoreException
	 */
	public PersistentResults(String requestingUser, Date requestDate, String status,
							 Datastore datastore, User user) throws ODKDatastoreException {
		Form form;
		try {
			form = Form.retrieveForm(FORM_ID_PERSISTENT_RESULT, datastore, user);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		objectEntity = new Submission(xformPersistentResultsParameters.modelVersion,
								xformPersistentResultsParameters.uiVersion,
								form.getFormDefinition(), datastore, user);
		setRequestingUser(requestingUser);
		setRequestDate(requestDate);
		setStatus(status);
		
		objectEntity.setIsComplete(true); // indicate that the data should be visible on queries
		// NOTE: the entity is not yet persisted! 
	}
	
	public String getRequestingUser() {
		return ((StringSubmissionType) objectEntity.getElementValue(requestingUser)).getValue();
	}
	
	public void setRequestingUser(String value) throws ODKEntityPersistException {
		((StringSubmissionType) objectEntity.getElementValue(requestingUser)).setValueFromString(value);
	}

	public Date getRequestDate() {
		return ((DateSubmissionType) objectEntity.getElementValue(requestDate)).getValue();
	}

	public void setRequestDate(Date value) {
		((DateSubmissionType) objectEntity.getElementValue(requestDate)).setValueFromDate(value);
	}
	
	public String getStatus() {
		return ((StringSubmissionType) objectEntity.getElementValue(status)).getValue();
	}
	
	public void setStatus(String value) throws ODKEntityPersistException {
		((StringSubmissionType) objectEntity.getElementValue(status)).setValueFromString(value);
	}
	
	public Date getCompletionDate() {
		return ((DateSubmissionType) objectEntity.getElementValue(completionDate)).getValue();
	}
	
	public void setCompletionDate(Date value) {
		((DateSubmissionType) objectEntity.getElementValue(completionDate)).setValueFromDate(value);
	}

	public byte[] getResultFile() throws ODKDatastoreException {
		BlobSubmissionType bt = ((BlobSubmissionType) objectEntity.getElementValue(resultFile));
		if ( bt.getAttachmentCount() == 0 ) return null;
		if ( bt.getAttachmentCount() > 1 ) {
			throw new IllegalStateException("Too many results attached!");
		}
		String version = bt.getCurrentVersion(1);
		return bt.getBlob(1, version);
	}

	public void setResultFile(byte[] byteArray, String contentType, Long contentLength,
								String unrootedFilePath, Datastore datastore, User user) throws ODKConversionException, ODKDatastoreException {
		BlobSubmissionType bt = ((BlobSubmissionType) objectEntity.getElementValue(resultFile));
		if ( bt.getAttachmentCount() > 0 ) {
			throw new IllegalStateException("Results are already attached!");
		}
		bt.setValueFromByteArray(byteArray, contentType, contentLength, unrootedFilePath, datastore, user);
	}

	/**
	 * Underlying top-level persistent object for the PerisistentResults form.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	private static final class PersistentResultsTable extends TopLevelDynamicBase {

		static final String TABLE_NAME = "_persistent_results";
	
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
		private PersistentResultsTable(String databaseSchema) {
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
		private PersistentResultsTable(PersistentResultsTable ref, User user) {
			super(ref, user);
			requestingUser = ref.requestingUser;
			requestDate = ref.requestDate;
			status = ref.status;
			completionDate = ref.completionDate;
		}
	
		@Override
		public PersistentResultsTable getEmptyRow(User user) {
			return new PersistentResultsTable(this, user);
		}
		
		private static PersistentResultsTable relation = null;
		
		static final PersistentResultsTable createRelation(Datastore datastore, User user) throws ODKDatastoreException {
			if ( relation == null ) {
				PersistentResultsTable relationPrototype;
				relationPrototype = new PersistentResultsTable(datastore.getDefaultSchemaName());
			    datastore.createRelation(relationPrototype, user); // may throw exception...
			    // at this point, the prototype has become fully populated
			    relation = relationPrototype; // set static variable only upon success...
			}
			return relation;
		}
	}
	
	/**
	 * Called by the form definition framework to define the Persistent Results form.
	 * 
	 * @param datastore
	 * @param user
	 * @throws ODKDatastoreException
	 */
	static final void createForm(Datastore datastore, User user) throws ODKDatastoreException {
		List<FormDataModel> model = new ArrayList<FormDataModel>();

		FormDataModel.createRelation(datastore, user);
		SubmissionAssociationTable.createRelation(datastore, user);
		
		PersistentResultsTable persistentResultsRelation = PersistentResultsTable.createRelation(datastore, user);
		PersistentResultsTable persistentResultsDefinition = datastore.createEntityUsingRelation(persistentResultsRelation, null, user);
		persistentResultsDefinition.setStringField(persistentResultsRelation.primaryKey, PersistentResultsTable.PERSISTENT_RESULT_DEFINITION_URI);
		
		Long lastOrdinal = 0L;
		
		lastOrdinal = FormDefinition.buildTableFormDataModel( model, 
				persistentResultsRelation, 
				persistentResultsDefinition, // top level table
				persistentResultsDefinition, // parent table...
				1L,
				datastore, user );

		String uriPrefix = FORM_ID_PERSISTENT_RESULT;
		FormDefinition.buildBinaryContentFormDataModel(model, 
				PersistentResultsTable.ELEMENT_NAME_RESULT_FILE_DEFINITION, 
				uriPrefix + PersistentResultsTable.PERSISTENT_RESULT_FILE_BINARY_CONTENT, 
				PersistentResultsTable.PERSISTENT_RESULT_FILE_BINARY_CONTENT, 
				uriPrefix + PersistentResultsTable.PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT, 
				PersistentResultsTable.PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT, 
				uriPrefix + PersistentResultsTable.PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT_REF_BLOB, 
				PersistentResultsTable.PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT_REF_BLOB, 
				uriPrefix + PersistentResultsTable.PERSISTENT_RESULT_FILE_REF_BLOB, 
				PersistentResultsTable.PERSISTENT_RESULT_FILE_REF_BLOB, 
				persistentResultsDefinition, // top level table
				persistentResultsRelation, // parent table
				++lastOrdinal, 
				datastore, user);
		
		FormDefinition.buildLongStringFormDataModel(model, 
				uriPrefix + PersistentResultsTable.FORM_INFO_LONG_STRING_REF_TEXT, 
				PersistentResultsTable.FORM_INFO_LONG_STRING_REF_TEXT, 
				uriPrefix + PersistentResultsTable.FORM_INFO_REF_TEXT, 
				PersistentResultsTable.FORM_INFO_REF_TEXT, 
				persistentResultsDefinition, // top level and parent table
				2L, 
				datastore, 
				user);
		
		FormDefinition.assertModel(xformPersistentResultsParameters, model, datastore, user);

		FormDefinition formDefinition = FormDefinition.getFormDefinition(xformPersistentResultsParameters, datastore, user);
		
		if ( PersistentResultsTable.relation != (PersistentResultsTable) formDefinition.getTopLevelGroup().getBackingObjectPrototype() ) {
			throw new IllegalStateException("PersistentResults form is not the canonical relation");
		}

		// and discover the form element model values for submissions of this type.
		requestingUser = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.requestingUser);
		requestDate = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.requestDate);
		status = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.status);
		completionDate = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.completionDate);
		resultFile = formDefinition.getTopLevelGroupElement().findElementByName(PersistentResultsTable.ELEMENT_NAME_RESULT_FILE_DEFINITION);

		String persistentResultsUri = persistentResultsRelation.getUri();
		// Now create a record in the FormInfo table for the PersistentResults table itself...
		FormInfoTable fiRelation = Form.getFormInfoRelation(datastore);
		FormDefinition formInfoDefinition = Form.getFormInfoDefinition(datastore);
		
		TopLevelDynamicBase fi = null;
		try {
			fi = datastore.getEntity(fiRelation, persistentResultsUri, user);
		} catch ( ODKEntityNotFoundException e ) {
			// we must have failed before persisting a FormInfo record
			// or this must be our first time through...
			Submission formInfo = new Submission(xformPersistentResultsParameters.modelVersion, xformPersistentResultsParameters.uiVersion,
									persistentResultsUri, formInfoDefinition, datastore, user);
			((StringSubmissionType) formInfo.getElementValue(FormInfo.formId)).setValueFromString(xformPersistentResultsParameters.formId);
			// default description...
			{
				RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(FormInfo.fiDescriptionTable);
				SubmissionSet sDescription = new SubmissionSet(formInfo, 1L, FormInfo.fiDescriptionTable, formInfoDefinition, formInfo.getKey(), datastore, user);
				((StringSubmissionType) sDescription.getElementValue(FormInfo.formName)).setValueFromString("Result Files");
				((StringSubmissionType) sDescription.getElementValue(FormInfo.description)).setValueFromString("Result files generated by Aggregate.");
				r.addSubmissionSet(sDescription);
			}
			// fileset...
			{
				RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(FormInfo.fiFilesetTable);
				SubmissionSet sFileset = new SubmissionSet(formInfo, 1L, FormInfo.fiFilesetTable, formInfoDefinition, formInfo.getKey(), datastore, user);
				((LongSubmissionType) sFileset.getElementValue(FormInfo.rootElementModelVersion)).setValueFromString(xformPersistentResultsParameters.modelVersion.toString());
				((LongSubmissionType) sFileset.getElementValue(FormInfo.rootElementUiVersion)).setValueFromString(xformPersistentResultsParameters.uiVersion.toString());
				((BooleanSubmissionType) sFileset.getElementValue(FormInfo.isFilesetComplete)).setValueFromString("yes");
				((BooleanSubmissionType) sFileset.getElementValue(FormInfo.isDownloadAllowed)).setValueFromString("yes");
				r.addSubmissionSet(sFileset);
			}
			// submission...
			{
				RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(FormInfo.fiSubmissionTable);
				SubmissionSet sSubmission = new SubmissionSet(formInfo, 1L, FormInfo.fiSubmissionTable, formInfoDefinition, formInfo.getKey(), datastore, user);
				((StringSubmissionType) sSubmission.getElementValue(FormInfo.submissionFormId)).setValueFromString(xformPersistentResultsParameters.formId);
				((LongSubmissionType) sSubmission.getElementValue(FormInfo.submissionModelVersion)).setValueFromString(xformPersistentResultsParameters.modelVersion.toString());
				((LongSubmissionType) sSubmission.getElementValue(FormInfo.submissionUiVersion)).setValueFromString(xformPersistentResultsParameters.uiVersion.toString());
				r.addSubmissionSet(sSubmission);
			}
			formInfo.persist(datastore, user);
			
			fi = datastore.getEntity(fiRelation, persistentResultsUri, user);
		}
		
		// and retrieve cleanly... 
	    Submission formInfo = new Submission(fi, formInfoDefinition, datastore, user);
	}

	/**
	 * Called by the form definition framework to link the form elements back to the
	 * underlying PersistentResultsTable.
	 * 
	 * @param backingTableMap
	 */
	static void populateBackingTableMap(
			Map<String, DynamicCommonFieldsBase> backingTableMap) {
		try {
		    UserService userService = (UserService) ContextFactory.get().getBean(
		    		BeanDefs.USER_BEAN);
		    User user = userService.getDaemonAccountUser();
		    Datastore datastore = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);

		    DynamicCommonFieldsBase b;
			b = PersistentResultsTable.createRelation(datastore, user);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
		} catch (ODKDatastoreException e) {
			throw new IllegalStateException("the relations should already have been created");
		}
	}
}
