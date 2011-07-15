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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.common.FormActionStatus;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormDefinition.OrdinalSequence;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.type.DateSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

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
		WORKSHEET_CREATE(TaskLockType.WORKSHEET_CREATION,10),
		PURGE_OLDER_SUBMISSIONS(TaskLockType.PURGE_OLDER_SUBMISSIONS,10);
		
		private final TaskLockType lockType;
		private final int maxAttemptCount;
		
		private TaskType(TaskLockType type, int maxAttemptCount) {
			this.lockType = type;
			this.maxAttemptCount = maxAttemptCount;
		}
		
		public final String toString() {
			if ( this == DELETE_FORM ) {
				return "Delete form";
			} else if ( this == WORKSHEET_CREATE ) {
				return "Create Google Worsheet";
			} else if ( this == PURGE_OLDER_SUBMISSIONS ) {
				return "Purge Older Submissions";
			} else {
				throw new IllegalStateException("String representation not defined for TaskType " + this.name());
			}
		}
		
		public final TaskLockType getLockType() {
			return lockType;
		}
		
		public final long getMaxAttemptCount() {
			return maxAttemptCount;
		}
	};
	
	public static final String FORM_ID_MISC_TASKS = "aggregate.opendatakit.org:MiscTasks";

	public static final XFormParameters xformMiscTaskParameters = 
		new XFormParameters( FORM_ID_MISC_TASKS, 1L, 0L);

	// delete any successful or abandoned misc tasks older than 30 days
	private static final long MANY_DAYS_AGO = 30*24*60*60*1000L;

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
	public MiscTasks(TaskType type, Form formRequested, Map<String,String> parameters, CallingContext cc) throws ODKDatastoreException {
		Form form;
		try {
			form = Form.retrieveForm(FORM_ID_MISC_TASKS, cc);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		User user = cc.getCurrentUser();
		objectEntity = new Submission(xformMiscTaskParameters.modelVersion,
								xformMiscTaskParameters.uiVersion,
								CommonFieldsBase.newUri(),
								form.getFormDefinition(), cc);
		setFormId(formRequested.getFormId());
		setRequestingUser(user.getUriUser());
		Date now = new Date();
		setRequestDate(now);
		setRequestParameters(parameters);
		setLastActivityDate(now);
		setAttemptCount(1L);
		setStatus(FormActionStatus.IN_PROGRESS);
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
	
	public FormActionStatus getStatus() {
		return FormActionStatus.valueOf(((StringSubmissionType) objectEntity.getElementValue(status)).getValue());
	}
	
	public void setStatus(FormActionStatus value) throws ODKEntityPersistException {
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
	
	public void persist(CallingContext cc) throws ODKEntityPersistException {
		objectEntity.persist(cc);
	}
	
	public void delete(CallingContext cc) throws ODKDatastoreException {
		List<EntityKey> keys = new ArrayList<EntityKey>();
		objectEntity.recursivelyAddEntityKeys(keys, cc);
		keys.add(objectEntity.getKey());
		cc.getDatastore().deleteEntities(keys, cc.getCurrentUser());
	}
	
	public SubmissionKey getSubmissionKey() {
		return objectEntity.constructSubmissionKey(null);
	}
	public static final List<MiscTasks> getStalledRequests(CallingContext cc) throws ODKDatastoreException {
		List<MiscTasks> taskList = new ArrayList<MiscTasks>();
		TaskType[] taskTypes = TaskType.values();
		for ( int i = 0 ; i < taskTypes.length ; ++i ) {
			getStalledTaskRequests(taskList, taskTypes[i], cc);
		}
		// The list, at this point, displays bias toward the first
		// task type.  Reorder the list to bias toward the most-stale
		// task.  This ensures that we process the stalled tasks in
		// a fair order regardless of their resource locking requirements.
		Collections.sort(taskList, new Comparator<MiscTasks>() {
			@Override
			public int compare(MiscTasks o1, MiscTasks o2) {
				return o1.getLastActivityDate().compareTo(o2.getLastActivityDate());
			}			
		});
		return taskList;
	}
	
	public static final void getStalledTaskRequests(List<MiscTasks> taskList, TaskType taskType, CallingContext cc) throws ODKDatastoreException {
		Form form;
		try {
			form = Form.retrieveForm(FORM_ID_MISC_TASKS, cc);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		Query q = cc.getDatastore().createQuery(form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), cc.getCurrentUser());
		Date now = new Date();
		Date ancientTimes = new Date(now.getTime() - MANY_DAYS_AGO);
		// TODO: rework for each task type...
		Date limit = new Date(now.getTime() - taskType.getLockType().getLockExpirationTimeout() - 1 );
		q.addFilter(getTaskTypeKey().getFormDataModel().getBackingKey(), FilterOperation.EQUAL, taskType.name());
		q.addFilter(getLastActivityDateKey().getFormDataModel().getBackingKey(), FilterOperation.LESS_THAN, limit );
		List<? extends CommonFieldsBase> l = q.executeQuery(0);
		/*
		 * The list of objects consists only of those that were last 
		 * fired at a lastActivityDate older than the retry interval, which
		 * should be longer than the allowed Task lifetime.
		 */
		List<MiscTasks> ancientHistory = new ArrayList<MiscTasks>();
		for ( CommonFieldsBase b : l ) {
			Submission s = new Submission( b.getUri(), form, cc );
			MiscTasks result = new MiscTasks(s);
			if ( !result.getStatus().isActiveRequest() ) {
				if ( result.getCompletionDate().before(ancientTimes) ) {
					ancientHistory.add(result);
				}
				continue;
			}
			
			if ( result.getAttemptCount().compareTo(taskType.getMaxAttemptCount()) >= 0 ) {
				// the task is stale, and should be marked abandoned,
				// but the worker thread must have failed.  Attempt 
				// it here...
				result.setAttemptCount(result.getAttemptCount()+1L);
				result.setStatus(FormActionStatus.ABANDONED);
				result.setCompletionDate(now);
				result.objectEntity.persist(cc);
				continue;
			}
			// OK.  If we are here, a task was last fired for this request
			// more than the retry interval ago and the task is eligible
			// to be restarted.
			taskList.add(result);
		}
		
		// purge the ancient history...
		for ( MiscTasks t : ancientHistory ) {
			t.delete(cc);
		}
	}

	public static List<MiscTasks> getAllTasksForForm(Form form, CallingContext cc) throws ODKDatastoreException {
		List<MiscTasks> taskList = new ArrayList<MiscTasks>();
		Form miscTasksForm;
		try {
			miscTasksForm = Form.retrieveForm(FORM_ID_MISC_TASKS, cc);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		User user = cc.getCurrentUser();
		Query q = cc.getDatastore().createQuery(miscTasksForm.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), user);
		q.addFilter(MiscTasks.getFormIdKey().getFormDataModel().getBackingKey(), FilterOperation.EQUAL, form.getFormId());
		// collect all MiscTasks entries that refer to the given form...
		List<? extends CommonFieldsBase> l = q.executeQuery(0);
		for ( CommonFieldsBase b : l ) {
			Submission s = new Submission( b.getUri(), miscTasksForm, cc );
			MiscTasks result = new MiscTasks(s);
			taskList.add(result);
		}
		return taskList;
	}
	
	private static void updateStatusTimestampMap( Map<String,FormActionStatusTimestamp> statusSet, String formId, FormActionStatusTimestamp candidate ) {
		FormActionStatusTimestamp existing = statusSet.get(formId);
		if ( existing == null ) {
			// no existing -- use candidate
			statusSet.put(formId, candidate);
		} else if ( candidate.getStatus().isActiveRequest() ) {
			if ( existing.getStatus().isActiveRequest() ) {
				// figure out which is most current
				if ( existing.getTimestamp().before(candidate.getTimestamp()) ) {
					// if there are multiple actions scheduled, this can bounce between them...
					statusSet.put(formId, candidate);
				}
			} else {
				// existing is a finished request -- replace
				statusSet.put(formId, candidate);
			}
			
		} else if ( !existing.getStatus().isActiveRequest() ) {
			if ( existing.getTimestamp().before(candidate.getTimestamp()) ) {
				// existing is older than candidate -- replace
				statusSet.put(formId, candidate);
			}
		} /* ELSE existing is active and candidate is not -- keep existing */
	}
	
	public static Map<String,FormActionStatusTimestamp> getFormDeletionStatusTimestampOfAllFormIds(CallingContext cc) throws ODKDatastoreException {
		Map<String,FormActionStatusTimestamp> statusSet = new HashMap<String,FormActionStatusTimestamp>();
		Form miscTasksForm;
		try {
			miscTasksForm = Form.retrieveForm(FORM_ID_MISC_TASKS, cc);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		User user = cc.getCurrentUser();
		Query q = cc.getDatastore().createQuery(miscTasksForm.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), user);
		q.addFilter(getTaskTypeKey().getFormDataModel().getBackingKey(), FilterOperation.EQUAL, TaskType.DELETE_FORM.name());
		// collect all Deletion tasks that are in progress or being retried...
		List<? extends CommonFieldsBase> l = q.executeQuery(0);
		for ( CommonFieldsBase b : l ) {
			Submission s = new Submission( b.getUri(), miscTasksForm, cc );
			MiscTasks result = new MiscTasks(s);
			// determine the time of the status setting...
			Date lastUpdate = result.getCompletionDate();
			if ( lastUpdate == null ) {
				lastUpdate = result.getLastActivityDate();// not completed -- try active
			}
			if ( lastUpdate == null ) {
				lastUpdate = result.getRequestDate(); // not yet active -- try requested
			}
			// form the candidate and get the existing value
			FormActionStatusTimestamp candidate = new FormActionStatusTimestamp(result.getStatus(), lastUpdate);
			updateStatusTimestampMap( statusSet, result.getFormId(), candidate);
		}
		return statusSet;
	}
	
	public static Map<String,FormActionStatusTimestamp> getPurgeSubmissionsStatusTimestampOfAllFormIds(CallingContext cc) throws ODKDatastoreException {
		Map<String,FormActionStatusTimestamp> statusSet = new HashMap<String,FormActionStatusTimestamp>();
		Form miscTasksForm;
		try {
			miscTasksForm = Form.retrieveForm(FORM_ID_MISC_TASKS, cc);
		} catch ( ODKFormNotFoundException e) {
			throw new ODKDatastoreException(e);
		}
		User user = cc.getCurrentUser();
		Query q = cc.getDatastore().createQuery(miscTasksForm.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype(), user);
		q.addFilter(getTaskTypeKey().getFormDataModel().getBackingKey(), FilterOperation.EQUAL, TaskType.PURGE_OLDER_SUBMISSIONS.name());
		// collect all Deletion tasks that are in progress or being retried...
		List<? extends CommonFieldsBase> l = q.executeQuery(0);
		for ( CommonFieldsBase b : l ) {
			Submission s = new Submission( b.getUri(), miscTasksForm, cc );
			MiscTasks result = new MiscTasks(s);
			// determine the time of the status setting...
			Date lastUpdate = result.getCompletionDate();
			if ( lastUpdate == null ) {
				lastUpdate = result.getLastActivityDate();// not completed -- try active
			}
			if ( lastUpdate == null ) {
				lastUpdate = result.getRequestDate(); // not yet active -- try requested
			}
			// form the candidate and get the existing value
			FormActionStatusTimestamp candidate = new FormActionStatusTimestamp(result.getStatus(), lastUpdate);
			updateStatusTimestampMap( statusSet, result.getFormId(), candidate);
		}
		return statusSet;
	}
	
	/**
	 * Underlying top-level persistent object for the PerisistentResults form.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	private static final class MiscTasksTable extends TopLevelDynamicBase {

		static final String TABLE_NAME = "_misc_tasks";
	
		private static final String MISC_TASK_DEFINITION_URI = "aggregate.opendatakit.org:MiscTasks-def";
		
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
		
		static synchronized final MiscTasksTable assertRelation(CallingContext cc) throws ODKDatastoreException {
			if ( relation == null ) {
				MiscTasksTable relationPrototype;
				Datastore ds = cc.getDatastore();
				User user = cc.getUserService().getDaemonAccountUser();
				relationPrototype = new MiscTasksTable(ds.getDefaultSchemaName());
			    ds.assertRelation(relationPrototype, user); // may throw exception...
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
	static final void createForm(CallingContext cc) throws ODKDatastoreException {
		
		boolean asDaemon = cc.getAsDeamon();
		try {
			cc.setAsDaemon(true);
			List<FormDataModel> model = new ArrayList<FormDataModel>();
			Datastore ds = cc.getDatastore();
			User user = cc.getCurrentUser();
			FormDataModel.assertRelation(cc);
			SubmissionAssociationTable.assertRelation(cc);
			
			MiscTasksTable miscTasksRelation = MiscTasksTable.assertRelation(cc);
			MiscTasksTable miscTasksDefinition = ds.createEntityUsingRelation(miscTasksRelation, user);
			miscTasksDefinition.setStringField(miscTasksRelation.primaryKey, MiscTasksTable.MISC_TASK_DEFINITION_URI);
			
			OrdinalSequence os = new OrdinalSequence();
			
			String parentTableKey = miscTasksDefinition.getUri();
			
			FormDefinition.buildTableFormDataModel( model, 
					miscTasksDefinition, 
					miscTasksDefinition, // top level table
					parentTableKey, // parent table...
					os,
					cc );
	
			os.ordinal = 2L;
			FormDefinition.buildLongStringFormDataModel(model, 
					MiscTasksTable.MISC_TASKS_LONG_STRING_REF_TEXT, 
					MiscTasksTable.MISC_TASKS_REF_TEXT, 
					miscTasksDefinition, // top level and parent table
					os, 
					cc);
			
			FormDefinition.assertModel(xformMiscTaskParameters, model, cc);
	
			String miscTasksUri = miscTasksRelation.getUri();
			
			// Create a record in the FormInfo table for the MiscTasks table itself...
			FormDefinition.assertFormInfoRecord(xformMiscTaskParameters, "Miscellaneous Tasks", "Miscellaneous tasks run in the background and managed by Aggregate", miscTasksUri, cc);
	
			// we've defined the form -- now fetch the bits of it...
			FormDefinition formDefinition = FormDefinition.getFormDefinition(xformMiscTaskParameters, cc);
			
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
		} finally {
			cc.setAsDaemon(asDaemon);
		}
	}

	/**
	 * Called by the form definition framework to link the form elements back to the
	 * underlying PersistentResultsTable.
	 * 
	 * @param backingTableMap
	 */
	static void populateBackingTableMap(
			Map<String, DynamicCommonFieldsBase> backingTableMap, CallingContext cc) {
		try {
		    DynamicCommonFieldsBase b;
			b = MiscTasksTable.assertRelation(cc);
			backingTableMap.put(b.getSchemaName() + "." + b.getTableName(), b);
		} catch (ODKDatastoreException e) {
			throw new IllegalStateException("the relations should already have been created");
		}
	}
}
