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

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.TaskScheduler;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class AggregrateThreadExecutor {

  private static AggregrateThreadExecutor classInstance = null;
  private TaskScheduler exec;

  private AggregrateThreadExecutor(TaskScheduler taskScheduler) {
    exec = taskScheduler;
  }

  public synchronized static void initialize(TaskScheduler taskScheduler) {
    if (classInstance != null)
      throw new IllegalStateException("called after having set the task scheduler");

    classInstance = new AggregrateThreadExecutor(taskScheduler);
  }

  public synchronized static AggregrateThreadExecutor getAggregateThreadExecutor() {
    if (classInstance == null)
      throw new IllegalStateException("called before having initialized the task scheduler");

    return classInstance;
  }

  public void execute(Runnable task) {
    exec.schedule(task, new Date(System.currentTimeMillis() + 100));
  }

  /**
   * Creates and executes a periodic action whose executions will commence every
   * period milliseconds.  I.e., at t, t+period, t+2*period, and so on. If any
   * execution of the task encounters an exception, subsequent executions are
   * suppressed. Otherwise, the task will only terminate via cancellation or
   * termination of the executor.
   *
   * @param command              - the task to execute.
   * @param periodInMilliseconds - the period between successive executions.
   * @return object that can be used to cancel the fixed-rate task in the executor
   */
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long periodInMilliseconds) {
    return exec.scheduleAtFixedRate(command, periodInMilliseconds);
  }
}
