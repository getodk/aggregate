package org.opendatakit.common.web;

import javax.servlet.ServletContext;

import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestContextFactory {

	  public static final String USER_BEAN = "user_service";
	  public static final String DATASTORE_BEAN = "datastore";
  
	    /**
	     * Singleton of the application context
	     */
	    private static final String APP_CONTEXT_PATH = "odk-settings.xml";
	    private static final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(APP_CONTEXT_PATH);

	    public static final class CallingContextImpl implements CallingContext {
	    	final String serverUrl;
	    	final String secureServerUrl;
	    	final String webApplicationBase;
	    	final Datastore datastore;
	    	final UserService userService;
	    	boolean asDaemon = true; // otherwise there isn't a current user...

	    	CallingContextImpl() {
	    		webApplicationBase = "/test/stub";
	    		serverUrl = "http://test.org" + webApplicationBase;
	    		secureServerUrl = "https://test.org" + webApplicationBase;
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
