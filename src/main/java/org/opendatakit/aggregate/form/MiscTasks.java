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
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Table of miscellaneous tasks.  These should be deleted after
 * a certain number of days... .
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class MiscTasks {
  private static final String FORM_ID_MISC_TASKS = "aggregate.opendatakit.org:MiscTasks";
  // delete any successful or abandoned misc tasks older than 30 days
  private static final long MANY_DAYS_AGO = 30 * 24 * 60 * 60 * 1000L;

  ;
  private final MiscTasksTable row;

  private MiscTasks(MiscTasksTable row) {
    this.row = row;
  }

  /**
   * Constructor when retrieving a MiscTasks entry from the datastore.
   *
   * @param miscTask -- submission key of the task to retrieve.
   * @param cc
   * @throws ODKDatastoreException
   */
  public MiscTasks(SubmissionKey miscTask, CallingContext cc) throws ODKDatastoreException {
    List<SubmissionKeyPart> parts = miscTask.splitSubmissionKey();
    if (parts == null || parts.size() == 0) {
      throw new IllegalArgumentException("submission key is empty");
    }
    if (!parts.get(0).getElementName().equals(FORM_ID_MISC_TASKS)) {
      throw new IllegalArgumentException("unrecognized form id");
    }
    if (parts.size() < 2) {
      throw new IllegalArgumentException(
          "submission key does not have a top level group");
    }
    SubmissionKeyPart tlg = parts.get(1);
    if (!tlg.getElementName().equals(MiscTasksTable.TABLE_NAME)) {
      throw new IllegalArgumentException("top level group name: "
          + tlg.getElementName()
          + " is not as expected: "
          + MiscTasksTable.TABLE_NAME);
    }
    if (tlg.getAuri() == null) {
      throw new IllegalArgumentException("submission key does not have top level auri");
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    MiscTasksTable relation = MiscTasksTable.assertRelation(cc);
    row = ds.getEntity(relation, tlg.getAuri(), user);
  }

  /**
   * Constructor for a new task.  Note that the created task
   * is not yet persisted.  To persist it, you must call persist(cc)
   *
   * @param requestingUser
   * @param requestDate
   * @param status
   * @param datastore
   * @param user
   * @throws ODKDatastoreException
   */
  public MiscTasks(TaskType type, IForm formRequested, Map<String, String> parameters, CallingContext cc) throws ODKDatastoreException {
    MiscTasksTable relation = MiscTasksTable.assertRelation(cc);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    row = ds.createEntityUsingRelation(relation, user);

    setFormId(formRequested.getFormId());
    setRequestingUser(user.getUriUser());
    Date now = new Date();
    setRequestDate(now);
    setRequestParameters(parameters);
    setLastActivityDate(now);
    setAttemptCount(1L);
    setStatus(FormActionStatus.IN_PROGRESS);
    setTaskType(type);

    // NOTE: the entity is not yet persisted!
  }

  public static final List<MiscTasks> getStalledRequests(CallingContext cc) throws ODKDatastoreException {
    List<MiscTasks> taskList = new ArrayList<MiscTasks>();
    TaskType[] taskTypes = TaskType.values();
    for (int i = 0; i < taskTypes.length; ++i) {
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
    MiscTasksTable relation = MiscTasksTable.assertRelation(cc);
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    Query q = ds.createQuery(relation, "MiscTasks.getStalledTaskRequests", user);
    Date now = new Date();
    Date ancientTimes = new Date(now.getTime() - MANY_DAYS_AGO);
    // TODO: rework for each task type...
    Date limit = new Date(now.getTime() - taskType.getLockType().getLockExpirationTimeout() - PersistConsts.MAX_SETTLE_MILLISECONDS);
    q.addFilter(MiscTasksTable.TASK_TYPE, FilterOperation.EQUAL, taskType.name());
    q.addSort(MiscTasksTable.TASK_TYPE, Direction.ASCENDING); // GAE work-around
    q.addFilter(MiscTasksTable.LAST_ACTIVITY_DATE, FilterOperation.LESS_THAN, limit);
    List<? extends CommonFieldsBase> l = q.executeQuery();
    /*
     * The list of objects consists only of those that were last
     * fired at a lastActivityDate older than the retry interval, which
     * should be longer than the allowed Task lifetime.
     */
    List<MiscTasks> ancientHistory = new ArrayList<MiscTasks>();
    for (CommonFieldsBase b : l) {
      MiscTasksTable r = (MiscTasksTable) b;
      MiscTasks result = new MiscTasks(r);
      if (!result.getStatus().isActiveRequest()) {
        if (result.getCompletionDate().before(ancientTimes)) {
          ancientHistory.add(result);
        }
        continue;
      }

      if (result.getAttemptCount().compareTo(taskType.getMaxAttemptCount()) >= 0) {
        // the task is stale, and should be marked abandoned,
        // but the worker thread must have failed.  Attempt
        // it here...
        result.setAttemptCount(result.getAttemptCount() + 1L);
        result.setStatus(FormActionStatus.ABANDONED);
        result.setCompletionDate(now);
        result.persist(cc);
        continue;
      }
      // OK.  If we are here, a task was last fired for this request
      // more than the retry interval ago and the task is eligible
      // to be restarted.
      taskList.add(result);
    }

    // purge the ancient history...
    for (MiscTasks t : ancientHistory) {
      t.delete(cc);
    }
  }

  public static List<MiscTasks> getAllTasksForForm(IForm form, CallingContext cc) throws ODKDatastoreException {
    List<MiscTasks> taskList = new ArrayList<MiscTasks>();
    MiscTasksTable relation = MiscTasksTable.assertRelation(cc);
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    Query q = ds.createQuery(relation, "MiscTasks.getAllTasksForForm", user);
    q.addFilter(MiscTasksTable.FORM_ID, FilterOperation.EQUAL, form.getFormId());
    // collect all MiscTasks entries that refer to the given form...
    List<? extends CommonFieldsBase> l = q.executeQuery();
    for (CommonFieldsBase b : l) {
      MiscTasksTable r = (MiscTasksTable) b;
      MiscTasks result = new MiscTasks(r);
      taskList.add(result);
    }
    return taskList;
  }

  private static void updateStatusTimestampMap(Map<String, FormActionStatusTimestamp> statusSet, String formId, FormActionStatusTimestamp candidate) {
    FormActionStatusTimestamp existing = statusSet.get(formId);
    if (existing == null) {
      // no existing -- use candidate
      statusSet.put(formId, candidate);
    } else if (candidate.getStatus().isActiveRequest()) {
      if (existing.getStatus().isActiveRequest()) {
        // figure out which is most current
        if (existing.getTimestamp().before(candidate.getTimestamp())) {
          // if there are multiple actions scheduled, this can bounce between them...
          statusSet.put(formId, candidate);
        }
      } else {
        // existing is a finished request -- replace
        statusSet.put(formId, candidate);
      }

    } else if (!existing.getStatus().isActiveRequest()) {
      if (existing.getTimestamp().before(candidate.getTimestamp())) {
        // existing is older than candidate -- replace
        statusSet.put(formId, candidate);
      }
    } /* ELSE existing is active and candidate is not -- keep existing */
  }

  public static FormActionStatusTimestamp getFormDeletionStatusTimestampOfFormId(String formId, CallingContext cc) throws ODKDatastoreException {
    Map<String, FormActionStatusTimestamp> thisFormId = getFormDeletionStatusTimestampOfAllFormIds(formId, cc);
    return thisFormId.get(formId);
  }

  public static Map<String, FormActionStatusTimestamp> getFormDeletionStatusTimestampOfAllFormIds(CallingContext cc) throws ODKDatastoreException {
    return getFormDeletionStatusTimestampOfAllFormIds(null, cc);
  }

  /**
   * Returns the full map or filters it to just the entry matching the formId
   *
   * @param formId
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  private static Map<String, FormActionStatusTimestamp> getFormDeletionStatusTimestampOfAllFormIds(String formId, CallingContext cc) throws ODKDatastoreException {
    Map<String, FormActionStatusTimestamp> statusSet = new HashMap<String, FormActionStatusTimestamp>();
    MiscTasksTable relation = MiscTasksTable.assertRelation(cc);
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    Query q = ds.createQuery(relation, "MiscTasks.getFormDeletionStatusTimestampOfAllFormIds", user);
    q.addFilter(MiscTasksTable.TASK_TYPE, FilterOperation.EQUAL, TaskType.DELETE_FORM.name());
    // collect all Deletion tasks that are in progress or being retried...
    List<? extends CommonFieldsBase> l = q.executeQuery();
    for (CommonFieldsBase b : l) {
      MiscTasksTable r = (MiscTasksTable) b;
      MiscTasks result = new MiscTasks(r);

      if (formId != null && !formId.equals(result.getFormId())) {
        continue;
      }
      // ignore abandoned or successful deletion requests...
      if (result.getStatus() == FormActionStatus.SUCCESSFUL ||
          result.getStatus() == FormActionStatus.ABANDONED) continue;

      // determine the time of the status setting...
      Date lastUpdate = result.getCompletionDate();
      if (lastUpdate == null) {
        lastUpdate = result.getLastActivityDate();// not completed -- try active
      }
      if (lastUpdate == null) {
        lastUpdate = result.getRequestDate(); // not yet active -- try requested
      }
      // form the candidate and get the existing value
      FormActionStatusTimestamp candidate = new FormActionStatusTimestamp(result.getStatus(), lastUpdate);
      updateStatusTimestampMap(statusSet, result.getFormId(), candidate);
    }
    return statusSet;
  }

  public static Map<String, FormActionStatusTimestamp> getPurgeSubmissionsStatusTimestampOfAllFormIds(CallingContext cc) throws ODKDatastoreException {
    Map<String, FormActionStatusTimestamp> statusSet = new HashMap<String, FormActionStatusTimestamp>();

    MiscTasksTable relation = MiscTasksTable.assertRelation(cc);
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    Query q = ds.createQuery(relation, "MiscTasks.getPurgeSubmissionsStatusTimestampOfAllFormIds", user);
    q.addFilter(MiscTasksTable.TASK_TYPE, FilterOperation.EQUAL, TaskType.PURGE_OLDER_SUBMISSIONS.name());
    // collect all Deletion tasks that are in progress or being retried...
    List<? extends CommonFieldsBase> l = q.executeQuery();
    for (CommonFieldsBase b : l) {
      MiscTasksTable r = (MiscTasksTable) b;
      MiscTasks result = new MiscTasks(r);
      // ignore abandoned or successful deletion requests...
      if (result.getStatus() == FormActionStatus.SUCCESSFUL ||
          result.getStatus() == FormActionStatus.ABANDONED) continue;

      // determine the time of the status setting...
      Date lastUpdate = result.getCompletionDate();
      if (lastUpdate == null) {
        lastUpdate = result.getLastActivityDate();// not completed -- try active
      }
      if (lastUpdate == null) {
        lastUpdate = result.getRequestDate(); // not yet active -- try requested
      }
      // form the candidate and get the existing value
      FormActionStatusTimestamp candidate = new FormActionStatusTimestamp(result.getStatus(), lastUpdate);
      updateStatusTimestampMap(statusSet, result.getFormId(), candidate);
    }
    return statusSet;
  }

  public String getUri() {
    return row.getUri();
  }

  public String getFormId() {
    return row.getStringField(MiscTasksTable.FORM_ID);
  }

  public void setFormId(String value) throws ODKEntityPersistException {
    if (!row.setStringField(MiscTasksTable.FORM_ID, value)) {
      throw new IllegalArgumentException("formId is too long");
    }
  }

  public void setRequestingUser(String value) throws ODKEntityPersistException {
    if (!row.setStringField(MiscTasksTable.REQUESTING_USER, value)) {
      throw new IllegalArgumentException("requestingUser is too long");
    }
  }

  public Date getRequestDate() {
    return row.getDateField(MiscTasksTable.REQUEST_DATE);
  }

  public void setRequestDate(Date value) {
    row.setDateField(MiscTasksTable.REQUEST_DATE, value);
  }

  public Map<String, String> getRequestParameters() throws ODKDatastoreException {
    String parameterDocument = row.getStringField(MiscTasksTable.REQUEST_PARAMETERS);
    try {
      return PropertyMapSerializer.deserializeRequestParameters(parameterDocument);
    } catch (Exception e) {
      throw new ODKDatastoreException("bad parameter list in database", e);
    }
  }

  public void setRequestParameters(Map<String, String> value) throws ODKEntityPersistException {
    if (!row.setStringField(MiscTasksTable.REQUEST_PARAMETERS,
        PropertyMapSerializer.serializeRequestParameters(value))) {
      throw new IllegalStateException("overflowed requestParameters");
    }
  }

  public Date getLastActivityDate() {
    return row.getDateField(MiscTasksTable.LAST_ACTIVITY_DATE);
  }

  public void setLastActivityDate(Date value) {
    row.setDateField(MiscTasksTable.LAST_ACTIVITY_DATE, value);
  }

  public Long getAttemptCount() {
    return row.getLongField(MiscTasksTable.ATTEMPT_COUNT);
  }

  public void setAttemptCount(Long value) {
    row.setLongField(MiscTasksTable.ATTEMPT_COUNT, value);
  }

  public FormActionStatus getStatus() {
    return FormActionStatus.valueOf(row.getStringField(MiscTasksTable.STATUS));
  }

  public void setStatus(FormActionStatus value) throws ODKEntityPersistException {
    if (!row.setStringField(MiscTasksTable.STATUS, value.name())) {
      throw new IllegalStateException("overflow status");
    }
  }

  public TaskType getTaskType() {
    return TaskType.valueOf(row.getStringField(MiscTasksTable.TASK_TYPE));
  }

  public void setTaskType(TaskType value) throws ODKEntityPersistException {
    if (!row.setStringField(MiscTasksTable.TASK_TYPE, value.name())) {
      throw new IllegalStateException("overflow taskType");
    }
  }

  public Date getCompletionDate() {
    return row.getDateField(MiscTasksTable.COMPLETION_DATE);
  }

  public void setCompletionDate(Date value) {
    row.setDateField(MiscTasksTable.COMPLETION_DATE, value);
  }

  public String getMiscTaskLockName() {
    return "MT:" + getFormId();
  }

  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    ds.putEntity(row, user);
  }

  public void delete(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    ds.deleteEntity(row.getEntityKey(), user);
  }

  public SubmissionKey getSubmissionKey() {
    return new SubmissionKey(FORM_ID_MISC_TASKS + "[@version=null and @uiVersion=null]/" + MiscTasksTable.TABLE_NAME + "[@key=" + row.getUri() + "]");
  }

  /**
   * Enumerated values that appear in the TaskType column of the
   * MiscTasks table.  This has a tie-in to the TaskLockType,
   * which defines the work time limit of a task, etc.
   *
   * @author mitchellsundt@gmail.com
   */
  public enum TaskType {
    DELETE_FORM(TaskLockType.FORM_DELETION, 10),
    WORKSHEET_CREATE(TaskLockType.WORKSHEET_CREATION, 10),
    PURGE_OLDER_SUBMISSIONS(TaskLockType.PURGE_OLDER_SUBMISSIONS, 10);

    private final TaskLockType lockType;
    private final int maxAttemptCount;

    private TaskType(TaskLockType type, int maxAttemptCount) {
      this.lockType = type;
      this.maxAttemptCount = maxAttemptCount;
    }

    public final String toString() {
      if (this == DELETE_FORM) {
        return "Delete form";
      } else if (this == WORKSHEET_CREATE) {
        return "Create Google Worsheet";
      } else if (this == PURGE_OLDER_SUBMISSIONS) {
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
  }

  /**
   * Underlying top-level persistent object for the PerisistentResults form.
   *
   * @author mitchellsundt@gmail.com
   */
  private static final class MiscTasksTable extends CommonFieldsBase {

    static final String TABLE_NAME = "_misc_tasks";

    private static final DataField FORM_ID = new DataField("FORM_ID",
        DataField.DataType.STRING, true, IForm.MAX_FORM_ID_LENGTH);

    private static final DataField REQUESTING_USER = new DataField("REQUESTING_USER",
        DataField.DataType.STRING, true);

    private static final DataField REQUEST_DATE = new DataField("REQUEST_DATE",
        DataField.DataType.DATETIME, true);

    private static final DataField REQUEST_PARAMETERS = new DataField("REQUEST_PARAMETERS",
        DataField.DataType.STRING, true, 8192L);

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
    private static MiscTasksTable relation = null;

    /**
     * Construct a relation prototype.
     *
     * @param databaseSchema
     */
    private MiscTasksTable(String databaseSchema) {
      super(databaseSchema, TABLE_NAME);
      fieldList.add(FORM_ID);
      fieldList.add(REQUESTING_USER);
      fieldList.add(REQUEST_DATE);
      fieldList.add(REQUEST_PARAMETERS);
      fieldList.add(LAST_ACTIVITY_DATE);
      fieldList.add(ATTEMPT_COUNT);
      fieldList.add(STATUS);
      fieldList.add(TASK_TYPE);
      fieldList.add(COMPLETION_DATE);
    }

    /**
     * Construct an empty entity.
     *
     * @param ref
     * @param user
     */
    private MiscTasksTable(MiscTasksTable ref, User user) {
      super(ref, user);
    }

    static synchronized final MiscTasksTable assertRelation(CallingContext cc) throws ODKDatastoreException {
      if (relation == null) {
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

    @Override
    public MiscTasksTable getEmptyRow(User user) {
      return new MiscTasksTable(this, user);
    }
  }
}
