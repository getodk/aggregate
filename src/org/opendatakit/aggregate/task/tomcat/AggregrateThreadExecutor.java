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
package org.opendatakit.aggregate.task.tomcat;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class AggregrateThreadExecutor {

  private static final int NUM_THREADS = 100;
  private static AggregrateThreadExecutor classInstance = null;

  public synchronized static AggregrateThreadExecutor getAggregateThreadExecutor() {
    if (classInstance == null) {
      classInstance = new AggregrateThreadExecutor();
    }
    return classInstance;
  }

  private ScheduledExecutorService exec;

  private AggregrateThreadExecutor() {
    exec = new ScheduledThreadPoolExecutor(NUM_THREADS);
  }

  public void execute(Runnable task) {
    exec.schedule(task, 100, TimeUnit.MILLISECONDS);
  }

  public void schedule(Runnable task, long delayInMilliseconds) {
    exec.schedule(task, delayInMilliseconds, TimeUnit.MILLISECONDS);
  }

  /**
   * Creates and executes a periodic action that becomes enabled first after the
   * given initial delay, and subsequently with the given period; that is
   * executions will commence after initialDelay then initialDelay+period, then
   * initialDelay + 2 * period, and so on. If any execution of the task
   * encounters an exception, subsequent executions are suppressed. Otherwise,
   * the task will only terminate via cancellation or termination of the
   * executor.
   * 
   * @param command
   *          - the task to execute.
   * @param initialDelayInMilliseconds
   *          - the time to delay first execution.
   * @param periodInMilliseconds
   *          - the period between successive executions.
   */
  public void scheduleAtFixedRate(Runnable command, long initialDelayInMilliseconds,
      long periodInMilliseconds) {
    exec.scheduleAtFixedRate(command, initialDelayInMilliseconds, periodInMilliseconds,
        TimeUnit.MILLISECONDS);
  }

  public boolean awaitTermination(long timeout, TimeUnit unit)
		throws InterruptedException {
	return exec.awaitTermination(timeout, unit);
  }

  public boolean isShutdown() {
	return exec.isShutdown();
  }

  public boolean isTerminated() {
	return exec.isTerminated();
  }

  public void shutdown() {
	exec.shutdown();
  }

  public List<Runnable> shutdownNow() {
	return exec.shutdownNow();
  }
}
