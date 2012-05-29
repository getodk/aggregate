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

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Server Context creates a singleton for application context to prevent
 * unnecessary construction
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ContextFactory {

  /**
   * Singleton of the application context
   */
  // TODO: write a CallingContextImpl that uses a standalone applicationContext
  // for unit testing.
  //
  // private static final String APP_CONTEXT_PATH = "odk-settings.xml";
  // private static final ApplicationContext applicationContext = new
  // ClassPathXmlApplicationContext(APP_CONTEXT_PATH);

  public static final class CallingContextImpl implements CallingContext {
    final String serverUrl;
    final String secureServerUrl;
    final String webApplicationBase;
    final ServletContext ctxt;
    final Datastore datastore;
    final UserService userService;
    boolean asDaemon = false;

    CallingContextImpl(ServletContext ctxt, HttpServletRequest req) {
      // for now, only store the servlet context and the serverUrl
      this.ctxt = ctxt;
      String path = ctxt.getContextPath();
      this.datastore = (Datastore) getBean(BeanDefs.DATASTORE_BEAN);
      this.userService = (UserService) getBean(BeanDefs.USER_BEAN);

      Realm realm = userService.getCurrentRealm();
      Integer identifiedPort = realm.getPort();
      Integer identifiedSecurePort = realm.getSecurePort();
      String identifiedHostname = realm.getHostname();

      if (identifiedHostname == null || identifiedHostname.length() == 0) {
        identifiedHostname = req.getServerName();
        if (identifiedHostname == null || identifiedHostname.length() == 0
            || identifiedHostname.equals("0.0.0.0")) {
          try {
            identifiedHostname = InetAddress.getLocalHost().getCanonicalHostName();
          } catch (UnknownHostException e) {
            identifiedHostname = "127.0.0.1";
          }
        }
      }

      String identifiedScheme = "http";
      if (realm.isSslRequired()) {
        identifiedScheme = "https";
        identifiedPort = identifiedSecurePort;
      }

      if (identifiedPort == null || identifiedPort == 0) {
        if (req.getScheme().equals(identifiedScheme)) {
          identifiedPort = req.getServerPort();
        } else if (realm.isSslRequired()) {
          identifiedPort = HtmlConsts.SECURE_WEB_PORT;
        } else {
          identifiedPort = HtmlConsts.WEB_PORT;
        }
      }

      boolean expectedPort = (identifiedScheme.equalsIgnoreCase("http") && identifiedPort == HtmlConsts.WEB_PORT)
          || (identifiedScheme.equalsIgnoreCase("https") && identifiedPort == HtmlConsts.SECURE_WEB_PORT);

      if (!expectedPort) {
        serverUrl = identifiedScheme + "://" + identifiedHostname + BasicConsts.COLON
            + Integer.toString(identifiedPort) + path;
      } else {
        serverUrl = identifiedScheme + "://" + identifiedHostname + path;
      }

      if (realm.isSslRequired() || !realm.isSslAvailable()) {
        secureServerUrl = serverUrl;
      } else {
        if (identifiedSecurePort != null && identifiedSecurePort != 0
            && identifiedSecurePort != HtmlConsts.SECURE_WEB_PORT) {
          // explicitly name the port
          secureServerUrl = "https://" + identifiedHostname + BasicConsts.COLON
              + Integer.toString(identifiedSecurePort) + path;
        } else {
          // assume it is the default https port...
          secureServerUrl = "https://" + identifiedHostname + path;
        }
      }
      webApplicationBase = path;
    }

    CallingContextImpl(CallingContext context) {
      this.serverUrl = context.getServerURL();
      this.secureServerUrl = context.getSecureServerURL();
      this.webApplicationBase = context.getWebApplicationURL();
      this.ctxt = context.getServletContext();
      this.datastore = context.getDatastore();
      this.userService = context.getUserService();
      this.asDaemon = context.getAsDeamon();
    }

    @Override
    public Object getBean(String beanName) {
      return WebApplicationContextUtils.getRequiredWebApplicationContext(ctxt).getBean(beanName);
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
      return ctxt;
    }

    @Override
    public String getWebApplicationURL() {
      return webApplicationBase;
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
    public void setAsDaemon(boolean asDaemon) {
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
  private ContextFactory() {
  }

  public static CallingContext getCallingContext(HttpServlet servlet, HttpServletRequest req) {
    return new CallingContextImpl(servlet.getServletContext(), req);
  }

  public static CallingContext getCallingContext(ServletContext sc, HttpServletRequest req) {
    return new CallingContextImpl(sc, req);
  }

  public static CallingContext duplicateContext(CallingContext context) {
    return new CallingContextImpl(context);
  }

}
