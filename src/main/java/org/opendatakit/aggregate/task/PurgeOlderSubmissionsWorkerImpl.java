/*
 * Copyright (C) 2011 University of Washington
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
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.common.FormActionStatus;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceDependencyException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.process.DeleteSubmissions;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup.CompletionFlag;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Common worker implementation for the purging of all of a form's submissions
 * older than a given date.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class PurgeOlderSubmissionsWorkerImpl {

  private static final int MAX_QUERY_LIMIT = 100;

  private final IForm form;
  private final SubmissionKey miscTasksKey;
  private final CallingContext cc;
  private final String pFormIdLockId;

  public PurgeOlderSubmissionsWorkerImpl(IForm form, SubmissionKey miscTasksKey, long attemptCount,
      CallingContext cc) {
    this.form = form;
    this.miscTasksKey = miscTasksKey;
    this.cc = cc;
    pFormIdLockId = UUID.randomUUID().toString();
  }

  public final void purgeOlderSubmissions() throws ODKDatastoreException, ODKFormNotFoundException,
      ODKExternalServiceDependencyException {

    Log logger = LogFactory.getLog(PurgeOlderSubmissionsWorkerImpl.class);
    logger.info("Beginning Submissions Purge: " + miscTasksKey.toString() + " form "
        + form.getFormId());

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
    } catch (ODKTaskLockException e1) {
      e1.printStackTrace();
    }

    if (!locked) {
      return;
    }

    try {
      if (t.getRequestDate().before(form.getCreationDate())) {
        // form is newer, so the task must not refer to this form definition...
        doMarkAsComplete(t);
      } else {
        // purge request should have been created after the form...
        doPurgeOlderSubmissions(t);
      }
    } catch (Exception e2) {
      e2.printStackTrace();
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

  private List<Submission> querySubmissionsDateRange(Date startDate, Date endDate)
      throws ODKFormNotFoundException, ODKIncompleteSubmissionData, ODKDatastoreException {

    // fetch completed submissions, ascending.  Stop before the endDate.
    FilterGroup filterGroup = new FilterGroup(UIConsts.FILTER_NONE, form.getFormId(), null);
    filterGroup.setQueryFetchLimit(MAX_QUERY_LIMIT);
    QueryByUIFilterGroup query = new QueryByUIFilterGroup(form, filterGroup,
        CompletionFlag.ONLY_COMPLETE_SUBMISSIONS, cc);
    query.addFilterByPrimaryDate(FilterOperation.LESS_THAN, endDate);
    
    // fetch the submissions
    List<Submission> submissions = query.getResultSubmissions(cc);
    return submissions;
  }

  private void doMarkAsComplete(MiscTasks t) throws ODKEntityPersistException,
      ODKOverQuotaException {
    // and mark us as completed... (don't delete for audit..).
    t.setCompletionDate(new Date());
    t.setStatus(FormActionStatus.SUCCESSFUL);
    t.persist(cc);
  }

  /**
   * we have gained a lock on the form. Now go through and try to delete all
   * submissions older than the given date under this form.
   * 
   * @return true if form is fully deleted...
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   */
  private boolean doPurgeOlderSubmissions(MiscTasks t) throws Exception {

    CommonFieldsBase relation = null;
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    Map<String, String> rp = t.getRequestParameters();
    Date purgeBeforeDate = WebUtils.parsePurgeDateString(rp.get(PurgeOlderSubmissions.PURGE_DATE));

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
      for (;;) {
        // retrieve submissions
        Date startDate = BasicConsts.EPOCH;
        List<Submission> submissions = querySubmissionsDateRange(startDate, purgeBeforeDate);

        if (submissions.size() == 0)
          break;

        List<SubmissionKey> keys = new ArrayList<SubmissionKey>();
        for (Submission s : submissions) {
          keys.add(new SubmissionKey(s.getFormId(), s.getFormElementModel().getElementName(), s.getKey().getKey()));
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
    doMarkAsComplete(t);
    return true;
  }
}
