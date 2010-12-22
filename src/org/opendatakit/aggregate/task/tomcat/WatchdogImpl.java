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

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.task.CsvGenerator;
import org.opendatakit.aggregate.task.FormDelete;
import org.opendatakit.aggregate.task.KmlGenerator;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.aggregate.task.Watchdog;
import org.opendatakit.aggregate.task.WatchdogWorkerImpl;
import org.opendatakit.aggregate.task.WorksheetCreator;
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
	UploadSubmissions uploadSubmissions = null;
	CsvGenerator csvGenerator = null;
	KmlGenerator kmlGenerator = null;
	FormDelete formDelete = null;
	WorksheetCreator worksheetCreator = null;

	/**
	 * Implementation of CallingContext for use by watchdog-launched tasks.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	public class CallingContextImpl implements CallingContext {

		boolean asDaemon = false;
		
		@Override
		public boolean getAsDeamon() {
			return asDaemon;
		}

		@Override
		public Object getBean(String beanName) {
			if ( BeanDefs.CSV_BEAN.equals(beanName) ) {
				return csvGenerator;
			} else if ( BeanDefs.DATASTORE_BEAN.equals(beanName)) {
				return datastore;
			} else if ( BeanDefs.FORM_DELETE_BEAN.equals(beanName)) {
				return formDelete;
			} else if ( BeanDefs.KML_BEAN.equals(beanName)) {
				return kmlGenerator;
			} else if ( BeanDefs.UPLOAD_TASK_BEAN.equals(beanName)) {
				return uploadSubmissions;
			} else if ( BeanDefs.USER_BEAN.equals(beanName)) {
				return userService;
			} else if ( BeanDefs.WORKSHEET_BEAN.equals(beanName)) {
				return worksheetCreator;
			} 
			throw new IllegalStateException("unable to locate bean");
		}

		@Override
		public User getCurrentUser() {
			return userService.getDaemonAccountUser();
		}

		@Override
		public Datastore getDatastore() {
			return datastore;
		}

		@Override
		public UserService getUserService() {
			return userService;
		}

		@Override
		public void setAsDaemon(boolean asDaemon) {
			this.asDaemon = asDaemon;
		}
	}
	
	static class WatchdogRunner implements Runnable {
		final WatchdogWorkerImpl impl;

		final long checkIntervalMilliseconds;
		final String baseWebServerUrl;
		final CallingContext cc;

		public WatchdogRunner(long checkIntervalMilliseconds,
				String baseWebServerUrl, CallingContext cc) {
			impl = new WatchdogWorkerImpl();
			this.checkIntervalMilliseconds = checkIntervalMilliseconds;
			this.baseWebServerUrl = baseWebServerUrl;
			this.cc = cc;
		}

		@Override
		public void run() {
			try {
				System.out.println("RUNNING WATCHDOG TASK IN TOMCAT");
				impl.checkTasks(checkIntervalMilliseconds, baseWebServerUrl, cc);
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
		WatchdogRunner wr = new WatchdogRunner(checkIntervalMilliseconds,
				baseWebServerUrl, new CallingContextImpl());

		exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
		System.out.println("CREATE WATCHDOG TASK IN TOMCAT");
		exec.scheduleAtFixedRate(wr, 0, checkIntervalMilliseconds);
	}

	@Override
	public boolean isAutoStartup() {
		System.out.println("isAutoStartup WATCHDOG TASK IN TOMCAT");
		return true;
	}

	@Override
	public void stop(Runnable signal) {
		System.out.println("stop(runnable) WATCHDOG TASK IN TOMCAT");
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
		System.out.println("isRunning WATCHDOG TASK IN TOMCAT");
		return isStarted && !exec.isTerminated();
	}

	@Override
	public void start() {
		System.out.println("start WATCHDOG TASK IN TOMCAT");
		// TODO: eliminate this arg
		String baseWebServerUrl = "/ODKAggregatePlatform";
		createWatchdogTask(3 * 60 * 1000, baseWebServerUrl);
		isStarted = true;
	}

	@Override
	public void stop() {
		System.out.println("stop WATCHDOG TASK IN TOMCAT");
		exec.shutdown();
	}

	public int getPhase() {
		System.out.println("getPhase WATCHDOG TASK IN TOMCAT");
		return 10;
	}

	public Datastore getDatastore() {
		return datastore;
	}

	public void setDatastore(Datastore datastore) {
		this.datastore = datastore;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public UploadSubmissions getUploadSubmissions() {
		return uploadSubmissions;
	}

	public void setUploadSubmissions(UploadSubmissions uploadSubmissions) {
		this.uploadSubmissions = uploadSubmissions;
	}

	public CsvGenerator getCsvGenerator() {
		return csvGenerator;
	}

	public void setCsvGenerator(CsvGenerator csvGenerator) {
		this.csvGenerator = csvGenerator;
	}

	public KmlGenerator getKmlGenerator() {
		return kmlGenerator;
	}

	public void setKmlGenerator(KmlGenerator kmlGenerator) {
		this.kmlGenerator = kmlGenerator;
	}

	public FormDelete getFormDelete() {
		return formDelete;
	}

	public void setFormDelete(FormDelete formDelete) {
		this.formDelete = formDelete;
	}

	public WorksheetCreator getWorksheetCreator() {
		return worksheetCreator;
	}

	public void setWorksheetCreator(WorksheetCreator worksheetCreator) {
		this.worksheetCreator = worksheetCreator;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("afterPropertiesSet WATCHDOG TASK IN TOMCAT");
		if ( datastore == null ) throw new IllegalStateException("no datastore specified");
		if ( userService == null ) throw new IllegalStateException("no user service specified");
		if ( uploadSubmissions == null ) throw new IllegalStateException("no uploadSubmissions specified");
		if ( csvGenerator == null ) throw new IllegalStateException("no csvGenerator specified");
		if ( kmlGenerator == null ) throw new IllegalStateException("no kmlGenerator specified");
		if ( formDelete == null ) throw new IllegalStateException("no formDelete specified");
		if ( worksheetCreator == null ) throw new IllegalStateException("no worksheetCreator specified");
	}
}
