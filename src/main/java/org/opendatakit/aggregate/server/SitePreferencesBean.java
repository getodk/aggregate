/*
 * Copyright (C) 2013 University of Washington
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
package org.opendatakit.aggregate.server;

import javax.servlet.ServletContext;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.CallingContext;

/**
 * Simple class to retrieve the site key from ServerPreferencesProperties during
 * start-up.
 *
 * @author mitchellsundt@gmail.com
 */
@SuppressWarnings("unused")
public class SitePreferencesBean {

  private Datastore datastore;
  private UserService userService;
  private String siteKey = null;

  private CallingContext ccHack = new CallingContext() {
    @Override
    public Object getBean(String beanName) {
      throw new IllegalStateException("unexpected call");
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
      throw new IllegalStateException("unexpected call");
    }

    @Override
    public boolean getAsDeamon() {
      return true;
    }

    @Override
    public User getCurrentUser() {
      return userService.getDaemonAccountUser();
    }

    @Override
    public ServletContext getServletContext() {
      throw new IllegalStateException("unexpected call");
    }

    @Override
    public String getWebApplicationURL() {
      throw new IllegalStateException("unexpected call");
    }

    @Override
    public String getWebApplicationURL(String servletAddr) {
      throw new IllegalStateException("unexpected call");
    }

    @Override
    public String getServerURL() {
      throw new IllegalStateException("unexpected call");
    }

    @Override
    public String getSecureServerURL() {
      throw new IllegalStateException("unexpected call");
    }
  };

  SitePreferencesBean() {
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

  @SuppressWarnings("unused")
  public synchronized String getSiteKey() throws ODKEntityNotFoundException, ODKOverQuotaException {
    if (siteKey == null) {
      siteKey = ServerPreferencesProperties.getSiteKey(ccHack);
    }
    return siteKey;
  }
}
