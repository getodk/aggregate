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
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * Builder for the TaskOptions structure. Also enqueues the created task onto
 * the specified task queue.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class TaskOptionsBuilder {

  public static final String FRONTEND_QUEUE = "frontend-queue";

  private final TaskOptions task;

  TaskOptionsBuilder(String addr) {
    task = TaskOptions.Builder.withUrl(BasicConsts.FORWARDSLASH + addr);
    task.method(TaskOptions.Method.GET);
  }

  public void countdownMillis(long countdownMillis) {
    task.countdownMillis(countdownMillis);
  }

  public void param(String key, String value) {
    task.param(key, value);
  }

  public void enqueue() {
    // these tasks run on the background thread...
	ModulesService modulesApi = ModulesServiceFactory.getModulesService();
	String hostname = modulesApi.getVersionHostname(ServletConsts.BACKEND_GAE_SERVICE, null);
    task.header(ServletConsts.HOST, hostname);

    Queue queue = QueueFactory.getDefaultQueue();
    queue.add(task);
  }

  public void enqueue(String queueName) {
    // the named queue runs on the foreground thread...
    Queue queue = QueueFactory.getQueue(queueName);
    queue.add(task);
  }
}
