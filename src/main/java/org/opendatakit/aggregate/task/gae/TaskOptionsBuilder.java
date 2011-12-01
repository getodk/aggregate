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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * Builder for the TaskOptions structure.  Also enqueues the created
 * task onto the specified task queue.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class TaskOptionsBuilder {

  private final TaskOptions task;
  
  TaskOptionsBuilder(String addr) {
    task = TaskOptions.Builder.withUrl(BasicConsts.FORWARDSLASH + addr);
    BackendService backendsApi = BackendServiceFactory.getBackendService();
    String hostname = backendsApi.getBackendAddress(ServletConsts.BACKEND_GAE_SERVICE);
    task.header(ServletConsts.HOST, hostname);
    
    task.method(TaskOptions.Method.GET);
  }
  
  public void countdownMillis(long countdownMillis) {
    task.countdownMillis(countdownMillis);
  }
  
  public void param(String key, String value) {
    // the task parameters are just like HTTP parameters in that 
    // they need to be URL-encoded when added to the parameter list.
    try {
      task.param(key, URLEncoder.encode(value, HtmlConsts.UTF8_ENCODE));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      LogFactory.getLog(TaskOptionsBuilder.class).error("Unexpected failure: " + e.toString());
      throw new IllegalStateException("Unexpected failure");
    }
  }
  
  public void enqueue() {
    Queue queue = QueueFactory.getDefaultQueue();
    queue.add(task);
  }
  
  public void enqueue(String queueName) {
    Queue queue = QueueFactory.getQueue(queueName);
    queue.add(task);
  }
}
