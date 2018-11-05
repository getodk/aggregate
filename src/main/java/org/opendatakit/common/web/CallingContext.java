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
package org.opendatakit.common.web;

import javax.servlet.ServletContext;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Context in which the call occurs.
 * The standard implementation is in ContextFactory.
 * An alternative implementation is provided for the Tomcat watchdog executor.
 * An alternative implementation should be provided for test apparatus.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface CallingContext {
  /**
   * Retrieve the bean with the given name.
   *
   * @param beanName
   * @return the bean or an exception
   */
  public Object getBean(String beanName);

  /**
   * @return the datastore
   */
  public Datastore getDatastore();

  /**
   * @return the user identity service
   */
  public UserService getUserService();

  /**
   * Set whether or not we should act as the daemon user.
   * Effectively a run-as feature, but not necessarily
   * linked with security.
   *
   * @param asDaemon
   */
  public void setAsDaemon(boolean asDaemon);

  /**
   * @return whether or not we are acting as the daemon user.
   */
  public boolean getAsDeamon();

  /**
   * @return the logged-in user, anonymous user, or the daemon user.
   */
  public User getCurrentUser();

  /**
   * @return the servlet context
   */
  public ServletContext getServletContext();

  /**
   * @return the slash-rooted path of this web application
   */
  public String getWebApplicationURL();

  /**
   * Use this to form the URLs for pages within this web application.
   *
   * @param servletAddr -- the root-relative path of a servlet
   * @return the slash-rooted path for the servlet within this web application
   */
  public String getWebApplicationURL(String servletAddr);

  /**
   * Return the base of a URL for this server.
   * These are of the form: "http://localhost:8080/webApp"
   * Or, if ssl is required, "https://localhost:8443/webApp"
   *
   * @return the serverURL useful for external links.
   */
  public String getServerURL();

  /**
   * Return the base of a secure URL for this server.
   * If ssl is available, "https://localhost:8443/webApp"
   * Or, if not, "http://localhost:8080/webApp"
   *
   * @return the serverURL useful for external links.
   */
  public String getSecureServerURL();
}