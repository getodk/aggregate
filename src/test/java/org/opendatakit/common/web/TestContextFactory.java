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

package org.opendatakit.common.web;

import javax.servlet.ServletContext;

import org.junit.Ignore;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.constants.BasicConsts;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Ignore("not a test")
public class TestContextFactory {

	  public static final String USER_BEAN = "user_service";
	  public static final String DATASTORE_BEAN = "datastore";

	    /**
	     * Singleton of the application context
	     */
	    private static final String APP_CONTEXT_PATH = "odk-settings.xml";
	    private static final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(APP_CONTEXT_PATH);

	    @Ignore("not a test")
	    public static final class CallingContextImpl implements CallingContext {
	    	final String serverUrl;
	    	final String secureServerUrl;
	    	final String webApplicationBase;
	    	final Datastore datastore;
	    	final UserService userService;
	    	boolean asDaemon = true; // otherwise there isn't a current user...

	    	CallingContextImpl() {
	    	    String baseUrl = System.getProperty("test.server.baseUrl","");
	    	    if ( baseUrl.length() > 0 && !baseUrl.startsWith(BasicConsts.FORWARDSLASH)) {
	    	    	baseUrl = BasicConsts.FORWARDSLASH + baseUrl;
	    	    }
	    	    webApplicationBase = baseUrl;
	    	    String hostname = System.getProperty("test.server.hostname", "localhost");
	    	    String port = System.getProperty("test.server.port","8888");
	    	    String secureport = System.getProperty("test.server.secure.port","8443");
	    		serverUrl = "http://" + hostname + ":" + port + webApplicationBase;
	    		secureServerUrl = "https://" + hostname + ":" + secureport + webApplicationBase;
	    		datastore = (Datastore) applicationContext.getBean(DATASTORE_BEAN);
	    		userService = (UserService) applicationContext.getBean(USER_BEAN);
	    	}

			@Override
	    	public Object getBean(String beanName) {
				return applicationContext.getBean(beanName);
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
			public ServletContext getServletContext() {
				return null;
			}

			@Override
	    	public String getWebApplicationURL() {
	    		return webApplicationBase + BasicConsts.FORWARDSLASH;
	    	}

			@Override
	    	public String getWebApplicationURL(String servletAddr) {
	    		return webApplicationBase + BasicConsts.FORWARDSLASH + servletAddr;
	    	}

			@Override
	    	public String getServerURL() {
	    		return serverUrl;
	    	}

			@Override
			public String getSecureServerURL() {
				return secureServerUrl;
			}

			@Override
	    	public void setAsDaemon(boolean asDaemon ) {
	    		this.asDaemon = asDaemon;
	    	}

			@Override
	    	public boolean getAsDeamon() {
	    		return asDaemon;
	    	}

			@Override
	    	public User getCurrentUser() {
	    		return asDaemon ? userService.getDaemonAccountUser() : userService.getCurrentUser();
	    	}
	    }

	    /**
	     * Private constructor
	     */
	    private TestContextFactory() {}

	    public static CallingContext getCallingContext() {
	    	return new CallingContextImpl();
	    }

}
