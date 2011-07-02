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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserClassSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * GWT server request handler for the SecurityAdminService interface.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class SecurityAdminServiceImpl extends RemoteServiceServlet implements
org.opendatakit.common.security.client.security.admin.SecurityAdminService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5028277386314314356L;

	@Override
	public ArrayList<UserSecurityInfo> getAllUsers(boolean withAuthorities ) throws AccessDeniedException, DatastoreFailureException {

	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

	    return SecurityServiceUtil.getAllUsers(withAuthorities, cc);
	}

	@Override
	public ArrayList<UserSecurityInfo> getUsers(GrantedAuthorityInfo auth)
			throws AccessDeniedException, DatastoreFailureException {

	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

	    ArrayList<UserSecurityInfo> users = new ArrayList<UserSecurityInfo>();
	    try {
		    Set<String> uriUsers = 
		    	UserGrantedAuthority.getUriUsers(new GrantedAuthorityImpl(auth.getName()),
		    			cc.getDatastore(), cc.getCurrentUser());

		    Query q = RegisteredUsersTable.createQuery(cc.getDatastore(), cc.getCurrentUser());
			RegisteredUsersTable.applyNaturalOrdering(q);
			
			List<? extends CommonFieldsBase> l = q.executeQuery(0);
			
			for ( CommonFieldsBase cb : l ) {
				RegisteredUsersTable t = (RegisteredUsersTable) cb;
				if ( uriUsers.contains(t.getUri()) ) {
					UserSecurityInfo i = new UserSecurityInfo(t.getUsername(), t.getNickname(), t.getEmail(),
											UserSecurityInfo.UserType.REGISTERED);
					SecurityServiceUtil.setAuthenticationLists(i, t.getUri(), cc);
					users.add(i);
				}
			}
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		}
		// the natural ordering (above) produces a sorted list...
		return users;
	}

	@Override
	public boolean isSimpleConfig() throws AccessDeniedException,
			DatastoreFailureException {

	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

	    try {
			return SecurityServiceUtil.isSimpleConfig(cc);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		}
	}
	
	@Override
	public void setUsersAndGrantedAuthorities( String xsrfString, 
							ArrayList<UserSecurityInfo> users,  
							ArrayList<GrantedAuthorityInfo> anonGrants, 
							ArrayList<GrantedAuthorityInfo> allGroups)
			throws AccessDeniedException, DatastoreFailureException {

	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

	    if ( !req.getSession().getId().equals(xsrfString) ) {
			throw new AccessDeniedException("Invalid request");
		}

	    SecurityServiceUtil.setStandardSiteAccessConfiguration( users, anonGrants, allGroups, cc ); 
	    // clear the cache of saved user identities as we don't know what has changed...
	    cc.getUserService().reloadPermissions();
	}

	@Override
	public void setUserPasswords(String xsrfString, ArrayList<CredentialsInfo> credentials)
			throws AccessDeniedException, DatastoreFailureException {

	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

	    if ( !req.getSession().getId().equals(xsrfString) ) {
			throw new AccessDeniedException("Invalid request");
		}

	    Datastore ds = cc.getDatastore();
	    User user = cc.getUserService().getCurrentUser();
		RegisteredUsersTable userDefinition = null;
		try {
			for ( CredentialsInfo credential : credentials ) {
				userDefinition = RegisteredUsersTable.getUniqueUserByUsername(credential.getUsername(), ds, user);
				if ( userDefinition == null ) {
					throw new AccessDeniedException("User is not a registered user.");
				}
	
				userDefinition.setDigestAuthPassword(credential.getDigestAuthHash());
				userDefinition.setBasicAuthPassword(credential.getBasicAuthHash());
				userDefinition.setBasicAuthSalt(credential.getBasicAuthSalt());
				ds.putEntity(userDefinition, user);
			}
		} catch ( ODKDatastoreException e ) {
			e.printStackTrace();
			throw new DatastoreFailureException(e.getMessage());
		}
	}

	@Override
	public UserClassSecurityInfo getUserClassPrivileges(String userClassName)
			throws AccessDeniedException, DatastoreFailureException {

	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

	    UserClassSecurityInfo i = new UserClassSecurityInfo(userClassName);
	    
		SecurityServiceUtil.setAuthenticationListsForUserClass(i, cc);
		return i;
	}

}
