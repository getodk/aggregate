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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
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
 * Table of miscellaneous tasks.  These should be deleted after 
 * a certain number of days... .
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class MiscTasks {
	
	/**
	 * Enumerated values that appear in the TaskType column of the
	 * MiscTasks table.  This has a tie-in to the TaskLockType,
	 * which defines the work time limit of a task, etc.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	public enum TaskType {
		DELETE_FORM(TaskLockType.FORM_DELETION,10),
		WORKSHEET_CREATE(TaskLockType.WORKSHEET_CREATION,10);
		
		private final TaskLockType lockType;
		private final int maxAttemptCount;
		
		private TaskType(TaskLockType type, int maxAttemptCount) {
			this.lockType = type;
			this.maxAttemptCount = maxAttemptCount;
		}
		
		public final String toString() {
			if ( this == DELETE_FORM ) {
				return "Delete form";
			} else {
				return "Create Google Worsheet";
			}
		}
		
		public final TaskLockType getLockType() {
			return lockType;
		}
		
		public final int getMaxAttemptCount() {
			return maxAttemptCount;
		}
	};
	
	/**
	 * Status of a task in the miscTasks table.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	public enum Status {
		IN_PROGRESS, // created or task is running
		RETRY_IN_PROGRESS, // task is running
		FAILED,    // task completed with failure; retry again later.
		ABANDONED, // task completed with failure; no more retries should occur.
		SUCCESSFUL; // task completed successfully.
		
		public String toString() {
			switch ( this ) {
			case IN_PROGRESS:
				return "Operation in progress";
			case RETRY_IN_PROGRESS:
				return "Retry in progress";
			case FAILED:
				return "Failure - will retry later";
			case ABANDONED:
				return "Failure - abandoned all retry attempts";
			case SUCCESSFUL:
				return "Operation completed successfully";
			default:
				throw new IllegalStateException("missing enum case");
			}
		}
	};
	
	public static final String FORM_ID_MISC_TASKS = "aggregate.opendatakit.org:MiscTasks";

	public static final XFormParameters xformMiscTaskParameters = 
		new XFormParameters( FORM_ID_MISC_TASKS, 1L, 0L);

	private static FormElementModel formId;
	private static FormElementModel requestingUser;
	private static FormElementModel requestDate;
	private static FormElementModel requestParameters;
	private static FormElementModel lastActivityDate;
	private static FormElementModel attemptCount;
	private static FormElementModel status;
	private static FormElementModel taskType;
	private static FormElementModel completionDate;

	public static FormElementModel getFormIdKey() {
		return formId;
	}

	public static FormElementModel getRequestingUserKey() {
		return requestingUser;
	}

	public static FormElementModel getRequestDateKey() {
		return requestDate;
	}

	public static FormElementModel getRequestParametersKey() {
		return requestParameters;
	}
	
	public static FormElementModel getLastActivityDateKey() {
		return lastActivityDate;
	}
	
	public static FormElementModel getAttemptCountKey() {
		return attemptCount;
	}

	public static FormElementModel getStatusKey() {
		return status;
	}

	public static FormElementModel getTaskTypeKey() {
		return taskType;
	}

	public static FormElementModel getCompletionDateKey() {
		return completionDate;
	}

	public Submission objectEntity;
	
	/**
	 * After you have a submission (e.g., from a query), create a 
	 * PersistentResults object to wrap it 
	 * and make access to values easier.
	 * 
	 * @param s
	 */
	public MiscTasks(Submission s) {
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
	public MiscTasks(TaskType type, Form formRequested, Map<String,String> parameters, Datastore datastore, User user) throws ODKDatastoreException {
		Form form;
		try {
			form = Form.retrieveForm(FORM_ID_MISC_TASKS, datastore, user);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		objectEntity = new Submission(xformMiscTaskParameters.modelVersion,
								xformMiscTaskParameters.uiVersion,
								form.getFormDefinition(), datastore, user);
		setFormId(formRequested.getFormId());
		setRequestingUser(user.getUriUser());
		Date now = new Date();
		setRequestDate(now);
		setRequestParameters(parameters);
		setLastActivityDate(now);
		setAttemptCount(1L);
		setStatus(Status.IN_PROGRESS);
		setTaskType(type);
		
		objectEntity.setIsComplete(true); // indicate that the data should be visible on queries
		// NOTE: the entity is not yet persisted! 
	}
	
	public String getUri() {
		return objectEntity.getKey().getKey();
	}
	
	public String getFormId() {
		return ((StringSubmissionType) objectEntity.getElementValue(formId)).getValue();
	}
	
	public void setFormId(String value) throws ODKEntityPersistException {
		((StringSubmissionType) objectEntity.getElementValue(formId)).setValueFromString(value);
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

	public Date getLastActivityDate() {
		return ((DateSubmissionType) objectEntity.getElementValue(lastActivityDate)).getValue();
	}

	public void setLastActivityDate(Date value) {
		((DateSubmissionType) objectEntity.getElementValue(lastActivityDate)).setValueFromDate(value);
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
	
	public TaskType getTaskType() {
		return TaskType.valueOf(((StringSubmissionType) objectEntity.getElementValue(taskType)).getValue());
	}
	
	public void setTaskType(TaskType value) throws ODKEntityPersistException {
		((StringSubmissionType) objectEntity.getElementValue(taskType)).setValueFromString(value.name());
	}
	
	public Date getCompletionDate() {
		return ((DateSubmissionType) objectEntity.getElementValue(completionDate)).getValue();
	}
	
	public void setCompletionDate(Date value) {
		((DateSubmissionType) objectEntity.getElementValue(completionDate)).setValueFromDate(value);
	}
	
	public String getMiscTaskLockName() {
		return "MT:" + getFormId();
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
	public static final List<MiscTasks> getStalledRequests(Datastore datastore, User user) throws ODKDatastoreException {
		List<MiscTasks> taskList = new ArrayList<MiscTasks>();
		TaskType[] taskTypes = TaskType.values();
		for ( int i = 0 ; i < taskTypes.length ; ++i ) {
			getStalledTaskRequests(taskList, taskTypes[i], datastore, user);
		}
		// The list, at this point, displays bias toward the first
		// task type.  Reorder the list to bias toward the most-stale
		// task.  This ensures that we process the stalled tasks in
		// a fair order regardless of their resource locking requirements.
		Collections.sort(taskList, new Comparator<MiscTasks>() {
			@Override
			public int compare(MiscTasks o1, MiscTasks o2) {
				return o1.getLastActivityDate().compareTo(o2.getLastActivityDate());// TODO Auto-generated method stub
			}			
		});
		return taskList;
	}
	
	public static final void getStalledTaskRequests(List<MiscTasks> taskList, TaskType taskType, Datastore datastore, User user) throws ODKDatastoreException {
		Form form;
		try {
			form = Form.retrieveForm(FORM_ID_MISC_TASKS, datastore, user);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		Query q = datastore.createQuery(form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), user);
		Date now = new Date();

		// TODO: rework for each task type...
		Date limit = new Date(now.getTime() - taskType.getLockType().getLockExpirationTimeout() - 1 );
		q.addFilter(getLastActivityDateKey().getFormDataModel().getBackingKey(), FilterOperation.LESS_THAN, limit );
		List<? extends CommonFieldsBase> l = q.executeQuery(0);
		/*
		 * The list of objects consists only of those that were last 
		 * fired at a lastActivityDate older than the retry interval, which
		 * should be longer than the allowed Task lifetime.
		 */
		for ( CommonFieldsBase b : l ) {
			Submission s = new Submission( b.getUri(), form, datastore, user );
			MiscTasks result = new MiscTasks(s);
			if ( result.getStatus() == Status.SUCCESSFUL ) continue;
			if ( result.getStatus() == Status.ABANDONED ) continue;
			if ( result.getAttemptCount() >= taskType.getMaxAttemptCount() ) {
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
			taskList.add(result);
		}
	}

	public static List<MiscTasks> getAllTasksForForm(Form form, Datastore datastore,
			User user) throws ODKDatastoreException {
		List<MiscTasks> taskList = new ArrayList<MiscTasks>();
		Form miscTasksForm;
		try {
			miscTasksForm = Form.retrieveForm(FORM_ID_MISC_TASKS, datastore, user);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		Query q = datastore.createQuery(miscTasksForm.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), user);
		q.addFilter(MiscTasks.getFormIdKey().getFormDataModel().getBackingKey(), FilterOperation.EQUAL, form.getFormId());
		// collect all MiscTasks entries that refer to the given form...
		List<? extends CommonFieldsBase> l = q.executeQuery(0);
		for ( CommonFieldsBase b : l ) {
			Submission s = new Submission( b.getUri(), miscTasksForm, datastore, user );
			MiscTasks result = new MiscTasks(s);
			taskList.add(result);
		}
		return taskList;
	}
	
	/**
	 * Underlying top-level persistent object for the PerisistentResults form.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	private static final class MiscTasksTable extends TopLevelDynamicBase {

		static final String TABLE_NAME = "_misc_tasks";
	
		private static final String MISC_TASK_DEFINITION_URI = "aggregate.opendatakit.org:MiscTasks-Definition";
		
		private static final DataField FORM_ID = new DataField("FORM_ID",
				DataField.DataType.STRING, true);
		
		private static final DataField REQUESTING_USER = new DataField("REQUESTING_USER",
				DataField.DataType.STRING, true);
	
		private static final DataField REQUEST_DATE = new DataField("REQUEST_DATE",
				DataField.DataType.DATETIME, true);
		
		private static final DataField REQUEST_PARAMETERS = new DataField("REQUEST_PARAMETERS",
				DataField.DataType.STRING, true, 4096L);

		private static final DataField LAST_ACTIVITY_DATE = new DataField("LAST_ACTIVITY_DATE",
				DataField.DataType.DATETIME, true);
	
		private static final DataField ATTEMPT_COUNT = new DataField("ATTEMPT_COUNT",
				DataField.DataType.INTEGER, true);
	
		private static final DataField STATUS = new DataField("STATUS",
				DataField.DataType.STRING, true);

		private static final DataField TASK_TYPE = new DataField("TASK_TYPE",
				DataField.DataType.STRING, true);
	
		private static final DataField COMPLETION_DATE = new DataField("COMPLETION_DATE",
				DataField.DataType.DATETIME, true);
	
		public final DataField formId;
		public final DataField requestingUser;
		public final DataField requestDate;
		public final DataField requestParameters;
		public final DataField lastActivityDate;
		public final DataField attemptCount;
		public final DataField status;
		public final DataField taskType;
		public final DataField completionDate;
	
		// additional virtual DataField -- long string text
		
		private static final String MISC_TASKS_REF_TEXT = "_misc_task_string_txt";
	
		private static final String MISC_TASKS_LONG_STRING_REF_TEXT = "_misc_task_string_ref";
	
		/**
		 * Construct a relation prototype.
		 * 
		 * @param databaseSchema
		 */
		private MiscTasksTable(String databaseSchema) {
			super(databaseSchema, TABLE_NAME);
			fieldList.add(formId = new DataField(FORM_ID));
			fieldList.add(requestingUser = new DataField(REQUESTING_USER));
			fieldList.add(requestDate = new DataField(REQUEST_DATE));
			fieldList.add(requestParameters = new DataField(REQUEST_PARAMETERS));
			fieldList.add(lastActivityDate = new DataField(LAST_ACTIVITY_DATE));
			fieldList.add(attemptCount = new DataField(ATTEMPT_COUNT));
			fieldList.add(status = new DataField(STATUS));
			fieldList.add(taskType = new DataField(TASK_TYPE));
			fieldList.add(completionDate = new DataField(COMPLETION_DATE));
	
			fieldValueMap.put(primaryKey, CommonFieldsBase.newMD5HashUri(FORM_ID_MISC_TASKS));
		}
	
		/**
		 * Construct an empty entity.
		 * 
		 * @param ref
		 * @param user
		 */
		private MiscTasksTable(MiscTasksTable ref, User user) {
			super(ref, user);
			formId = ref.formId;
			requestingUser = ref.requestingUser;
			requestDate = ref.requestDate;
			requestParameters = ref.requestParameters;
			lastActivityDate = ref.lastActivityDate;
			attemptCount = ref.attemptCount;
			status = ref.status;
			taskType = ref.taskType;
			completionDate = ref.completionDate;
		}
	
		@Override
		public MiscTasksTable getEmptyRow(User user) {
			return new MiscTasksTable(this, user);
		}
		
		private static MiscTasksTable relation = null;
		
		static synchronized final MiscTasksTable createRelation(Datastore datastore, User user) throws ODKDatastoreException {
			if ( relation == null ) {
				MiscTasksTable relationPrototype;
				relationPrototype = new MiscTasksTable(datastore.getDefaultSchemaName());
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
		
		MiscTasksTable miscTasksRelation = MiscTasksTable.createRelation(datastore, user);
		MiscTasksTable miscTasksDefinition = datastore.createEntityUsingRelation(miscTasksRelation, null, user);
		miscTasksDefinition.setStringField(miscTasksRelation.primaryKey, MiscTasksTable.MISC_TASK_DEFINITION_URI);
		
		FormDefinition.buildTableFormDataModel( model, 
				miscTasksRelation, 
				miscTasksDefinition, // top level table
				miscTasksDefinition, // parent table...
				1L,
				datastore, user );

		String uriPrefix = FORM_ID_MISC_TASKS;
		
		FormDefinition.buildLongStringFormDataModel(model, 
				uriPrefix + MiscTasksTable.MISC_TASKS_LONG_STRING_REF_TEXT, 
				MiscTasksTable.MISC_TASKS_LONG_STRING_REF_TEXT, 
				uriPrefix + MiscTasksTable.MISC_TASKS_REF_TEXT, 
				MiscTasksTable.MISC_TASKS_REF_TEXT, 
				miscTasksDefinition, // top level and parent table
				2L, 
				datastore, 
				user);
		
		FormDefinition.assertModel(xformMiscTaskParameters, model, datastore, user);

		FormDefinition formDefinition = FormDefinition.getFormDefinition(xformMiscTaskParameters, datastore, user);
		
		if ( MiscTasksTable.relation != (MiscTasksTable) formDefinition.getTopLevelGroup().getBackingObjectPrototype() ) {
			throw new IllegalStateException("PersistentResults form is not the canonical relation");
		}

		// and discover the form element model values for submissions of this type.
		formId = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.formId);
		requestingUser = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.requestingUser);
		requestDate = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.requestDate);
		requestParameters = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.requestParameters);
		lastActivityDate = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.lastActivityDate);
		attemptCount = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.attemptCount);
		status = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.status);
		taskType = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.taskType);
		completionDate = FormDefinition.findElement(formDefinition.getTopLevelGroupElement(), MiscTasksTable.relation.completionDate);

		String persistentResultsUri = miscTasksRelation.getUri();
		
		FormDefinition.assertFormInfoRecord(xformMiscTaskParameters, "Miscellaneous Tasks", "Miscellaneous tasks run in the background and managed by Aggregate", persistentResultsUri, datastore, user);
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
			b = MiscTasksTable.createRelation(datastore, user);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
		} catch (ODKDatastoreException e) {
			throw new IllegalStateException("the relations should already have been created");
		}
	}
}
