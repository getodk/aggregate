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

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Server Context creates a singleton for application context to prevent unnecessary construction
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ContextFactory {
  
    private static final String APP_CONTEXT_PATH = "odk-settings.xml";
  
    /**
     * Singleton of the application context
     */
    private static final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(APP_CONTEXT_PATH);

    public static final class CallingContextImpl implements CallingContext {
    	final ServletContext ctxt;
    	final Datastore datastore;
    	final UserService userService;
    	boolean asDaemon = false;
    	
    	CallingContextImpl(ServletContext ctxt) {
    		this.ctxt = ctxt;
    		this.datastore = (Datastore) getBean(BeanDefs.DATASTORE_BEAN);
    		this.userService = (UserService) getBean(BeanDefs.USER_BEAN);
    	}
    	
    	public Object getBean(String beanName) {
    		if ( ctxt == null ) {
    			return applicationContext.getBean(beanName);
    		} else {
    			return WebApplicationContextUtils.getRequiredWebApplicationContext(ctxt).getBean(beanName);
    		}
    	}
    	
    	public Datastore getDatastore() {
    		return datastore;
    	}
    	
    	public UserService getUserService() {
    		return userService;
    	}
    	
    	public void setAsDaemon(boolean asDaemon ) {
    		this.asDaemon = true;
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
    
    /**
     * For unit testing only...
     * 
     * @param beanName
     * @return
     */
    public static Object getbean(String beanName) {
    	return applicationContext.getBean(beanName);
    }
    
    public static CallingContext getCallingContext(ServletContext ctxt) {
    	return new CallingContextImpl(ctxt);
    }
}
