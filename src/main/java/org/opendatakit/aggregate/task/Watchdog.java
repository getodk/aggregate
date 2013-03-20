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
 * Watchdog has two radically divergent implementations.
 * <ul><li>On Tomcat, Watchdog is executed using the spring task framework.</li><li>
 * On GAE, Watchdog is fired every WATCHDOG_RETRY_INTERVAL_MILLISECONDS
 * when the website is active or, if, Watchdog itself determines
 * that there is work, then Watchdog is re-fired every
 * WATCHDOG_BUSY_RETRY_INTERVAL_MILLISECONDS until there is no
 * pending work.</li></ul>
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public interface Watchdog {

  public void setFasterPublishingEnabled(boolean value);

  public boolean getFasterPublishingEnabled();

  /**
   * Invoked when the website is accessed after a period of inactivity.
   *
   * @param cc
   */
  public void onUsage(long delayMilliseconds, CallingContext cc);

  /**
   * @return implemented only on Tomcat for getting CC in task context.
   */
  public CallingContext getCallingContext();
}
