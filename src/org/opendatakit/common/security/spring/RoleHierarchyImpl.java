/*
 * Extensively copied from Spring Security 3.0.5 RoleHierarchyImpl
 * Copyright (C) 2010 University of Washington.
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
package org.opendatakit.common.security.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.hierarchicalroles.CycleInRoleHierarchyException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Much of this implementation is copied verbatim from Spring 3.0.5
 * RoleHierarchyImpl.  The only difference is the use of InitializingBean
 * and the implementation of the buildRolesReachableInOneStepMap() which
 * now queries the database for the entries to insert into the map.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class RoleHierarchyImpl implements RoleHierarchy, InitializingBean {

	private static final Log logger = LogFactory.getLog(RoleHierarchyImpl.class);

	/** bean to the datastore */
	private Datastore datastore = null;
	/** bean to the userService */
	private UserService userService = null;

    /**
     * rolesReachableInOneOrMoreStepsMap is a Map that under the key of a specific role 
     * name contains a set of all roles reachable from this role in 1 or more steps.
     * 
     * NOTE: should only be set/accessed with updateRolesMap()/getRolesMap()
     */
    private Map<GrantedAuthority, Set<GrantedAuthority>> rolesReachableInOneOrMoreStepsMap = null;
	
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

	@Override
	public void afterPropertiesSet() throws Exception {
		if ( datastore == null ) {
			throw new IllegalStateException("datastore cannot be unspecified");
		}
		if ( userService == null ) {
			throw new IllegalStateException("userService cannot be unspecified");
		}
		
		refreshReachableGrantedAuthorities();
	}

	/**
	 * Update the rolesReachableInOneOrMoreStepsMap with a clean fetch from the database.
	 * 
	 * @throws ODKDatastoreException
	 */
	public void refreshReachableGrantedAuthorities() throws ODKDatastoreException {
		Map<GrantedAuthority, Set<GrantedAuthority>> localRolesReachableInOneOrMoreStepsMap =
			buildRolesReachableInOneOrMoreStepsMap(buildRolesReachableInOneStepMap());
		updateRolesMap(localRolesReachableInOneOrMoreStepsMap);
		// and wipe the user service, since permissions may have changed...
		userService.reloadPermissions();
	}
	
	/**
	 * Atomically swap out the rolesReachableInOneOrMoreStepsMap.
	 * 
	 * @param localRolesReachableInOneOrMoreStepsMap
	 */
	private synchronized void updateRolesMap(Map<GrantedAuthority, Set<GrantedAuthority>> localRolesReachableInOneOrMoreStepsMap) {
		rolesReachableInOneOrMoreStepsMap = localRolesReachableInOneOrMoreStepsMap;
	}

	/**
	 * Atomically fetch the rolesReachableInOneOrMoreStepsMap.
	 * @return
	 */
	private synchronized Map<GrantedAuthority, Set<GrantedAuthority>> getRolesMap() {
		return rolesReachableInOneOrMoreStepsMap;
	}

	@Override
    public Collection<GrantedAuthority> getReachableGrantedAuthorities(Collection<GrantedAuthority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return AuthorityUtils.NO_AUTHORITIES;
        }
        Map<GrantedAuthority, Set<GrantedAuthority>> localRolesReachableInOneOrMoreStepsMap = getRolesMap();
        
        Set<GrantedAuthority> reachableRoles = new HashSet<GrantedAuthority>();

        for (GrantedAuthority authority : authorities) {
            addReachableRoles(reachableRoles, authority);
            Set<GrantedAuthority> additionalReachableRoles = 
            		getRolesReachableInOneOrMoreSteps(localRolesReachableInOneOrMoreStepsMap, authority);
            if (additionalReachableRoles != null) {
                reachableRoles.addAll(additionalReachableRoles);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("getReachableGrantedAuthorities() - From the roles " + authorities
                    + " one can reach " + reachableRoles + " in zero or more steps.");
        }

        List<GrantedAuthority> reachableRoleList = new ArrayList<GrantedAuthority>(reachableRoles.size());
        reachableRoleList.addAll(reachableRoles);

        return reachableRoleList;
    }

    // SEC-863
    private void addReachableRoles(Set<GrantedAuthority> reachableRoles,
            GrantedAuthority authority) {

        Iterator<GrantedAuthority> iterator = reachableRoles.iterator();
        while (iterator.hasNext()) {
            GrantedAuthority testAuthority = iterator.next();
            String testKey = testAuthority.getAuthority();
            if ((testKey != null) && (testKey.equals(authority.getAuthority()))) {
                return;
            }
        }
        reachableRoles.add(authority);
    }

    // SEC-863
    private Set<GrantedAuthority> getRolesReachableInOneOrMoreSteps(
    		Map<GrantedAuthority, Set<GrantedAuthority>> localRolesReachableInOneOrMoreStepsMap,
            GrantedAuthority authority) {

        if (authority.getAuthority() == null) {
            return null;
        }

        Iterator<GrantedAuthority> iterator = localRolesReachableInOneOrMoreStepsMap.keySet().iterator();
        while (iterator.hasNext()) {
            GrantedAuthority testAuthority = iterator.next();
            String testKey = testAuthority.getAuthority();
            if ((testKey != null) && (testKey.equals(authority.getAuthority()))) {
                return localRolesReachableInOneOrMoreStepsMap.get(testAuthority);
            }
        }

        return null;
    }
    /**
     * For every higher role from rolesReachableInOneStepMap store all roles that are reachable from it in the map of
     * roles reachable in one or more steps. (Or throw a CycleInRoleHierarchyException if a cycle in the role
     * hierarchy definition is detected)
     */
    private Map<GrantedAuthority, Set<GrantedAuthority>> buildRolesReachableInOneOrMoreStepsMap(
    		Map<GrantedAuthority, Set<GrantedAuthority>> rolesReachableInOneStepMap) {
    	Map<GrantedAuthority, Set<GrantedAuthority>> localCopyRolesReachableInOneOrMoreStepsMap =
    		new HashMap<GrantedAuthority, Set<GrantedAuthority>>();
        // iterate over all higher roles from rolesReachableInOneStepMap

        for(GrantedAuthority role : rolesReachableInOneStepMap.keySet()) {
            Set<GrantedAuthority> rolesToVisitSet = new HashSet<GrantedAuthority>();

            if (rolesReachableInOneStepMap.containsKey(role)) {
                rolesToVisitSet.addAll(rolesReachableInOneStepMap.get(role));
            }

            Set<GrantedAuthority> visitedRolesSet = new HashSet<GrantedAuthority>();

            while (!rolesToVisitSet.isEmpty()) {
                // take a role from the rolesToVisit set
                GrantedAuthority aRole = (GrantedAuthority) rolesToVisitSet.iterator().next();
                rolesToVisitSet.remove(aRole);
                addReachableRoles(visitedRolesSet, aRole);
                if (rolesReachableInOneStepMap.containsKey(aRole)) {
                    Set<GrantedAuthority> newReachableRoles = rolesReachableInOneStepMap.get(aRole);

                    // definition of a cycle: you can reach the role you are starting from
                    if (rolesToVisitSet.contains(role) || visitedRolesSet.contains(role)) {
                        throw new CycleInRoleHierarchyException();
                    } else {
                         // no cycle
                        rolesToVisitSet.addAll(newReachableRoles);
                    }
                }
            }
            localCopyRolesReachableInOneOrMoreStepsMap.put(role, visitedRolesSet);

            logger.debug("buildRolesReachableInOneOrMoreStepsMap() - From role "
                    + role + " one can reach " + visitedRolesSet + " in one or more steps.");
        }

        return localCopyRolesReachableInOneOrMoreStepsMap;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Code added for ODK Aggregate implementation
    
    /**
     * Parse input and build the map for the roles reachable in one step: the higher role will become a key that
     * references a set of the reachable lower roles.
     * @throws ODKDatastoreException 
     */
    private Map<GrantedAuthority, Set<GrantedAuthority>> buildRolesReachableInOneStepMap() throws ODKDatastoreException {
    	Map<GrantedAuthority, Set<GrantedAuthority>> rolesReachableInOneStepMap = new HashMap<GrantedAuthority, Set<GrantedAuthority>>();

        User user = userService.getDaemonAccountUser();
        GrantedAuthorityHierarchyTable relation = GrantedAuthorityHierarchyTable.assertRelation(datastore, user);
        Query query = datastore.createQuery(relation, user);
        List<? extends CommonFieldsBase> results = query.executeQuery(0);
        if ( results.size() == 0 ) {
        	GrantedAuthorityHierarchyTable.bootstrap(datastore, user);
        	query = datastore.createQuery(relation, user);
        	results = query.executeQuery(0);
        }
        for ( CommonFieldsBase b : results ) {
        	GrantedAuthorityHierarchyTable e = (GrantedAuthorityHierarchyTable) b;
            GrantedAuthority higherRole = e.getDominatingGrantedAuthority();
            GrantedAuthority lowerRole = e.getSubordinateGrantedAuthority();
            Set<GrantedAuthority> rolesReachableInOneStepSet = null;

            if (!rolesReachableInOneStepMap.containsKey(higherRole)) {
                rolesReachableInOneStepSet = new HashSet<GrantedAuthority>();
                rolesReachableInOneStepMap.put(higherRole, rolesReachableInOneStepSet);
            } else {
                rolesReachableInOneStepSet = rolesReachableInOneStepMap.get(higherRole);
            }
            addReachableRoles(rolesReachableInOneStepSet, lowerRole);

            logger.debug("buildRolesReachableInOneStepMap() - From role "
                    + higherRole + " one can reach role " + lowerRole + " in one step.");
        }
        
        return rolesReachableInOneStepMap;
    }
}
