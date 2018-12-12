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
  public Object getBean(String beanName);

  public Datastore getDatastore();

  public UserService getUserService();

  public void setAsDaemon(boolean asDaemon);

  public boolean getAsDeamon();

  public User getCurrentUser();

  public ServletContext getServletContext();

  public String getWebApplicationURL();

  public String getWebApplicationURL(String servletAddr);

  public String getServerURL();

  public String getSecureServerURL();
}