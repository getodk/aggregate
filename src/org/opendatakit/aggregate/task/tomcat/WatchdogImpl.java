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

import java.util.concurrent.TimeUnit;

import org.opendatakit.aggregate.task.Watchdog;
import org.opendatakit.aggregate.task.WatchdogWorkerImpl;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class WatchdogImpl implements Watchdog, SmartLifecycle, InitializingBean {

	boolean isStarted = false;
	AggregrateThreadExecutor exec = AggregrateThreadExecutor
			.getAggregateThreadExecutor();
	Datastore datastore = null;
	UserService userService = null;

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
				System.out.println("RUNNING WATCHDOG TASK IN TOMCAT");
				impl.checkTasks(checkIntervalMilliseconds, baseWebServerUrl,
						ds, user);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: Problem - decide what to do if an exception occurs
			}
		}
	}

	// TODO: nothing calls this method right now. Figure out how to start
	// watchdog in Tomcat
	public void createWatchdogTask(long checkIntervalMilliseconds,
			String baseWebServerUrl) {
		User user = userService.getDaemonAccountUser();
		WatchdogRunner wr = new WatchdogRunner(checkIntervalMilliseconds,
				baseWebServerUrl, datastore, user);

		exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
		System.out.println("THIS IS CREATE WATCHDOG TASK IN TOMCAT");
		exec.scheduleAtFixedRate(wr, 0, checkIntervalMilliseconds);
	}

	@Override
	public boolean isAutoStartup() {
		System.out.println("isAutoStartup IS CREATE WATCHDOG TASK IN TOMCAT");
		return true;
	}

	@Override
	public void stop(Runnable signal) {
		System.out.println("stop(runnable) IS CREATE WATCHDOG TASK IN TOMCAT");
		try {
			exec.shutdown();
			isStarted = false;
			exec.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			signal.run();
		}
	}

	@Override
	public boolean isRunning() {
		System.out.println("isRunning IS CREATE WATCHDOG TASK IN TOMCAT");
		return isStarted && !exec.isTerminated();
	}

	@Override
	public void start() {
		System.out.println("start IS CREATE WATCHDOG TASK IN TOMCAT");
		// TODO: eliminate this arg
		String baseWebServerUrl = "/ODKAggregatePlatform";
		createWatchdogTask(3 * 60 * 1000, baseWebServerUrl);
		isStarted = true;
	}

	@Override
	public void stop() {
		System.out.println("stop IS CREATE WATCHDOG TASK IN TOMCAT");
		exec.shutdown();
	}

	public int getPhase() {
		System.out.println("getPhase IS CREATE WATCHDOG TASK IN TOMCAT");
		return 10;
	}

	public Datastore getDatastore() {
		return datastore;
	}

	public void setDatastore(Datastore datastore) {
		System.out.println("setDatastore IS CREATE WATCHDOG TASK IN TOMCAT");
		this.datastore = datastore;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		System.out.println("setUserService IS CREATE WATCHDOG TASK IN TOMCAT");
		this.userService = userService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("afterPropertiesSet IS CREATE WATCHDOG TASK IN TOMCAT");
		if ( datastore == null ) throw new IllegalStateException("no datastore specified");
		if ( userService == null ) throw new IllegalStateException("no user service specified");
	}
}
