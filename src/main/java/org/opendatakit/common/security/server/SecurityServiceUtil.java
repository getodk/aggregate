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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserClassSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

/**
 * Common utility methods extracted from the AccessConfigurationServlet so they
 * can be shared between the servlet and GWT server classes.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class SecurityServiceUtil {

	private static final Set<String> specialNames = new HashSet<String>();

	public static final GrantedAuthority siteAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_SITE_ADMINS);
	public static final GrantedAuthority formAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_DATA_ADMINS);
	public static final GrantedAuthority dataViewerAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_DATA_VIEWERS);
	public static final GrantedAuthority submitterAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_DATA_COLLECTORS);
	public static final GrantedAuthority authenticatedAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_AUTHENTICATED.name());
	public static final GrantedAuthority anonAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_ANONYMOUS.name());

	public static final List<String> siteGrants;
	public static final List<String> formGrants;
	public static final List<String> dataViewerGrants;
	public static final List<String> submitterGrants;

	public static final List<String> anonSubmitterGrants;
	public static final List<String> anonAttachmentViewerGrants;
	
	static {
		List<String> isiteGrants = new ArrayList<String>();
		isiteGrants.add(GrantedAuthorityNames.ROLE_ACCESS_ADMIN.name());
		isiteGrants.add(GrantedAuthorityNames.GROUP_DATA_ADMINS);
		isiteGrants.add(GrantedAuthorityNames.GROUP_DATA_VIEWERS);
		isiteGrants.add(GrantedAuthorityNames.GROUP_DATA_COLLECTORS);
		siteGrants = Collections.unmodifiableList(isiteGrants);
	
		List<String> iformGrants = new ArrayList<String>();
		iformGrants.add(GrantedAuthorityNames.ROLE_FORM_ADMIN.name());
		iformGrants.add(GrantedAuthorityNames.ROLE_SERVICES_ADMIN.name());
		formGrants = Collections.unmodifiableList(iformGrants);
	
		List<String> idataViewerGrants = new ArrayList<String>();
		idataViewerGrants.add(GrantedAuthorityNames.ROLE_ANALYST.name());
		idataViewerGrants.add(GrantedAuthorityNames.ROLE_ATTACHMENT_VIEWER.name());
		idataViewerGrants.add(GrantedAuthorityNames.ROLE_USER.name());
		dataViewerGrants = Collections.unmodifiableList(idataViewerGrants);
		
		List<String> isubmitterGrants = new ArrayList<String>();
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_DOWNLOAD.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_LIST.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_SUBMISSION_UPLOAD.name());
		submitterGrants = Collections.unmodifiableList(isubmitterGrants);

		// todo: handle anon as a 'user'
		List<String> ianonSubmitterGrants = new ArrayList<String>();
		ianonSubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_DOWNLOAD.name());
		ianonSubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_LIST.name());
		ianonSubmitterGrants.add(GrantedAuthorityNames.ROLE_SUBMISSION_UPLOAD.name());
		anonSubmitterGrants = Collections.unmodifiableList(ianonSubmitterGrants);

		List<String> ianonAttachmentViewerGrants = new ArrayList<String>();
		ianonAttachmentViewerGrants.add(GrantedAuthorityNames.ROLE_ATTACHMENT_VIEWER.name());
		anonAttachmentViewerGrants = Collections.unmodifiableList(ianonAttachmentViewerGrants);
	}

	/**
	 * Determine whether or not the configuration is a full or partially constructed
	 * simple configuration.  If it has additional elements, we show the click-through
	 * to custom management screen.  Otherwise, show the wizard screen.
	 * 
	 * @param cc
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static boolean isSimpleConfig( CallingContext cc ) throws ODKDatastoreException {
		TreeMap<String, TreeSet<String>> hierarchy = 
			GrantedAuthorityHierarchyTable.getEntireGrantedAuthorityHierarchy(cc.getDatastore(), cc.getCurrentUser());
		
		// check that a subset of the expected set of fields are there...
		for ( Map.Entry<String, TreeSet<String>> e : hierarchy.entrySet() ) {
			if ( e.getKey().equals(GrantedAuthorityNames.GROUP_SITE_ADMINS) ) {
				for ( String s : e.getValue() ) {
					if ( siteGrants.contains(s) ) continue;
					return false; 
				}
			} else if ( e.getKey().equals(GrantedAuthorityNames.GROUP_DATA_ADMINS) ) {
				for ( String s : e.getValue() ) {
					if ( formGrants.contains(s) ) continue;
					return false; 
				}
			} else if ( e.getKey().equals(GrantedAuthorityNames.GROUP_DATA_VIEWERS) ) {
				for ( String s : e.getValue() ) {
					if ( dataViewerGrants.contains(s) ) continue;
					return false; 
				}
			} else if ( e.getKey().equals(GrantedAuthorityNames.GROUP_DATA_COLLECTORS) ) {
				for ( String s : e.getValue() ) {
					if ( submitterGrants.contains(s) ) continue;
					return false; 
				}
			} else if ( e.getKey().equals(GrantedAuthorityNames.USER_IS_ANONYMOUS.name())) {
				for ( String s : e.getValue() ) {
					if ( anonSubmitterGrants.contains(s) ||
							anonAttachmentViewerGrants.contains(s) ) continue;
					return false; 
				}
			} else {
				// some other name -- must be a custom set-up...
				return false;
			}
		}
		return true;
	}

	public static UserSecurityInfo getSuperUser(CallingContext cc) throws DatastoreFailureException {
	    try {
	    	RegisteredUsersTable t = RegisteredUsersTable.assertSuperUser(cc);
			UserSecurityInfo i = new UserSecurityInfo(t.getUsername(), t.getNickname(), t.getEmail(), 
														UserSecurityInfo.UserType.REGISTERED);
			return i;
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		}
	}
	
	public static ArrayList<UserSecurityInfo> getAllUsers(boolean withAuthorities, CallingContext cc ) throws AccessDeniedException, DatastoreFailureException {

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

	static void setAuthenticationLists(UserSecurityInfo userInfo, String uriUser, CallingContext cc) throws ODKDatastoreException {
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		RoleHierarchy hierarchy = (RoleHierarchy) cc.getBean("hierarchicalRoleRelationships");
		Set<GrantedAuthority> grants = UserGrantedAuthority.getGrantedAuthorities(uriUser, ds, user);
		TreeSet<GrantedAuthorityInfo> groups = new TreeSet<GrantedAuthorityInfo>();
		TreeSet<GrantedAuthorityInfo> authorities = new TreeSet<GrantedAuthorityInfo>();
		for ( GrantedAuthority grant : grants ) {
			if ( GrantedAuthorityNames.permissionsCanBeAssigned(grant.getAuthority()) ) {
				groups.add( new GrantedAuthorityInfo(grant.getAuthority()));
			} else {
				authorities.add( new GrantedAuthorityInfo(grant.getAuthority()));
			}
		}
		Collection<GrantedAuthority> auths = hierarchy.getReachableGrantedAuthorities(grants);
		for ( GrantedAuthority auth : auths ) {
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(auth.getAuthority()) ) {
				authorities.add( new GrantedAuthorityInfo(auth.getAuthority()));
			}
		}
		userInfo.setAssignedUserGroups(groups);
		userInfo.setGrantedAuthorities(authorities);
	}

	static void setAuthenticationListsFromDirectAuthorities(UserSecurityInfo userInfo, Collection<GrantedAuthority> grants, CallingContext cc) throws ODKDatastoreException {
		RoleHierarchy hierarchy = (RoleHierarchy) cc.getBean("hierarchicalRoleRelationships");
		TreeSet<GrantedAuthorityInfo> groups = new TreeSet<GrantedAuthorityInfo>();
		TreeSet<GrantedAuthorityInfo> authorities = new TreeSet<GrantedAuthorityInfo>();
		for ( GrantedAuthority grant : grants ) {
			if ( GrantedAuthorityNames.permissionsCanBeAssigned(grant.getAuthority()) ) {
				groups.add( new GrantedAuthorityInfo(grant.getAuthority()));
			} else {
				authorities.add( new GrantedAuthorityInfo(grant.getAuthority()));
			}
		}
		Collection<GrantedAuthority> auths = hierarchy.getReachableGrantedAuthorities(grants);
		for ( GrantedAuthority auth : auths ) {
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(auth.getAuthority()) ) {
				authorities.add( new GrantedAuthorityInfo(auth.getAuthority()));
			}
		}
		userInfo.setAssignedUserGroups(groups);
		userInfo.setGrantedAuthorities(authorities);
	}

	static void setAuthenticationListsForSpecialUser(UserSecurityInfo userInfo, GrantedAuthorityNames name, CallingContext cc) {
		RoleHierarchy hierarchy = (RoleHierarchy) cc.getBean("hierarchicalRoleRelationships");
		TreeSet<GrantedAuthorityInfo> groups = new TreeSet<GrantedAuthorityInfo>();
		groups.add(new GrantedAuthorityInfo(name.toString()));
		TreeSet<GrantedAuthorityInfo> authorities = new TreeSet<GrantedAuthorityInfo>();
		ArrayList<GrantedAuthority> grants = new ArrayList<GrantedAuthority>();
		grants.add(new GrantedAuthorityImpl(name.toString()));
		Collection<GrantedAuthority> auths = hierarchy.getReachableGrantedAuthorities(grants);
		for ( GrantedAuthority auth : auths ) {
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(auth.getAuthority()) ) {
				authorities.add( new GrantedAuthorityInfo(auth.getAuthority()));
			}
		}
		userInfo.setAssignedUserGroups(groups);
		userInfo.setGrantedAuthorities(authorities);
	}
	
	static void setAuthenticationListsForUserClass(UserClassSecurityInfo userClassInfo, CallingContext cc)
					throws DatastoreFailureException {
		RoleHierarchy hierarchy = (RoleHierarchy) cc.getBean("hierarchicalRoleRelationships");
		GrantedAuthority auth = new GrantedAuthorityImpl(userClassInfo.getUserClassName());
		// get the directly granted authorities
		TreeSet<String> grants;
		try {
			grants = GrantedAuthorityHierarchyTable.getSubordinateGrantedAuthorities(auth, cc);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException("Unable to fetch subordinate authorities");
		}
		
		List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
		{
			TreeSet<GrantedAuthorityInfo> groups = new TreeSet<GrantedAuthorityInfo>();
			TreeSet<GrantedAuthorityInfo> roles = new TreeSet<GrantedAuthorityInfo>();
			for ( String g : grants ) {
				grantedAuths.add(new GrantedAuthorityImpl(g));
				if ( GrantedAuthorityNames.permissionsCanBeAssigned(g) ) {
					groups.add(new GrantedAuthorityInfo(g));
				} else {
					roles.add( new GrantedAuthorityInfo(g));
				}
			}
			userClassInfo.setAssignedUserGroups(groups);
			userClassInfo.setAssignedGrantedAuthorities(roles);
		}
		
		TreeSet<GrantedAuthorityInfo> authorities = new TreeSet<GrantedAuthorityInfo>();
		Collection<GrantedAuthority> auths = hierarchy.getReachableGrantedAuthorities(grantedAuths);
		for ( GrantedAuthority a : auths ) {
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(a.getAuthority()) ) {
				authorities.add( new GrantedAuthorityInfo(a.getAuthority()));
			}
		}
		userClassInfo.setGrantedAuthorities(authorities);
	}
	
	public static final synchronized boolean isSpecialName(String authority) {
		if ( SecurityServiceUtil.specialNames.isEmpty() ) {
			for ( GrantedAuthorityNames n : GrantedAuthorityNames.values() ) {
				SecurityServiceUtil.specialNames.add(n.name());
			}
		}
		
		return SecurityServiceUtil.specialNames.contains(authority) ||
				authority.startsWith(GrantedAuthorityNames.MAILTO_PREFIX) ||
				authority.startsWith(GrantedAuthorityNames.RUN_AS_PREFIX) ||
				authority.startsWith(GrantedAuthorityNames.ROLE_PREFIX);
	}

	/**
	 * Construct and return the Email object for the superUser.
	 * 
	 * @param cc
	 * @return
	 */
	public static final EmailParser.Email getSuperUserEmail( CallingContext cc ) {
		String suEmail = cc.getUserService().getSuperUserEmail();
		return new EmailParser.Email(suEmail.substring(SecurityUtils.MAILTO_COLON.length(), suEmail.indexOf(SecurityUtils.AT_SIGN)), suEmail);
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
	public static Map<UserSecurityInfo, String> setUsers( ArrayList<UserSecurityInfo> users, CallingContext cc) 
					throws DatastoreFailureException, AccessDeniedException {
		List<UserSecurityInfo> allUsersList = getAllUsers(false, cc);
		
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
	public static void setUsersOfGrantedAuthority( Map<UserSecurityInfo, String> pkMap, 
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

	/**
	 * Method to enforce an access configuration constraining only registered users,
	 * authenticated users and anonymous access. 
	 * @param users
	 * @param anonGrants
	 * @param allGroups
	 * @param cc
	 * @throws DatastoreFailureException
	 * @throws AccessDeniedException
	 */
	public static final void setStandardSiteAccessConfiguration( ArrayList<UserSecurityInfo> users,
			ArrayList<GrantedAuthorityInfo> authenticatedGrants,
			ArrayList<GrantedAuthorityInfo> anonGrants,
			ArrayList<GrantedAuthorityInfo> allGroups, CallingContext cc ) throws DatastoreFailureException, AccessDeniedException {
		
		List<String> authenticatedGrantStrings = new ArrayList<String>();
		for ( GrantedAuthorityInfo a : authenticatedGrants ) {
			authenticatedGrantStrings.add(a.getName());
		}
		
		List<String> anonGrantStrings = new ArrayList<String>();
		for ( GrantedAuthorityInfo a : anonGrants ) {
			anonGrantStrings.add(a.getName());
		}

		try {
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(siteAuth, SecurityServiceUtil.siteGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(formAuth, SecurityServiceUtil.formGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(dataViewerAuth, SecurityServiceUtil.dataViewerGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(submitterAuth, SecurityServiceUtil.submitterGrants, cc);
			
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(authenticatedAuth, anonGrantStrings, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(anonAuth, anonGrantStrings, cc);
			
			TreeSet<String> authorities = GrantedAuthorityHierarchyTable.getAllPermissionsAssignableGrantedAuthorities(cc.getDatastore(), cc.getCurrentUser());
			authorities.remove(siteAuth.getAuthority());
			authorities.remove(formAuth.getAuthority());
			authorities.remove(dataViewerAuth.getAuthority());
			authorities.remove(submitterAuth.getAuthority());
			authorities.remove(authenticatedAuth.getAuthority());
			authorities.remove(anonAuth.getAuthority());
			
			// remove anything else from database...
			List<String> empty = Collections.emptyList();
			for ( String s : authorities ) {
				GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(new GrantedAuthorityImpl(s), empty, cc );
			}
			
			Map<UserSecurityInfo, String> pkMap = setUsers(users, cc);
			setUsersOfGrantedAuthority(pkMap, siteAuth, cc);
			setUsersOfGrantedAuthority(pkMap, formAuth, cc);
			setUsersOfGrantedAuthority(pkMap, dataViewerAuth, cc);
			setUsersOfGrantedAuthority(pkMap, submitterAuth, cc);
			
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException("Incomplete update");
		}
	}

	/**
	 * Configures the server to have the default role names and role hierarchy.
	 * 
	 * @param cc
	 * @throws DatastoreFailureException
	 * @throws AccessDeniedException
	 */
	public static final void setDefaultRoleNamesAndHierarchy( 
			CallingContext cc ) throws DatastoreFailureException, AccessDeniedException {
		
		ArrayList<GrantedAuthorityInfo> authenticatedGrants = new ArrayList<GrantedAuthorityInfo>(); 
		ArrayList<GrantedAuthorityInfo> anonGrants = new ArrayList<GrantedAuthorityInfo>(); 
		ArrayList<UserSecurityInfo> users = new ArrayList<UserSecurityInfo>();  
		ArrayList<GrantedAuthorityInfo> allGroups = new ArrayList<GrantedAuthorityInfo>();

		// NOTE: No users are defined at this point (including the superUser) see superUserBootstrap below...
		setStandardSiteAccessConfiguration( users, authenticatedGrants, anonGrants, allGroups, cc );
	}
	
	/**
	 * Ensures that a (single) registered user record exists for the superUser,
	 * adds that user to the list of site administrators, establishes that 
	 * user as the sole user with permanent access to the permissions management tab,
	 * and, if the user is new, it sets a flag to force the user to visit the
	 * permissions tab upon first access to the site (this is done inside assertSuperUser).
	 * 
	 * @param cc
	 * @throws ODKDatastoreException
	 */
	public static final synchronized void superUserBootstrap(CallingContext cc) throws ODKDatastoreException {
		// assert that the superuser exists...
		RegisteredUsersTable su = RegisteredUsersTable.assertSuperUser(cc);
		
		Set<String> uriUsers;
		
		// add the superuser to the list of site administrators
		uriUsers = UserGrantedAuthority.getUriUsers(siteAuth, cc.getDatastore(), cc.getCurrentUser());
		uriUsers.add(su.getUri());
		UserGrantedAuthority.assertGrantedAuthorityMembers(siteAuth, uriUsers, cc);
		
		// assert that the superuser is the only one with permanent access administration rights...
		uriUsers.clear();
		uriUsers.add(su.getUri());
		UserGrantedAuthority.assertGrantedAuthorityMembers(
				new GrantedAuthorityImpl(GrantedAuthorityNames.ROLE_ACCESS_ADMIN.name()), uriUsers, cc);
	}

}
