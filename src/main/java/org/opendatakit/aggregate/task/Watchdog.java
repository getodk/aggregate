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

import org.opendatakit.common.web.CallingContext;

/**
 * Watchdog is the coordinating object for the firing of a WatchdogWorkerImpl
 * action, which performs the actual supervisory tests and restarts stalled
 * tasks.
 * <p>
 * Watchdog has two radically divergent implementations.
 * <ul><li>On Tomcat, WatchdogWorkerImpl is executed using the spring task framework.
 * The Watchdog implementation manipulates the Executor from that framework.</li>
 * <li>On GAE, the WatchdogWorkerImpl is fired by either a cron, or through website
 * activity, or via deferred task executions.</li></ul>
 * If there is no pending work, the Watchdog is re-fired every
 * BackendActionsTable.IDLING_WATCHDOG_RETRY_INTERVAL_MILLISECONDS.
 * If there is work, then Watchdog is re-fired every
 * BackendActionsTable.FAST_PUBLISHING_RETRY_MILLISECONDS
 * until there is no pending work.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface Watchdog {

  boolean getFasterWatchdogCycleEnabled();

  void setFasterWatchdogCycleEnabled(boolean value);

  void onUsage(long delayMilliseconds, CallingContext cc);

  CallingContext getCallingContext();
}
