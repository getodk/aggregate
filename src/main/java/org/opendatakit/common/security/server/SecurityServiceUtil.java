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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
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

	public static final List<String> siteGrants;
	public static final List<String> formGrants;
	public static final List<String> submitterGrants;

	public static final List<String> anonSubmitterGrants;
	public static final List<String> anonAttachmentViewerGrants;
	
	static {
		List<String> isiteGrants = new ArrayList<String>();
		isiteGrants.add(GrantedAuthorityNames.ROLE_ACCESS_ADMIN.name());
		isiteGrants.add(GrantedAuthorityNames.GROUP_FORM_ADMINS);
		siteGrants = Collections.unmodifiableList(isiteGrants);
	
		List<String> iformGrants = new ArrayList<String>();
		iformGrants.add(GrantedAuthorityNames.ROLE_FORM_ADMIN.name());
		iformGrants.add(GrantedAuthorityNames.GROUP_SUBMITTERS);
		formGrants = Collections.unmodifiableList(iformGrants);
	
		List<String> isubmitterGrants = new ArrayList<String>();
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_ANALYST.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_ATTACHMENT_VIEWER.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_DOWNLOAD.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_LIST.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_SERVICES_ADMIN.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_SUBMISSION_UPLOAD.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_USER.name());
		submitterGrants = Collections.unmodifiableList(isubmitterGrants);

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
			} else if ( e.getKey().equals(GrantedAuthorityNames.GROUP_FORM_ADMINS) ) {
				for ( String s : e.getValue() ) {
					if ( formGrants.contains(s) ) continue;
					return false; 
				}
			} else if ( e.getKey().equals(GrantedAuthorityNames.GROUP_SUBMITTERS) ) {
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

}
