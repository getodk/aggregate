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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
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

	private static final GrantedAuthority siteAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_SITE_ADMINS);
	private static final GrantedAuthority formAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_FORM_ADMINS);
	private static final GrantedAuthority submitterAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_SUBMITTERS);
	private static final GrantedAuthority anonAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_ANONYMOUS.name());

	@Override
	public ArrayList<UserSecurityInfo> getAllUsers(boolean withAuthorities ) throws AccessDeniedException, DatastoreFailureException {

	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

	    ArrayList<UserSecurityInfo> users = new ArrayList<UserSecurityInfo>();
	    try {
			Query q = RegisteredUsersTable.createQuery(cc.getDatastore(), cc.getCurrentUser());
			RegisteredUsersTable.applyNaturalOrdering(q);
			
			List<? extends CommonFieldsBase> l = q.executeQuery(0);
			
			for ( CommonFieldsBase cb : l ) {
				RegisteredUsersTable t = (RegisteredUsersTable) cb;
				UserSecurityInfo i = new UserSecurityInfo(t.getUsername(), t.getNickname(), t.getEmail(), 
															UserSecurityInfo.UserType.REGISTERED);
				if ( withAuthorities ) {
					SecurityServiceUtil.setAuthenticationLists(i, t.getUri(), cc);
				}
				users.add(i);
			}
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		}
		// the natural ordering (above) produces a sorted list...
		return users;
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
	public UserSecurityInfo getAnonymousUser() throws AccessDeniedException,
			DatastoreFailureException {

	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

		UserSecurityInfo i = new UserSecurityInfo(User.ANONYMOUS_USER, User.ANONYMOUS_USER_NICKNAME,
									null, UserSecurityInfo.UserType.ANONYMOUS );
		SecurityServiceUtil.setAuthenticationListsForSpecialUser(i, GrantedAuthorityNames.USER_IS_ANONYMOUS, cc);
		return i;
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

	/**
	 * Given a collection of users, ensure that each user is a registered user 
	 * (creating a registered user if one doesn't exist).
	 * </p>
	 * <p>The collection is assumed to be exhaustive.  Users not in the list will
	 * be deleted.</p>
	 * 
	 * @param users
	 * @param cc
	 * @return map of users to their Uri strings
	 * @throws DatastoreFailureException
	 * @throws AccessDeniedException 
	 */
	private Map<UserSecurityInfo, String> setUsers( ArrayList<UserSecurityInfo> users, CallingContext cc) 
					throws DatastoreFailureException, AccessDeniedException {
		List<UserSecurityInfo> allUsersList = getAllUsers(false);
		
		Set<UserSecurityInfo> removedUsers = new TreeSet<UserSecurityInfo>();
		removedUsers.addAll(allUsersList);
		removedUsers.removeAll(users);
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();

		Map<UserSecurityInfo, String> pkMap = new HashMap<UserSecurityInfo, String>();
		try {
			// mark absent users as removed...
			for ( UserSecurityInfo u : removedUsers ) {
				RegisteredUsersTable t = 
					RegisteredUsersTable.getUniqueUserByUsername(u.getUsername(), ds, user);
				if ( t != null ) {
					t.setIsRemoved(true);
					ds.putEntity(t, user);
				}
			}
			// go through all other users.  Assert that they exist.
			// This will update the fields to match those specified.
			for ( UserSecurityInfo u : users ) {
				RegisteredUsersTable t = RegisteredUsersTable.assertActiveUserByUserSecurityInfo(u, cc);
				pkMap.put(u, t.getUri());
			}
		} catch ( ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException("Incomplete security update");
		}
		return pkMap;
	}
	
	/**
	 * Given a collection of users, ensure that each user is a registered user 
	 * (creating a registered user if one doesn't exist) and assign
	 * those users to the granted authority.  
	 * <p>The collection is assumed to be exhaustive.  If there are other e-mails
	 * already assigned to the granted authority, they will be removed so that 
	 * exactly the passed-in set of users are assigned to the authority, no more, 
	 * no less.</p>
	 * 
	 * @param users
	 * @param auth
	 * @param cc
	 * @throws DatastoreFailureException 
	 */
	private void setUsersOfGrantedAuthority( Map<UserSecurityInfo, String> pkMap, 
								GrantedAuthority auth, CallingContext cc) throws DatastoreFailureException {
		GrantedAuthorityInfo g = new GrantedAuthorityInfo(auth.getAuthority());
		// build the set of uriUsers for this granted authority...
		TreeSet<String> desiredMembers = new TreeSet<String>();
		
		for ( Map.Entry<UserSecurityInfo, String> u : pkMap.entrySet() ) {
			UserSecurityInfo info = u.getKey();
			String uriUser = u.getValue();

			if ( info.getAssignedUserGroups().contains(g) ) {
				desiredMembers.add(uriUser);
			}
		}
	
		// assert that the authority has exactly this set of uriUsers (no more, no less)
		try {
			UserGrantedAuthority.assertGrantedAuthorityMembers(auth, desiredMembers, cc);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException("Incomplete security update");
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

		List<String> anonGrantStrings = new ArrayList<String>();
		for ( GrantedAuthorityInfo a : anonGrants ) {
			anonGrantStrings.add(a.getName());
		}

		try {
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(siteAuth, SecurityServiceUtil.siteGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(formAuth, SecurityServiceUtil.formGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(submitterAuth, SecurityServiceUtil.submitterGrants, cc);

			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(anonAuth, anonGrantStrings, cc);
			
			TreeSet<String> authorities = GrantedAuthorityHierarchyTable.getAllPermissionsAssignableGrantedAuthorities(cc.getDatastore(), cc.getCurrentUser());
			authorities.remove(siteAuth.getAuthority());
			authorities.remove(formAuth.getAuthority());
			authorities.remove(submitterAuth.getAuthority());
			authorities.remove(anonAuth.getAuthority());
			
			// remove anything else from database...
			List<String> empty = Collections.emptyList();
			for ( String s : authorities ) {
				GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(new GrantedAuthorityImpl(s), empty, cc );
			}
			
			Map<UserSecurityInfo, String> pkMap = setUsers(users, cc);
			setUsersOfGrantedAuthority(pkMap, siteAuth, cc);
			setUsersOfGrantedAuthority(pkMap, formAuth, cc);
			setUsersOfGrantedAuthority(pkMap, submitterAuth, cc);

		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException("Incomplete update");
		}
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

}
