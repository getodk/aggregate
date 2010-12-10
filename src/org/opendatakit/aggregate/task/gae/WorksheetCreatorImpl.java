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

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.aggregate.task.gae.servlet.WorksheetServlet;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;

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
  public final void createWorksheetTask(String serverName, String spreadsheetName,
      ExternalServiceOption esType, int delay, Form form, Datastore datastore, User user)
      throws ODKExternalServiceException {
    TaskOptions task = TaskOptions.Builder.withUrl(ServletConsts.WEB_ROOT + WorksheetServlet.ADDR);
    task.method(TaskOptions.Method.GET);
    task.countdownMillis(delay);
    task.param(ExternalServiceConsts.EXT_SERV_ADDRESS, spreadsheetName);
    task.param(ServletConsts.FORM_ID, form.getFormId());
    task.param(ServletConsts.EXTERNAL_SERVICE_TYPE, esType.toString());

    Queue queue = QueueFactory.getDefaultQueue();
    try {
      queue.add(task);
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }
  }

}
