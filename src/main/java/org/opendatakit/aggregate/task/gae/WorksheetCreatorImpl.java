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
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.aggregate.task.gae.servlet.WorksheetServlet;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This is a singleton bean. It cannot have any per-request state. It uses a
 * static inner class to encapsulate the per-request state of a running
 * background task.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class WorksheetCreatorImpl implements WorksheetCreator {

  @Override
  public final void createWorksheetTask(IForm form, MiscTasks miscTasks, long attemptCount,
      CallingContext cc) throws ODKFormNotFoundException, ODKDatastoreException {
    Map<String, String> params = miscTasks.getRequestParameters();

    TaskOptionsBuilder b = new TaskOptionsBuilder(WorksheetServlet.ADDR);
    b.countdownMillis(Math.max(PersistConsts.MAX_SETTLE_MILLISECONDS, SpreadsheetConsts.WORKSHEET_CREATION_DELAY));
    b.param(ServletConsts.FORM_ID, form.getFormId());
    b.param(ExternalServiceConsts.EXT_SERV_ADDRESS,
        params.get(ExternalServiceConsts.EXT_SERV_ADDRESS));
    b.param(ServletConsts.EXTERNAL_SERVICE_TYPE, params.get(ServletConsts.EXTERNAL_SERVICE_TYPE));
    b.param(ServletConsts.MISC_TASKS_KEY, miscTasks.getSubmissionKey().toString());
    b.param(ServletConsts.ATTEMPT_COUNT, Long.toString(attemptCount));
    b.enqueue();
  }

}
