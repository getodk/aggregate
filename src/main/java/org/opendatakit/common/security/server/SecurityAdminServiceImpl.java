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

package org.opendatakit.common.security.server;

import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.web.CallingContext;

/**
 * GWT server request handler for the SecurityAdminService interface.
 *
 * @author mitchellsundt@gmail.com
 */
public class SecurityAdminServiceImpl extends XsrfProtectedServiceServlet implements
    org.opendatakit.common.security.client.security.admin.SecurityAdminService {

  /**
   *
   */
  private static final long serialVersionUID = -5028277386314314356L;

  @Override
  public ArrayList<UserSecurityInfo> getAllUsers(boolean withAuthorities) throws DatastoreFailureException {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    return SecurityServiceUtil.getAllUsers(withAuthorities, cc);
  }

  @Override
  public void setUsersAndGrantedAuthorities(String xsrfString,
                                            ArrayList<UserSecurityInfo> users,
                                            ArrayList<GrantedAuthorityName> allGroups)
      throws AccessDeniedException, DatastoreFailureException {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    if (!req.getSession().getId().equals(xsrfString)) {
      throw new AccessDeniedException("Invalid request");
    }

    SecurityServiceUtil.setStandardSiteAccessConfiguration(users, allGroups, cc);
    // clear the cache of saved user identities as we don't know what has changed...
    cc.getUserService().reloadPermissions();
  }
}
