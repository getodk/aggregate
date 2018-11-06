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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import javax.servlet.ServletContext;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.utils.WebStartup;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.hierarchicalroles.CycleInRoleHierarchyException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Much of this implementation is copied verbatim from Spring 3.0.5
 * RoleHierarchyImpl.  The only difference is the use of InitializingBean
 * and the implementation of the buildRolesReachableInOneStepMap() which
 * now queries the database for the entries to insert into the map.
 *
 * @author mitchellsundt@gmail.com
 */
public class RoleHierarchyImpl implements RoleHierarchy, InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(RoleHierarchyImpl.class);
  // look for flagged changes every CHECK_INTERVAL.
  private static final long CHECK_INTERVAL = 1000L; // 1 seconds
  // refresh everything every UPDATE_INTERVAL.
  private static final long UPDATE_INTERVAL = 2 * 60 * 1000L; // 2 minutes
  /**
   * bean to the datastore
   */
  private Datastore datastore = null;
  /**
   * bean to the userService
   */
  private UserService userService = null;
  /**
   * bean to password hash algorithm
   */
  private MessageDigestPasswordEncoder passwordEncoder = null;
  /**
   * bean to the startup action
   */
  private WebStartup startupAction = null;

  private long lastCheckTimestamp = System.currentTimeMillis();
  private long lastUpdateTimestamp = System.currentTimeMillis();

  /**
   * rolesReachableInOneOrMoreStepsMap is a Map that under the key of a specific role
   * name contains a set of all roles reachable from this role in 1 or more steps.
   * <p>
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

  public MessageDigestPasswordEncoder getPasswordEncoder() {
    return passwordEncoder;
  }

  public void setPasswordEncoder(MessageDigestPasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  public WebStartup getStartupAction() {
    return startupAction;
  }

  public void setStartupAction(WebStartup startupAction) {
    this.startupAction = startupAction;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (datastore == null) {
      throw new IllegalStateException("datastore cannot be unspecified");
    }
    if (userService == null) {
      throw new IllegalStateException("userService cannot be unspecified");
    }

    refreshReachableGrantedAuthorities();

    CallingContext bootstrapCc = new CallingContext() {
      @Override
      public Object getBean(String beanName) {
        if (beanName.equals(SecurityBeanDefs.ROLE_HIERARCHY_MANAGER)) {
          return RoleHierarchyImpl.this;
        } else if (beanName.equals(SecurityBeanDefs.BASIC_AUTH_PASSWORD_ENCODER)) {
          return RoleHierarchyImpl.this.passwordEncoder;
        } else {
          throw new IllegalStateException("Undefined");
        }
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
        if (asDaemon != true) {
          throw new IllegalStateException("Invalid context");
        }
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
        throw new IllegalStateException("Undefined");
      }

      @Override
      public String getWebApplicationURL() {
        throw new IllegalStateException("Undefined");
      }

      @Override
      public String getWebApplicationURL(String servletAddr) {
        throw new IllegalStateException("Undefined");
      }

      @Override
      public String getServerURL() {
        throw new IllegalStateException("Undefined");
      }

      @Override
      public String getSecureServerURL() {
        throw new IllegalStateException("Undefined");
      }
    };

    Datastore ds = bootstrapCc.getDatastore();
    User user = bootstrapCc.getCurrentUser();

    // gain single-access lock record in database...
    String lockedResourceName = "---startup-serialization-lock---";
    String startupLockId = UUID.randomUUID().toString();

    int i = 0;
    boolean locked = false;
    while (!locked) {
      if ((++i) % 10 == 0) {
        logger.warn("excessive wait count for startup serialization lock. Count: " + i);
        try {
          Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
        } catch (InterruptedException e) {
          // we remain in the loop even if we get kicked out.
        }
      } else if (i != 1) {
        try {
          Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
        } catch (InterruptedException e) {
          // we remain in the loop even if we get kicked out.
        }
      }
      TaskLock startupTaskLock = ds.createTaskLock(user);
      if (startupTaskLock.obtainLock(startupLockId, lockedResourceName,
          TaskLockType.STARTUP_SERIALIZATION)) {
        locked = true;
      }
      startupTaskLock = null;
    }

    // we hold the lock while we initialize stuff here...
    try {
      if (!userService.isAccessManagementConfigured()) {
        logger.warn("Configuring with default role name and role hierarchy");
        SecurityServiceUtil.setDefaultRoleNamesAndHierarchy(bootstrapCc);
      }

      // ensure that the superuser has admin privileges
      SecurityServiceUtil.superUserBootstrap(bootstrapCc);

      refreshReachableGrantedAuthorities();

      if (startupAction != null) {
        startupAction.doStartupAction(bootstrapCc);
      }
    } finally {
      // release the startup serialization lock
      try {
        for (i = 0; i < 10; i++) {
          TaskLock startupTaskLock = ds.createTaskLock(user);
          if (startupTaskLock.releaseLock(startupLockId, lockedResourceName,
              TaskLockType.STARTUP_SERIALIZATION)) {
            break;
          }
          startupTaskLock = null;
          try {
            Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
          } catch (InterruptedException e) {
            // just move on, this retry mechanism
            // is to make things nice
          }
        }
      } catch (ODKTaskLockException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Update the rolesReachableInOneOrMoreStepsMap with a clean fetch from the database.
   *
   */
  public void refreshReachableGrantedAuthorities() throws ODKDatastoreException {
    logger.info("Executing: refreshReachableGrantedAuthorities");
    try {
      Map<GrantedAuthority, Set<GrantedAuthority>> localRolesReachableInOneOrMoreStepsMap =
          buildRolesReachableInOneOrMoreStepsMap(buildRolesReachableInOneStepMap());
      updateRolesMap(localRolesReachableInOneOrMoreStepsMap);
      // and wipe the user service, since permissions may have changed...
      userService.reloadPermissions();
      lastCheckTimestamp = lastUpdateTimestamp = System.currentTimeMillis();
    } catch (ODKDatastoreException e) {
      logger.warn("Datastore failure: refreshReachableGrantedAuthorities -- adjusting retry time");
      // set ourselves up to hit the database again after half a check interval
      lastCheckTimestamp = System.currentTimeMillis() - CHECK_INTERVAL / 2L;
      throw e;
    }
  }

  /**
   * Atomically swap out the rolesReachableInOneOrMoreStepsMap.
   *
   */
  private synchronized void updateRolesMap(Map<GrantedAuthority, Set<GrantedAuthority>> localRolesReachableInOneOrMoreStepsMap) {
    rolesReachableInOneOrMoreStepsMap = localRolesReachableInOneOrMoreStepsMap;
  }

  /**
   * Atomically fetch the rolesReachableInOneOrMoreStepsMap.
   *
   */
  private synchronized Map<GrantedAuthority, Set<GrantedAuthority>> getRolesMap() {
    return rolesReachableInOneOrMoreStepsMap;
  }

  @Override
  public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(Collection<? extends GrantedAuthority> authorities) {
    if (authorities == null || authorities.isEmpty()) {
      return AuthorityUtils.NO_AUTHORITIES;
    }
    long timeRequestStarts = System.currentTimeMillis();
    if (timeRequestStarts > lastUpdateTimestamp + UPDATE_INTERVAL) {
      // update the security configuration entirely every UPDATE_INTERVAL...
      try {
        refreshReachableGrantedAuthorities();
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
      }
    } else if (timeRequestStarts > lastCheckTimestamp + CHECK_INTERVAL) {
      // check for updates to the security configuration every CHECK_INTERVAL...
      try {
        User daemon = userService.getDaemonAccountUser();
        long lastUsersChange = SecurityRevisionsTable.getLastRegisteredUsersRevisionDate(datastore, daemon);
        long lastGrantsChange = SecurityRevisionsTable.getLastRoleHierarchyRevisionDate(datastore, daemon);
        if (lastGrantsChange > lastCheckTimestamp) {
          refreshReachableGrantedAuthorities();
          // NOTE: Timestamps updated and user permissions have been reloaded.
        } else if (lastUsersChange > lastCheckTimestamp) {
          lastCheckTimestamp = System.currentTimeMillis();
          userService.reloadPermissions();
        } else {
          lastCheckTimestamp = System.currentTimeMillis();
          logger.debug("getReachableGrantedAuthorities -- interval check");
        }
      } catch (ODKDatastoreException e) {
        // log it, but assume we are OK...
        e.printStackTrace();
      }
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

    for (GrantedAuthority role : rolesReachableInOneStepMap.keySet()) {
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
   *
   */
  private synchronized Map<GrantedAuthority, Set<GrantedAuthority>> buildRolesReachableInOneStepMap() throws ODKDatastoreException {
    Map<GrantedAuthority, Set<GrantedAuthority>> rolesReachableInOneStepMap = new HashMap<GrantedAuthority, Set<GrantedAuthority>>();

    User user = userService.getDaemonAccountUser();
    TreeMap<String, TreeSet<String>> oneStepRelations =
        GrantedAuthorityHierarchyTable.getEntireGrantedAuthorityHierarchy(datastore, user);

    for (Map.Entry<String, TreeSet<String>> e : oneStepRelations.entrySet()) {
      GrantedAuthority higherRole = new SimpleGrantedAuthority(e.getKey());
      Set<GrantedAuthority> rolesReachableInOneStepSet = null;

      if (!rolesReachableInOneStepMap.containsKey(higherRole)) {
        rolesReachableInOneStepSet = new HashSet<GrantedAuthority>();
        rolesReachableInOneStepMap.put(higherRole, rolesReachableInOneStepSet);
      } else {
        rolesReachableInOneStepSet = rolesReachableInOneStepMap.get(higherRole);
      }

      for (String s : e.getValue()) {
        GrantedAuthority lowerRole = new SimpleGrantedAuthority(s);

        addReachableRoles(rolesReachableInOneStepSet, lowerRole);

        logger.debug("buildRolesReachableInOneStepMap() - From role "
            + higherRole + " one can reach role " + lowerRole + " in one step.");
      }
    }

    return rolesReachableInOneStepMap;
  }
}
