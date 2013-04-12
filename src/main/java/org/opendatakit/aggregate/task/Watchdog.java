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
 *
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
 *
 */
public interface Watchdog {

  /**
   * Triggers the change in behavior between the slow and fast cycles.
   * Note: this does not alter the ServerPreferencesProperties
   * values.  When the next getFasterWatchdogCycleEnabled() is called,
   * the Watchdog may therefore reset itself to the opposite cycle.
   *
   * @param value
   */
  public void setFasterWatchdogCycleEnabled(boolean value);

  /**
   * Get whether or not the watchdog is currently on a fast or slow cycle.
   * This triggers a periodic interrogation of the datastore and will
   * automatically transition the watchdog into the appropriate cycle
   * should the datastore indicate that such a change is necessary.
   *
   * The determination of the Watchdog state honors both:
   * ServerPreferencesProperties.getFasterBackgroundActionsDisabled() and
   * ServerPreferencesProperties.getFasterWatchdogCycleEnabled() settings.
   *
   * @return
   */
  public boolean getFasterWatchdogCycleEnabled();

  /**
   * Invoked to schedule a Watchdog.  This is a no-op on Tomcat, but is
   * the primary means by which GAE schedules watchdog workers.
   *
   * @param cc
   */
  public void onUsage(long delayMilliseconds, CallingContext cc);

  /**
   * @return implemented only on Tomcat for getting CC in task context.
   */
  public CallingContext getCallingContext();
}
