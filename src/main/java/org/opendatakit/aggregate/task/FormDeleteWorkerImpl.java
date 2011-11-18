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
package org.opendatakit.aggregate.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.common.FormActionStatus;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKExternalServiceDependencyException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.MiscTasks.TaskType;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.process.DeleteSubmissions;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Common worker implementation for the deletion of a form.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormDeleteWorkerImpl {

  private final Form form;
  private final SubmissionKey miscTasksKey;
  private final CallingContext cc;
  private final String pFormIdLockId;

  public FormDeleteWorkerImpl(Form form, SubmissionKey miscTasksKey, long attemptCount,
      CallingContext cc) {
    this.form = form;
    this.miscTasksKey = miscTasksKey;
    this.cc = cc;
    pFormIdLockId = UUID.randomUUID().toString();
  }

  public final void deleteForm() throws ODKDatastoreException, ODKFormNotFoundException,
      ODKExternalServiceDependencyException {

    LogFactory.getLog(FormDeleteWorkerImpl.class).info("deletion task: " + miscTasksKey.toString());

    MiscTasks t;
    try {
      t = new MiscTasks(miscTasksKey, cc);
    } catch (Exception e) {
      return;
    }
    // gain lock on the formId itself...
    // the locked resource should be the formId, but for testing
    // it is useful to have the external services collide using
    // formId. Prefix with MT: to indicate that it is a miscellaneousTask
    // lock.
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    String lockedResourceName = t.getMiscTaskLockName();
    TaskLock formIdTaskLock = ds.createTaskLock(user);

    boolean locked = false;
    try {
      if (formIdTaskLock.obtainLock(pFormIdLockId, lockedResourceName, TaskLockType.FORM_DELETION)) {
        locked = true;
      }
      formIdTaskLock = null;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
    }

    if (!locked) {
      return;
    }

    try {
      if (t.getRequestDate().before(form.getCreationDate())) {
        // form is newer, so the task must not refer to this form definition...
        doMarkAsComplete(t);
      } else {
        // deletion request should have been created after the form...
        doDeletion(t);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      formIdTaskLock = ds.createTaskLock(user);
      try {
        for (int i = 0; i < 10; i++) {
          if (formIdTaskLock.releaseLock(pFormIdLockId, lockedResourceName,
              TaskLockType.FORM_DELETION))
            break;
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // just move on, this retry mechanism
            // is to make things nice
          }
        }
      } catch (ODKTaskLockException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * Delete any miscellaneous tasks for this form.
   * 
   * @param self
   * @return
   * @throws ODKDatastoreException
   */
  private boolean deleteMiscTasks(MiscTasks self) throws ODKDatastoreException {
    // first, delete all the tasks for this form.
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    List<MiscTasks> tasks = MiscTasks.getAllTasksForForm(form, cc);
    for (MiscTasks task : tasks) {
      if (task.getUri().equals(self.getUri())) {
        continue; // us!
      }

      if (task.getStatus() == FormActionStatus.ABANDONED
          || task.getStatus() == FormActionStatus.SUCCESSFUL) {
        // already complete -- just delete it.
        task.delete(cc);
        continue;
      }

      TaskType otherType = task.getTaskType();
      if (otherType.equals(self.getTaskType())) {
        // we have already matched on the resource name...
        // we hold this lock already!
        // delete the task...
        task.delete(cc);
        continue;
      }

      // gain the lock and delete the task
      {
        TaskLock taskLock = ds.createTaskLock(user);
        String pLockId = UUID.randomUUID().toString();
        boolean deleted = false;
        try {
          if (taskLock.obtainLock(pLockId, task.getMiscTaskLockName(), task.getTaskType()
              .getLockType())) {
            taskLock = null;
            // delete the task...
            task.delete(cc);
            deleted = true;
          }
        } catch (ODKTaskLockException e1) {
          e1.printStackTrace();
        } finally {
          if (!deleted) {
            // TODO: repost...
            return false;
          }
        }
        // release the lock
        taskLock = ds.createTaskLock(user);
        try {
          for (int i = 0; i < 10; i++) {
            if (taskLock.releaseLock(pLockId, task.getMiscTaskLockName(), task.getTaskType()
                .getLockType()))
              break;
            try {
              Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
            } catch (InterruptedException e) {
              // just move on, this retry mechanism is to only
              // make things
              // nice
            }
          }
        } catch (ODKTaskLockException e) {
          e.printStackTrace();
        }
      }
    }
    return true;
  }

  /**
   * Delete any persistent result tasks (and results files) for this form.
   * 
   * @param self
   * @return
   * @throws ODKDatastoreException
   */
  private boolean deletePersistentResultTasks() throws ODKDatastoreException {
    // first, delete all the tasks for this form.
    List<PersistentResults> tasks = PersistentResults.getAllTasksForForm(form, cc);
    for (PersistentResults task : tasks) {
      // delete the task...
      task.delete(cc);
    }
    return true;
  }

  /**
   * Delete any external service tasks for this form.
   * 
   * @return
   * @throws ODKDatastoreException
   */
  private boolean deleteExternalServiceTasks() throws ODKDatastoreException {
    List<ExternalService> services = FormServiceCursor.getExternalServicesForForm(form, cc);
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    boolean allDeleted = true;
    for (ExternalService service : services) {
      String uriExternalService = service.getFormServiceCursor().getUri();
      TaskLock taskLock = ds.createTaskLock(user);
      String pLockId = UUID.randomUUID().toString();
      boolean deleted = false;
      try {
        if (taskLock.obtainLock(pLockId, uriExternalService, TaskLockType.UPLOAD_SUBMISSION)) {
          taskLock = null;
          service.delete(cc);
          deleted = true;
        }
      } catch (ODKTaskLockException e1) {
        e1.printStackTrace();
      } finally {
        if (!deleted) {
          allDeleted = false;
        }
      }
      taskLock = ds.createTaskLock(user);
      try {
        for (int i = 0; i < 10; i++) {
          if (taskLock.releaseLock(pLockId, uriExternalService, TaskLockType.UPLOAD_SUBMISSION))
            break;
          try {
            Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
          } catch (InterruptedException e) {
            // just move on, this retry mechanism is to only
            // make things
            // nice
          }
        }
      } catch (ODKTaskLockException e) {
        e.printStackTrace();
      }
    }
    return allDeleted;
  }

  private void deleteFilters() throws ODKDatastoreException {

    try {
      List<SubmissionFilterGroup> filterGroupList = SubmissionFilterGroup.getFilterGroupList(
          form.getFormId(), cc);
      for (SubmissionFilterGroup group : filterGroupList) {
        group.delete(cc);
      }
    } catch (Exception e) {
      // TODO: send exception over service
      e.printStackTrace();
    }

  }

  private void doMarkAsComplete(MiscTasks t) throws ODKEntityPersistException, ODKOverQuotaException {
    // and mark us as completed... (don't delete for audit..).
    t.setCompletionDate(new Date());
    t.setStatus(FormActionStatus.SUCCESSFUL);
    t.persist(cc);
  }

  /**
   * we have gained a lock on the form. Now go through and try to delete all
   * other MTs and external services related to this form.
   * 
   * @return true if form is fully deleted...
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws ODKOverQuotaException
   * @throws ODKFormNotFoundException
   */
  private boolean doDeletion(MiscTasks t) throws ODKOverQuotaException, ODKFormNotFoundException, ODKDatastoreException, ODKTaskLockException {

    if (!deleteMiscTasks(t))
      return false;

    deletePersistentResultTasks();

    if (!deleteExternalServiceTasks())
      return false;

    CommonFieldsBase relation = null;
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    // it is possible to have a FormInfo entry without any information
    // on the backing object (no records in FormDataModel). In that
    // case, the formDefinition will be null, causing getTLGE() to throw
    // an exception.
    try {
      relation = form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype();
    } catch (Exception e) {
      relation = null;
    }

    if (relation != null) {
      QueryResumePoint startCursor = null;
      for (;;) {
        // retrieve submissions
        Query surveyQuery = ds.createQuery(relation, "FormDeleteWorkerImpl.doDeletion", user);
        surveyQuery.addSort(relation.lastUpdateDate, Query.Direction.DESCENDING);
        surveyQuery.addSort(relation.primaryKey, Query.Direction.DESCENDING);

        QueryResult result = surveyQuery.executeQuery(startCursor, ServletConsts.FORM_DELETE_RECORD_QUERY_LIMIT);
        startCursor = result.getResumeCursor();
        
        if (result.getResultList().size() == 0)
          break;

        List<SubmissionKey> keys = new ArrayList<SubmissionKey>();
        String topLevelGroupName = form.getTopLevelGroupElement().getElementName();
        for (CommonFieldsBase en : result.getResultList()) {
          TopLevelDynamicBase tl = (TopLevelDynamicBase) en;
          keys.add(new SubmissionKey(form.getFormId(), tl.getModelVersion(), tl.getUiVersion(),
              topLevelGroupName, tl.getUri()));
        }
        DeleteSubmissions delete;
        delete = new DeleteSubmissions(keys);
        delete.deleteSubmissions(cc);

        t.setLastActivityDate(new Date());
        t.persist(cc);
        // renew lock

        TaskLock taskLock = ds.createTaskLock(user);
        // TODO: figure out what to do if this returns false
        taskLock.renewLock(pFormIdLockId, t.getMiscTaskLockName(), t.getTaskType().getLockType());
        taskLock = null;
      }
    }

    // we are avoiding strong locking, so some services might
    // have been set up during the deletion. Delete them.
    if (!deleteExternalServiceTasks())
      return false;

    deletePersistentResultTasks();

    // same is true with other miscellaneous tasks. Delete them.
    if (!deleteMiscTasks(t))
      return false;

    // delete the filters (waiting until the end because a task could have using a filter)
    deleteFilters();
    
    // delete the form.
    form.deleteForm(cc);

    doMarkAsComplete(t);
    return true;
  }
}
