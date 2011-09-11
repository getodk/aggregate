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
package org.opendatakit.aggregate.task.gae;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.PurgeOlderSubmissions;
import org.opendatakit.aggregate.task.gae.servlet.PurgeOlderSubmissionsTaskServlet;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;


/**
 * This is a singleton bean.  It cannot have any per-request state.
 * It uses a static inner class to encapsulate the per-request state
 * of a running background task.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class PurgeOlderSubmissionsImpl implements PurgeOlderSubmissions {

  @Override
  public final void createPurgeOlderSubmissionsTask(Form form, SubmissionKey miscTasksKey, long attemptCount,
	      CallingContext cc) {
    TaskOptions task = TaskOptions.Builder.withUrl(BasicConsts.FORWARDSLASH + PurgeOlderSubmissionsTaskServlet.ADDR);
    task.method(TaskOptions.Method.GET);
    task.countdownMillis(PersistConsts.MIN_SETTLE_MILLISECONDS);
    task.param(ServletConsts.FORM_ID, form.getFormId());
    task.param(ServletConsts.MISC_TASKS_KEY, miscTasksKey.toString());
    task.param(ServletConsts.ATTEMPT_COUNT, Long.toString(attemptCount));
    Queue queue = QueueFactory.getDefaultQueue();
    queue.add(task);
  }

}
