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
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.DateSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
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
//	public static final long RETRY_INTERVAL_MILLISECONDS = (11 * 60) * 1000;
	public static final long RETRY_INTERVAL_MILLISECONDS = 10000;
	public static final long MAX_RETRY_ATTEMPTS = 3;
	
	public enum Status {
		GENERATION_IN_PROGRESS, // created or task is running
		RETRY_IN_PROGRESS, // task is running
		FAILED,    // task completed with failure; retry again later.
		ABANDONED, // task completed with failure; no more retries should occur.
		AVAILABLE; // task completed; results are available.
		
		public String toString() {
			switch ( this ) {
			case GENERATION_IN_PROGRESS:
				return "Generation in progress";
			case RETRY_IN_PROGRESS:
				return "Retry in progress";
			case FAILED:
				return "Failure - will retry later";
			case ABANDONED:
				return "Failure - abandoned all retry attempts";
			case AVAILABLE:
				return "Dataset Available";
			default:
				throw new IllegalStateException("missing enum case");
			}
		}
	};
	
	public enum ResultType {
		CSV,
		KML;
		
		public String toString() {
			if ( this == CSV ) {
				return "Csv file";
			} else {
				return "Kml file";
			}
		}
	};
	
	public static final String FORM_ID_PERSISTENT_RESULT = "aggregate.opendatakit.org:PersistentResults";

	public static final XFormParameters xformPersistentResultsParameters = 
		new XFormParameters( FORM_ID_PERSISTENT_RESULT, 1L, 0L);

	private static FormElementModel requestingUser;
	private static FormElementModel requestDate;
	private static FormElementModel requestParameters;
	private static FormElementModel lastRetryDate;
	private static FormElementModel attemptCount;
	private static FormElementModel status;
	private static FormElementModel resultType;
	private static FormElementModel completionDate;
	private static FormElementModel resultFile;
	private static FormElementModel formId;

	public static FormElementModel getRequestingUserKey() {
		return requestingUser;
	}

	public static FormElementModel getRequestDateKey() {
		return requestDate;
	}

	public static FormElementModel getRequestParametersKey() {
		return requestParameters;
	}
	
	public static FormElementModel getLastRetryDateKey() {
		return lastRetryDate;
	}
	
	public static FormElementModel getAttemptCountKey() {
		return attemptCount;
	}

	public static FormElementModel getStatusKey() {
		return status;
	}

	public static FormElementModel getResultTypeKey() {
		return resultType;
	}

	public static FormElementModel getCompletionDateKey() {
		return completionDate;
	}

	public static FormElementModel getResultFileKey() {
		return resultFile;
	}
	
	public static FormElementModel getFormIdKey() {
	  return formId;
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
	public PersistentResults(ResultType type, Form form, Map<String,String> parameters, Datastore datastore, User user) throws ODKDatastoreException {
		Form persistentResultsForm;
		try {
			persistentResultsForm = Form.retrieveForm(FORM_ID_PERSISTENT_RESULT, datastore, user);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		objectEntity = new Submission(xformPersistentResultsParameters.modelVersion,
								xformPersistentResultsParameters.uiVersion,
								persistentResultsForm.getFormDefinition(), datastore, user);
		setRequestingUser(user.getUriUser());
		Date now = new Date();
		setRequestDate(now);
		setRequestParameters(parameters);
		setLastRetryDate(now);
		setAttemptCount(1L);
		setStatus(Status.GENERATION_IN_PROGRESS);
		setResultType(type);
		setFormId(form.getFormId());
		
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

	public Map<String,String> getRequestParameters() throws ODKDatastoreException {
		String parameterDocument = ((StringSubmissionType) objectEntity.getElementValue(requestParameters)).getValue();
		try {
			return PropertyMapSerializer.deserializeRequestParameters(parameterDocument);
		} catch ( Exception e ) {
			throw new ODKDatastoreException("bad parameter list in database", e);
		}
	}

	public void setRequestParameters(Map<String,String> value) throws ODKEntityPersistException {
		((StringSubmissionType) objectEntity.getElementValue(requestParameters)).setValueFromString(
					PropertyMapSerializer.serializeRequestParameters(value));
	}

	public Date getLastRetryDate() {
		return ((DateSubmissionType) objectEntity.getElementValue(lastRetryDate)).getValue();
	}

	public void setLastRetryDate(Date value) {
		((DateSubmissionType) objectEntity.getElementValue(lastRetryDate)).setValueFromDate(value);
	}
	
	public Long getAttemptCount() {
		return ((LongSubmissionType) objectEntity.getElementValue(attemptCount)).getValue();
	}
	
	public void setAttemptCount(Long value) {
		((LongSubmissionType) objectEntity.getElementValue(attemptCount)).setValue(value);
	}
	
	public Status getStatus() {
		return Status.valueOf(((StringSubmissionType) objectEntity.getElementValue(status)).getValue());
	}
	
	public void setStatus(Status value) throws ODKEntityPersistException {
		((StringSubmissionType) objectEntity.getElementValue(status)).setValueFromString(value.name());
	}
	
	public ResultType getResultType() {
		return ResultType.valueOf(((StringSubmissionType) objectEntity.getElementValue(resultType)).getValue());
	}
	
	public void setResultType(ResultType value) throws ODKEntityPersistException {
		((StringSubmissionType) objectEntity.getElementValue(resultType)).setValueFromString(value.name());
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
								String unrootedFilePath, Datastore datastore, User user) throws ODKDatastoreException {
		BlobSubmissionType bt = ((BlobSubmissionType) objectEntity.getElementValue(resultFile));
		if ( bt.getAttachmentCount() > 0 ) {
			throw new IllegalStateException("Results are already attached!");
		}
		bt.setValueFromByteArray(byteArray, contentType, contentLength, unrootedFilePath, datastore, user);
	}
	
	public String getFormId() {
	  return ((StringSubmissionType) objectEntity.getElementValue(formId)).getValue();
	}
	
	public void setFormId(String value) throws ODKEntityPersistException {
	  ((StringSubmissionType) objectEntity.getElementValue(PersistentResults.formId)).setValueFromString(value);
	}

	public void deleteResultFile(Datastore datastore, User user) throws ODKDatastoreException {
		BlobSubmissionType bt = ((BlobSubmissionType) objectEntity.getElementValue(resultFile));
		if ( bt.getAttachmentCount() > 0 ) {
			bt.deleteAll(datastore, user);
		}
	}
	
	public void persist(Datastore datastore, User user) throws ODKEntityPersistException {
		objectEntity.persist(datastore, user);
	}
	
	public void delete(Datastore datastore, User user) throws ODKDatastoreException {
		List<EntityKey> keys = new ArrayList<EntityKey>();
		objectEntity.recursivelyAddEntityKeys(keys);
		keys.add(objectEntity.getKey());
		datastore.deleteEntities(keys, user);
	}
	
	public SubmissionKey getSubmissionKey() {
		return objectEntity.constructSubmissionKey(null);
	}
	
	public static final List<PersistentResults> getStalledRequests(Datastore datastore, User user) throws ODKDatastoreException {
		Form form;
		try {
			form = Form.retrieveForm(FORM_ID_PERSISTENT_RESULT, datastore, user);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		Query q = datastore.createQuery(form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), user);
		Date now = new Date();
	
		Date limit = new Date(now.getTime() - RETRY_INTERVAL_MILLISECONDS );
		q.addFilter(getLastRetryDateKey().getFormDataModel().getBackingKey(), FilterOperation.LESS_THAN, limit );
		List<? extends CommonFieldsBase> l = q.executeQuery(0);
		/*
		 * The list of objects consists only of those that were last 
		 * fired at a lastRetryDate older than the retry interval, which
		 * should be longer than the allowed Task lifetime.
		 */
		List<PersistentResults> r = new ArrayList<PersistentResults>();
		for ( CommonFieldsBase b : l ) {
			Submission s = new Submission( b.getUri(), form, datastore, user );
			PersistentResults result = new PersistentResults(s);
			if ( result.getStatus() == Status.AVAILABLE ) continue;
			if ( result.getStatus() == Status.ABANDONED ) continue;
			if ( result.getAttemptCount() >= MAX_RETRY_ATTEMPTS ) {
				// the task is stale, and should be marked abandoned,
				// but the worker thread must have failed.  Attempt 
				// it here...
				result.setAttemptCount(result.getAttemptCount()+1L);
				result.setStatus(Status.ABANDONED);
				result.setCompletionDate(now);
				result.objectEntity.persist(datastore, user);
				continue;
			}
			// OK.  If we are here, a task was last fired for this request
			// more than the retry interval ago and the task is eligible
			// to be restarted.
			r.add(result);
		}
		return r;
	}

	public static List<PersistentResults> getAllTasksForForm(Form theForm,
			Datastore datastore, User user) throws ODKDatastoreException {
		Form form;
		try {
			form = Form.retrieveForm(FORM_ID_PERSISTENT_RESULT, datastore, user);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		Query q = datastore.createQuery(form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), user);
		q.addFilter(PersistentResults.getFormIdKey().getFormDataModel().getBackingKey(), FilterOperation.EQUAL, theForm.getFormId());
		List<? extends CommonFieldsBase> l = q.executeQuery(0);
		
		List<PersistentResults> r = new ArrayList<PersistentResults>();
		for ( CommonFieldsBase b : l ) {
			Submission s = new Submission( b.getUri(), form, datastore, user );
			PersistentResults result = new PersistentResults(s);
			r.add(result);
		}
		return r;
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
		
		private static final DataField REQUEST_PARAMETERS = new DataField("REQUEST_PARAMETERS",
				DataField.DataType.STRING, true, 4096L);

		private static final DataField LAST_RETRY_DATE = new DataField("LAST_RETRY_DATE",
				DataField.DataType.DATETIME, true);
	
		private static final DataField ATTEMPT_COUNT = new DataField("ATTEMPT_COUNT",
				DataField.DataType.INTEGER, true);
	
		private static final DataField STATUS = new DataField("STATUS",
				DataField.DataType.STRING, true);

		private static final DataField RESULT_TYPE = new DataField("RESULT_TYPE",
				DataField.DataType.STRING, true);
	
		private static final DataField COMPLETION_DATE = new DataField("COMPLETION_DATE",
				DataField.DataType.DATETIME, true);
		
		private static final DataField FORM_ID = new DataField("FORM_ID_KEY",
		      DataField.DataType.STRING, true);
	
		public final DataField requestingUser;
		public final DataField requestDate;
		public final DataField requestParameters;
		public final DataField lastRetryDate;
		public final DataField attemptCount;
		public final DataField status;
		public final DataField resultType;
		public final DataField completionDate;
		public final DataField formId;
	
		// additional virtual DataField -- binary content
		
		static final String ELEMENT_NAME_RESULT_FILE_DEFINITION = "resultFile";
	
		private static final String PERSISTENT_RESULT_FILE_REF_BLOB = "_persistent_result_file_blb";
	
		private static final String PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT_REF_BLOB = "_persistent_result_file_ref";
	
		private static final String PERSISTENT_RESULT_FILE_VERSIONED_BINARY_CONTENT = "_persistent_result_file_vbn";
	
		private static final String PERSISTENT_RESULT_FILE_BINARY_CONTENT = "_persistent_result_file_bin";
	
		// additional virtual DataField -- long string text
		
		private static final String PERSISTENT_RESULT_REF_TEXT = "_persistent_result_string_txt";
	
		private static final String PERSISTENT_RESULT_LONG_STRING_REF_TEXT = "_persistent_result_string_ref";
	
		/**
		 * Construct a relation prototype.
		 * 
		 * @param databaseSchema
		 */
		private PersistentResultsTable(String databaseSchema) {
			super(databaseSchema, TABLE_NAME);
			fieldList.add(requestingUser = new DataField(REQUESTING_USER));
			fieldList.add(requestDate = new DataField(REQUEST_DATE));
			fieldList.add(requestParameters = new DataField(REQUEST_PARAMETERS));
			fieldList.add(lastRetryDate = new DataField(LAST_RETRY_DATE));
			fieldList.add(attemptCount = new DataField(ATTEMPT_COUNT));
			fieldList.add(status = new DataField(STATUS));
			fieldList.add(resultType = new DataField(RESULT_TYPE));
			fieldList.add(completionDate = new DataField(COMPLETION_DATE));
			fieldList.add(formId = new DataField(FORM_ID));
	
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
			requestParameters = ref.requestParameters;
			lastRetryDate = ref.lastRetryDate;
			attemptCount = ref.attemptCount;
			status = ref.status;
			resultType = ref.resultType;
			completionDate = ref.completionDate;
			formId = ref.formId;
		}
	
		@Override
		public PersistentResultsTable getEmptyRow(User user) {
			return new PersistentResultsTable(this, user);
		}
		
		private static PersistentResultsTable relation = null;
		
		static synchronized final PersistentResultsTable createRelation(Datastore datastore, User user) throws ODKDatastoreException {
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
				uriPrefix + PersistentResultsTable.PERSISTENT_RESULT_LONG_STRING_REF_TEXT, 
				PersistentResultsTable.PERSISTENT_RESULT_LONG_STRING_REF_TEXT, 
				uriPrefix + PersistentResultsTable.PERSISTENT_RESULT_REF_TEXT, 
				PersistentResultsTable.PERSISTENT_RESULT_REF_TEXT, 
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
		requestParameters = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.requestParameters);
		lastRetryDate = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.lastRetryDate);
		attemptCount = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.attemptCount);
		status = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.status);
		resultType = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.resultType);
		completionDate = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.completionDate);
		resultFile = formDefinition.getTopLevelGroupElement().findElementByName(PersistentResultsTable.ELEMENT_NAME_RESULT_FILE_DEFINITION);
		formId = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), PersistentResultsTable.relation.formId);
		

		String persistentResultsUri = persistentResultsRelation.getUri();
		// Now create a record in the FormInfo table for the PersistentResults table itself...
		FormInfoTable fiRelation = Form.getFormInfoRelation(datastore);
		FormDefinition formInfoDefinition = Form.getFormInfoDefinition(datastore);

		FormDefinition.assertFormInfoRecord(xformPersistentResultsParameters, "Result Files", "Result files generated by Aggregate.", persistentResultsUri, datastore, user);
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
