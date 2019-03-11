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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import javax.servlet.http.HttpServletRequest;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.servlet.UserManagePasswordsServlet;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.RealmSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

/**
 * GWT Server implementation for the SecurityService interface. This provides
 * privileges context to the client and is therefore accessible to anyone with a
 * ROLE_USER privilege.
 *
 * @author mitchellsundt@gmail.com
 */
public class SecurityServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.common.security.client.security.SecurityService {

  /**
   *
   */
  private static final long serialVersionUID = -7360632450727200941L;

  @Override
  public UserSecurityInfo getUserInfo() throws DatastoreFailureException {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    String uriUser = user.getUriUser();
    UserSecurityInfo info;
    try {
      if (user.isRegistered()) {
        RegisteredUsersTable t;
        t = RegisteredUsersTable.getUserByUri(uriUser, ds, user);
        if (t != null) {
          info = new UserSecurityInfo(t.getUsername(), t.getFullName(), t.getEmail(),
              UserSecurityInfo.UserType.REGISTERED);
          SecurityServiceUtil.setAuthenticationLists(info, t.getUri(), cc);
        } else {
          throw new DatastoreFailureException("Unable to retrieve user record");
        }
      } else if (user.isAnonymous()) {
        info = new UserSecurityInfo(User.ANONYMOUS_USER, User.ANONYMOUS_USER_NICKNAME, null,
            UserSecurityInfo.UserType.ANONYMOUS);
        SecurityServiceUtil.setAuthenticationListsForSpecialUser(info,
            GrantedAuthorityName.USER_IS_ANONYMOUS, cc);
      } else {
        // should never get to this case via interactive actions...
        throw new DatastoreFailureException("Internal error: 45443");
      }
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
    return info;
  }

  @Override
  public RealmSecurityInfo getRealmInfo(String xsrfString) throws AccessDeniedException, DatastoreFailureException {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    if (!req.getSession().getId().equals(xsrfString)) {
      throw new AccessDeniedException("Invalid request");
    }

    RealmSecurityInfo r = new RealmSecurityInfo();
    r.setRealmString(cc.getUserService().getCurrentRealm().getRealmString());
    MessageDigestPasswordEncoder mde = (MessageDigestPasswordEncoder) cc
        .getBean(SecurityBeanDefs.BASIC_AUTH_PASSWORD_ENCODER);
    r.setBasicAuthHashEncoding(mde.getAlgorithm());
    r.setSuperUserEmail(cc.getUserService().getSuperUserEmail());
    r.setSuperUsername(cc.getUserService().getSuperUserUsername());
    try {
      r.setSuperUsernamePasswordSet(cc.getUserService().isSuperUsernamePasswordSet(cc));
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException("Unable to access datastore");
    }
    // User interface layer uses this URL to submit password changes securely
    r.setChangeUserPasswordURL(cc.getWebApplicationURL(UserManagePasswordsServlet.ADDR));
    return r;
  }
}
