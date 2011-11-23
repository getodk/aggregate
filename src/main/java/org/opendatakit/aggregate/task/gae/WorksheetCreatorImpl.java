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
package org.opendatakit.aggregate.task.gae;

import java.util.Map;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.constants.externalservice.SpreadsheetConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.aggregate.task.gae.servlet.WorksheetServlet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
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
public class WorksheetCreatorImpl implements WorksheetCreator {

  @Override
  public final void createWorksheetTask(IForm form,
			SubmissionKey miscTasksKey, long attemptCount,
			CallingContext cc)
      throws ODKFormNotFoundException, ODKDatastoreException {
    MiscTasks r = new MiscTasks(miscTasksKey, cc);
    Map<String, String> params = r.getRequestParameters();

    TaskOptions task = TaskOptions.Builder.withUrl(BasicConsts.FORWARDSLASH + WorksheetServlet.ADDR);
    task.method(TaskOptions.Method.GET);
    task.countdownMillis(SpreadsheetConsts.WORKSHEET_CREATION_DELAY);
    task.param(ServletConsts.FORM_ID, form.getFormId());
    task.param(ExternalServiceConsts.EXT_SERV_ADDRESS, params.get(ExternalServiceConsts.EXT_SERV_ADDRESS));
    task.param(ServletConsts.EXTERNAL_SERVICE_TYPE, params.get(ServletConsts.EXTERNAL_SERVICE_TYPE));
    task.param(ServletConsts.MISC_TASKS_KEY, miscTasksKey.toString());
    task.param(ServletConsts.ATTEMPT_COUNT, Long.toString(attemptCount));

    Queue queue = QueueFactory.getDefaultQueue();
	queue.add(task);
  }

}
