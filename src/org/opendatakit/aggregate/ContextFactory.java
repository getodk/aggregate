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


package org.opendatakit.aggregate;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Server Context creates a singleton for application context to prevent unnecessary construction
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ContextFactory {
  
  
    /**
     * Singleton of the application context
     */
    // TODO: write a CallingContextImpl that uses a standalone applicationContext for unit testing.
	//
    // private static final String APP_CONTEXT_PATH = "odk-settings.xml";
    // private static final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(APP_CONTEXT_PATH);

    public static final class CallingContextImpl implements CallingContext {
    	final String serverUrl;
    	final String webApplicationBase;
    	final ServletContext ctxt;
    	final Datastore datastore;
    	final UserService userService;
    	boolean asDaemon = false;
    	
    	CallingContextImpl(HttpServlet servlet, HttpServletRequest req) {
    		// for now, only store the servlet context and the serverUrl
    		ctxt = servlet.getServletContext();
    		String path = ctxt.getContextPath();
    		boolean expectedPort = 
    			(req.getScheme().equalsIgnoreCase("http") &&
    					req.getServerPort() == HtmlConsts.WEB_PORT) ||
    	    	(req.getScheme().equalsIgnoreCase("https") &&
    	    				req.getServerPort() == HtmlConsts.SECURE_WEB_PORT);
    		if (!expectedPort) {
    	    	serverUrl = req.getServerName() + BasicConsts.COLON + 
    	    		Integer.toString(req.getServerPort()) + path;
    	    } else {
    	    	serverUrl = req.getServerName() + path;
    	    }
    	    webApplicationBase = path;
    		this.datastore = (Datastore) getBean(BeanDefs.DATASTORE_BEAN);
    		this.userService = (UserService) getBean(BeanDefs.USER_BEAN);
    	}
    	
    	public Object getBean(String beanName) {
			return WebApplicationContextUtils.getRequiredWebApplicationContext(ctxt).getBean(beanName);
    	}
    	
    	public Datastore getDatastore() {
    		return datastore;
    	}
    	
    	public UserService getUserService() {
    		return userService;
    	}
    	
    	public String getWebApplicationURL() {
    		return webApplicationBase + BasicConsts.FORWARDSLASH;
    	}
    	
    	public String getWebApplicationURL(String servletAddr) {
    		return webApplicationBase + BasicConsts.FORWARDSLASH + servletAddr;
    	}
    	
    	public String getServerURL() {
    		return serverUrl;
    	}
    	
    	public void setAsDaemon(boolean asDaemon ) {
    		this.asDaemon = asDaemon;
    	}
    	
    	public boolean getAsDeamon() {
    		return asDaemon;
    	}
    	
    	public User getCurrentUser() {
    		return asDaemon ? userService.getDaemonAccountUser() : userService.getCurrentUser();
    	}
    }

    /**
     * Private constructor 
     */
    private ContextFactory() {}
    
    public static CallingContext getCallingContext(HttpServlet servlet, HttpServletRequest req) {
    	return new CallingContextImpl(servlet, req);
    }
}
