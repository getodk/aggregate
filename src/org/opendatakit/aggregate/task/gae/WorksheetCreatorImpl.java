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

import org.opendatakit.aggregate.servlet.WorksheetServlet;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.AbstractWorksheetCreatorImpl;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;

public class WorksheetCreatorImpl extends AbstractWorksheetCreatorImpl {

  @Override
  public final void createWorksheetTask(String appName, String serverName, String spreadsheetName, 
      ExternalServiceOption esType, int delay, Form form, Datastore datastore, User user) throws ODKExternalServiceException {
    TaskOptions task = TaskOptions.Builder.url("/" + WorksheetServlet.ADDR);
    task.method(TaskOptions.Method.GET);
    task.countdownMillis(delay);
    task.param(ServletConsts.SPREADSHEET_NAME_PARAM, spreadsheetName);
    task.param(ServletConsts.ODK_ID, form.getFormId());
    task.param(ServletConsts.EXTERNAL_SERVICE_TYPE, esType.toString());

    Queue queue = QueueFactory.getDefaultQueue();
    try {
      queue.add(task);
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }
  }

}
