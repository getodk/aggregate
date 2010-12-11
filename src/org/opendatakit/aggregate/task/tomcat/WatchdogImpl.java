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

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.task.Watchdog;
import org.opendatakit.aggregate.task.WatchdogWorkerImpl;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class WatchdogImpl implements Watchdog {

	static class WatchdogRunner implements Runnable {
		final WatchdogWorkerImpl impl;

		final long checkIntervalMilliseconds;
		final String baseWebServerUrl;
		final Datastore ds;
		final User user;
		
		public WatchdogRunner(long checkIntervalMilliseconds,
				String baseWebServerUrl, Datastore ds, User user) {
			impl = new WatchdogWorkerImpl();
			this.checkIntervalMilliseconds = checkIntervalMilliseconds;
			this.baseWebServerUrl = baseWebServerUrl;
			this.ds = ds;
			this.user = user;
		}

		@Override
		public void run() {
			try {
				impl.checkTasks(checkIntervalMilliseconds,
                baseWebServerUrl, ds, user);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: Problem - decide what to do if an exception occurs
			}
		}
	}


  // TODO: nothing calls this method right now. Figure out how to start watchdog in Tomcat
  public void createWatchdogTask(long checkIntervalMilliseconds, String baseWebServerUrl) {
      Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
      UserService userService = (UserService) ContextFactory.get().getBean(BeanDefs.USER_BEAN);
      User user = userService.getDaemonAccountUser();
	  WatchdogRunner wr = new WatchdogRunner(checkIntervalMilliseconds, baseWebServerUrl,
			  				ds, user);

    System.out.println("THIS IS WATCHDOG TASK IN TOMCAT");
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.scheduleAtFixedRate(wr, 0, checkIntervalMilliseconds);
  }
}
