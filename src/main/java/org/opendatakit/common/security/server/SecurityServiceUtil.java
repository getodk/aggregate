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
import java.util.TreeSet;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.SecurityRevisionsTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Common utility methods extracted from the AccessConfigurationServlet so they
 * can be shared between the servlet and GWT server classes.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class SecurityServiceUtil {

  private static final Set<String> specialNames = new HashSet<String>();

  public static final GrantedAuthority siteAuth = new SimpleGrantedAuthority(
      GrantedAuthorityName.GROUP_SITE_ADMINS.name());
  public static final List<String> siteAdministratorGrants;

  public static final GrantedAuthority dataOwnerAuth = new SimpleGrantedAuthority(
      GrantedAuthorityName.GROUP_FORM_MANAGERS.name());
  public static final List<String> dataOwnerGrants;

  public static final GrantedAuthority administerTablesAuth = new SimpleGrantedAuthority(
      GrantedAuthorityName.GROUP_ADMINISTER_TABLES.name());
  public static final List<String> administerTablesGrants;

  public static final GrantedAuthority synchronizeTablesAuth = new SimpleGrantedAuthority(
      GrantedAuthorityName.GROUP_SYNCHRONIZE_TABLES.name());
  public static final List<String> synchronizeTablesGrants;

  public static final GrantedAuthority dataViewerAuth = new SimpleGrantedAuthority(
      GrantedAuthorityName.GROUP_DATA_VIEWERS.name());
  public static final List<String> dataViewerGrants;

  public static final GrantedAuthority dataCollectorAuth = new SimpleGrantedAuthority(
      GrantedAuthorityName.GROUP_DATA_COLLECTORS.name());
  public static final List<String> dataCollectorGrants;

  public static final GrantedAuthority anonAuth = new SimpleGrantedAuthority(
      GrantedAuthorityName.USER_IS_ANONYMOUS.name());
  // special grants for Google Earth work-around
  public static final List<String> anonAttachmentViewerGrants;

  static {
    List<String> isiteAdministratorGrants = new ArrayList<String>();
    isiteAdministratorGrants.add(GrantedAuthorityName.ROLE_USER.name());
    isiteAdministratorGrants.add(GrantedAuthorityName.ROLE_SITE_ACCESS_ADMIN.name());
    isiteAdministratorGrants.add(GrantedAuthorityName.GROUP_ADMINISTER_TABLES.name());
    isiteAdministratorGrants.add(GrantedAuthorityName.GROUP_SYNCHRONIZE_TABLES.name());
    isiteAdministratorGrants.add(GrantedAuthorityName.GROUP_FORM_MANAGERS.name());
    isiteAdministratorGrants.add(GrantedAuthorityName.GROUP_DATA_VIEWERS.name());
    siteAdministratorGrants = Collections.unmodifiableList(isiteAdministratorGrants);

    List<String> iadministerTablesGrants = new ArrayList<String>();
    iadministerTablesGrants.add(GrantedAuthorityName.ROLE_USER.name());
    iadministerTablesGrants.add(GrantedAuthorityName.ROLE_ADMINISTER_TABLES.name());
    iadministerTablesGrants.add(GrantedAuthorityName.GROUP_DATA_VIEWERS.name());
    administerTablesGrants = Collections.unmodifiableList(iadministerTablesGrants);

    List<String> isynchronizeTablesGrants = new ArrayList<String>();
    isynchronizeTablesGrants.add(GrantedAuthorityName.ROLE_USER.name());
    isynchronizeTablesGrants.add(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES.name());
    isynchronizeTablesGrants.add(GrantedAuthorityName.GROUP_DATA_VIEWERS.name());
    synchronizeTablesGrants = Collections.unmodifiableList(isynchronizeTablesGrants);

    List<String> idataOwnerGrants = new ArrayList<String>();
    idataOwnerGrants.add(GrantedAuthorityName.ROLE_USER.name());
    idataOwnerGrants.add(GrantedAuthorityName.ROLE_DATA_OWNER.name());
    idataOwnerGrants.add(GrantedAuthorityName.GROUP_DATA_VIEWERS.name());
    dataOwnerGrants = Collections.unmodifiableList(idataOwnerGrants);

    List<String> idataViewerGrants = new ArrayList<String>();
    idataViewerGrants.add(GrantedAuthorityName.ROLE_USER.name());
    idataViewerGrants.add(GrantedAuthorityName.ROLE_DATA_VIEWER.name());
    dataViewerGrants = Collections.unmodifiableList(idataViewerGrants);

    List<String> idataCollectorGrants = new ArrayList<String>();
    idataCollectorGrants.add(GrantedAuthorityName.ROLE_DATA_COLLECTOR.name());
    dataCollectorGrants = Collections.unmodifiableList(idataCollectorGrants);

    // Work-around hack for Google Earth top-level balloon image
    List<String> ianonAttachmentViewerGrants = new ArrayList<String>();
    ianonAttachmentViewerGrants.add(GrantedAuthorityName.ROLE_ATTACHMENT_VIEWER.name());
    anonAttachmentViewerGrants = Collections.unmodifiableList(ianonAttachmentViewerGrants);
  }

  /**
   * Return all registered users and the Anonymous user.
   *
   * @param withAuthorities
   * @param cc
   * @return
   * @throws AccessDeniedException
   * @throws DatastoreFailureException
   */
  public static ArrayList<UserSecurityInfo> getAllUsers(boolean withAuthorities, CallingContext cc)
      throws AccessDeniedException, DatastoreFailureException {

    ArrayList<UserSecurityInfo> users = new ArrayList<UserSecurityInfo>();
    try {
      Query q = RegisteredUsersTable.createQuery(cc.getDatastore(),
          "SecurityServiceUtil.getAllUsers", cc.getCurrentUser());
      RegisteredUsersTable.applyNaturalOrdering(q, cc);

      List<? extends CommonFieldsBase> l = q.executeQuery();

      for (CommonFieldsBase cb : l) {
        RegisteredUsersTable t = (RegisteredUsersTable) cb;
        UserSecurityInfo i = new UserSecurityInfo(t.getUsername(), t.getFullName(), t.getEmail(),
            UserSecurityInfo.UserType.REGISTERED);
        if (withAuthorities) {
          SecurityServiceUtil.setAuthenticationLists(i, t.getUri(), cc);
        }
        users.add(i);
      }
      // TODO: why doesn't this work?
      UserSecurityInfo anonymous = new UserSecurityInfo(User.ANONYMOUS_USER,
          User.ANONYMOUS_USER_NICKNAME, null, UserSecurityInfo.UserType.ANONYMOUS);
      if (withAuthorities) {
        SecurityServiceUtil.setAuthenticationListsForSpecialUser(anonymous,
            GrantedAuthorityName.USER_IS_ANONYMOUS, cc);
      }
      users.add(anonymous);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
    // the natural ordering (above) produces a sorted list...
    return users;
  }

  public static HashMap<String, UserSecurityInfo> getUriUserSecurityInfoMap(
      boolean withAuthorities, CallingContext cc) throws AccessDeniedException,
      DatastoreFailureException {

    HashMap<String, UserSecurityInfo> users = new HashMap<String, UserSecurityInfo>();
    try {
      Query q = RegisteredUsersTable.createQuery(cc.getDatastore(),
          "SecurityServiceUtil.getAllUsers", cc.getCurrentUser());
      RegisteredUsersTable.applyNaturalOrdering(q, cc);

      List<? extends CommonFieldsBase> l = q.executeQuery();

      for (CommonFieldsBase cb : l) {
        RegisteredUsersTable t = (RegisteredUsersTable) cb;
        UserSecurityInfo i = new UserSecurityInfo(t.getUsername(), t.getFullName(), t.getEmail(),
            UserSecurityInfo.UserType.REGISTERED);
        if (withAuthorities) {
          SecurityServiceUtil.setAuthenticationLists(i, t.getUri(), cc);
        }
        users.put(t.getUri(), i);
      }
      // TODO: why doesn't this work?
      UserSecurityInfo anonymous = new UserSecurityInfo(User.ANONYMOUS_USER,
          User.ANONYMOUS_USER_NICKNAME, null, UserSecurityInfo.UserType.ANONYMOUS);
      if (withAuthorities) {
        SecurityServiceUtil.setAuthenticationListsForSpecialUser(anonymous,
            GrantedAuthorityName.USER_IS_ANONYMOUS, cc);
      }
      users.put(User.ANONYMOUS_USER, anonymous);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
    return users;
  }

  static GrantedAuthorityName mapName(GrantedAuthority auth, Set<GrantedAuthority> badGrants) {
    GrantedAuthorityName name = null;
    try {
      name = GrantedAuthorityName.valueOf(auth.getAuthority());
    } catch (Exception e) {
      badGrants.add(auth);
    }
    return name;
  }

  /**
   * During upgrades or other operations, we may change the set of granted
   * authorities (the valid set are identified by GrantedAuthorityNames). Remove
   * the bad grants wherever we find them...
   *
   * @param badGrants
   * @throws ODKDatastoreException
   */
  static void removeBadGrantedAuthorities(Set<GrantedAuthority> badGrants, CallingContext cc)
      throws ODKDatastoreException {
    ArrayList<String> empty = new ArrayList<String>();
    for (GrantedAuthority auth : badGrants) {
      UserGrantedAuthority.assertGrantedAuthorityMembers(auth, empty, cc);
    }
  }

  static void setAuthenticationLists(UserSecurityInfo userInfo, String uriUser, CallingContext cc)
      throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    RoleHierarchy hierarchy = (RoleHierarchy) cc.getBean("hierarchicalRoleRelationships");
    Set<GrantedAuthority> grants = UserGrantedAuthority.getGrantedAuthorities(uriUser, ds, user);
    Set<GrantedAuthority> badGrants = new TreeSet<GrantedAuthority>();
    TreeSet<GrantedAuthorityName> groups = new TreeSet<GrantedAuthorityName>();
    TreeSet<GrantedAuthorityName> authorities = new TreeSet<GrantedAuthorityName>();
    for (GrantedAuthority grant : grants) {
      GrantedAuthorityName name = mapName(grant, badGrants);
      if (name != null) {
        if (GrantedAuthorityName.permissionsCanBeAssigned(grant.getAuthority())) {
          groups.add(name);
        } else {
          authorities.add(name);
        }
      }
    }
    Collection<? extends GrantedAuthority> auths = hierarchy.getReachableGrantedAuthorities(grants);
    for (GrantedAuthority auth : auths) {
      GrantedAuthorityName name = mapName(auth, badGrants);
      if (name != null && !GrantedAuthorityName.permissionsCanBeAssigned(auth.getAuthority())) {
        authorities.add(name);
      }
    }
    userInfo.setAssignedUserGroups(groups);
    userInfo.setGrantedAuthorities(authorities);
    removeBadGrantedAuthorities(badGrants, cc);
  }

  static void setAuthenticationListsForSpecialUser(UserSecurityInfo userInfo,
      GrantedAuthorityName specialGroup, CallingContext cc) throws DatastoreFailureException {
    RoleHierarchy hierarchy = (RoleHierarchy) cc.getBean("hierarchicalRoleRelationships");
    Set<GrantedAuthority> badGrants = new TreeSet<GrantedAuthority>();
    // The assigned groups are the specialGroup that this user defines
    // (i.e., anonymous or daemon) plus all directly-assigned assignable
    // permissions.
    TreeSet<GrantedAuthorityName> groups = new TreeSet<GrantedAuthorityName>();
    TreeSet<GrantedAuthorityName> authorities = new TreeSet<GrantedAuthorityName>();
    groups.add(specialGroup);
    GrantedAuthority specialAuth = new SimpleGrantedAuthority(specialGroup.name());
    try {
      Set<GrantedAuthority> auths = GrantedAuthorityHierarchyTable
          .getSubordinateGrantedAuthorities(specialAuth, cc);
      for (GrantedAuthority auth : auths) {
        GrantedAuthorityName name = mapName(auth, badGrants);
        if (name != null) {
          groups.add(name);
        }
      }
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException("Unable to retrieve granted authorities of "
          + specialGroup.name());
    }

    Collection<? extends GrantedAuthority> auths = hierarchy
        .getReachableGrantedAuthorities(Collections.singletonList(specialAuth));
    for (GrantedAuthority auth : auths) {
      GrantedAuthorityName name = mapName(auth, badGrants);
      if (name != null && !GrantedAuthorityName.permissionsCanBeAssigned(auth.getAuthority())) {
        authorities.add(name);
      }
    }
    userInfo.setAssignedUserGroups(groups);
    userInfo.setGrantedAuthorities(authorities);
    try {
      removeBadGrantedAuthorities(badGrants, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get the complete set of granted authorities (ROLE and RUN_AS grants) this user possesses.
   * 
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static TreeSet<GrantedAuthorityName> getCurrentUserSecurityInfo(CallingContext cc)
      throws ODKDatastoreException {
    User user = cc.getCurrentUser();
    TreeSet<GrantedAuthorityName> authorities = new TreeSet<GrantedAuthorityName>();
    if (user.isAnonymous()) {
      RoleHierarchy hierarchy = (RoleHierarchy) cc.getBean("hierarchicalRoleRelationships");
      Set<GrantedAuthority> badGrants = new TreeSet<GrantedAuthority>();
      // The assigned groups are the specialGroup that this user defines
      // (i.e., anonymous or daemon) plus all directly-assigned assignable
      // permissions.
      GrantedAuthority specialAuth = new SimpleGrantedAuthority(GrantedAuthorityName.USER_IS_ANONYMOUS.name());

      Collection<? extends GrantedAuthority> auths = hierarchy
          .getReachableGrantedAuthorities(Collections.singletonList(specialAuth));
      for (GrantedAuthority auth : auths) {
        GrantedAuthorityName name = mapName(auth, badGrants);
        if (name != null && !GrantedAuthorityName.permissionsCanBeAssigned(auth.getAuthority())) {
          authorities.add(name);
        }
      }
      removeBadGrantedAuthorities(badGrants, cc);
    } else {
      RegisteredUsersTable t;
      t = RegisteredUsersTable.getUserByUri(user.getUriUser(), cc.getDatastore(), user);

      Datastore ds = cc.getDatastore();
      RoleHierarchy hierarchy = (RoleHierarchy) cc.getBean("hierarchicalRoleRelationships");
      Set<GrantedAuthority> grants = UserGrantedAuthority.getGrantedAuthorities(user.getUriUser(), ds, user);
      Set<GrantedAuthority> badGrants = new TreeSet<GrantedAuthority>();
      TreeSet<GrantedAuthorityName> groups = new TreeSet<GrantedAuthorityName>();
      for (GrantedAuthority grant : grants) {
        GrantedAuthorityName name = mapName(grant, badGrants);
        if (name != null) {
          if (GrantedAuthorityName.permissionsCanBeAssigned(grant.getAuthority())) {
            groups.add(name);
          } else {
            authorities.add(name);
          }
        }
      }
      Collection<? extends GrantedAuthority> auths = hierarchy.getReachableGrantedAuthorities(grants);
      for (GrantedAuthority auth : auths) {
        GrantedAuthorityName name = mapName(auth, badGrants);
        if (name != null && !GrantedAuthorityName.permissionsCanBeAssigned(auth.getAuthority())) {
          authorities.add(name);
        }
      }
      removeBadGrantedAuthorities(badGrants, cc);
    }
    return authorities;
  }

  public static final synchronized boolean isSpecialName(String authority) {
    if (SecurityServiceUtil.specialNames.isEmpty()) {
      for (GrantedAuthorityName n : GrantedAuthorityName.values()) {
        SecurityServiceUtil.specialNames.add(n.name());
      }
    }

    return SecurityServiceUtil.specialNames.contains(authority)
        || authority.startsWith(GrantedAuthorityName.RUN_AS_PREFIX)
        || authority.startsWith(GrantedAuthorityName.ROLE_PREFIX);
  }

  /**
   * Construct and return the Email object for the superUser.
   *
   * @param cc
   * @return
   */
  public static final EmailParser.Email getSuperUserEmail(CallingContext cc) {
    String suEmail = cc.getUserService().getSuperUserEmail();
    if ( suEmail == null ) {
      return null;
    }
    return new EmailParser.Email(suEmail.substring(SecurityUtils.MAILTO_COLON.length(),
        suEmail.indexOf(SecurityUtils.AT_SIGN)), suEmail);
  }

  /**
   * Given a collection of users, ensure that each user is a registered user
   * (creating a registered user if one doesn't exist). </p>
   * <p>
   * The collection is assumed to be exhaustive. Users not in the list will be
   * deleted.
   * </p>
   *
   * @param users
   * @param cc
   * @return map of users to their Uri strings
   * @throws DatastoreFailureException
   * @throws AccessDeniedException
   */
  private static Map<UserSecurityInfo, String> setUsers(ArrayList<UserSecurityInfo> users,
      CallingContext cc) throws DatastoreFailureException, AccessDeniedException {
    List<UserSecurityInfo> allUsersList = getAllUsers(false, cc);

    Set<UserSecurityInfo> removedUsers = new TreeSet<UserSecurityInfo>();
    removedUsers.addAll(allUsersList);
    removedUsers.removeAll(users);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    Map<UserSecurityInfo, String> pkMap = new HashMap<UserSecurityInfo, String>();
    try {
      // mark absent users as removed...
      for (UserSecurityInfo u : removedUsers) {
        if (u.getType() != UserType.REGISTERED)
          continue;
        RegisteredUsersTable t;
        if (u.getUsername() == null) {
          t = RegisteredUsersTable.getUniqueUserByEmail(u.getEmail(), ds, user);
        } else {
          t = RegisteredUsersTable.getUniqueUserByUsername(u.getUsername(), ds, user);
        }
        if (t != null) {
          t.setIsRemoved(true);
          ds.putEntity(t, user);
        }
      }
      // go through all other users. Assert that they exist.
      // This will update the fields to match those specified.
      for (UserSecurityInfo u : users) {
        if (u.getType() != UserType.REGISTERED)
          continue;
        RegisteredUsersTable t = RegisteredUsersTable.assertActiveUserByUserSecurityInfo(u, cc);
        pkMap.put(u, t.getUri());
      }
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException("Incomplete security update", e);
    }
    return pkMap;
  }

  /**
   * Given a collection of users, ensure that each user is a registered user
   * (creating a registered user if one doesn't exist) and assign those users to
   * the granted authority.
   * <p>
   * The collection is assumed to be exhaustive. If there are other e-mails
   * already assigned to the granted authority, they will be removed so that
   * exactly the passed-in set of users are assigned to the authority, no more,
   * no less.
   * </p>
   *
   * @param users
   * @param auth
   * @param cc
   * @throws DatastoreFailureException
   */
  private static void setUsersOfGrantedAuthority(Map<UserSecurityInfo, String> pkMap,
      GrantedAuthority auth, CallingContext cc) throws DatastoreFailureException {
    Set<GrantedAuthority> badGrants = new TreeSet<GrantedAuthority>();
    GrantedAuthorityName name = mapName(auth, badGrants);
    if (name != null) {
      // build the set of uriUsers for this granted authority...
      TreeSet<String> desiredMembers = new TreeSet<String>();

      for (Map.Entry<UserSecurityInfo, String> u : pkMap.entrySet()) {
        UserSecurityInfo info = u.getKey();
        String uriUser = u.getValue();

        if (info.getAssignedUserGroups().contains(name)) {
          desiredMembers.add(uriUser);
        }
      }

      // assert that the authority has exactly this set of uriUsers (no more, no
      // less)
      try {
        UserGrantedAuthority.assertGrantedAuthorityMembers(auth, desiredMembers, cc);
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
        throw new DatastoreFailureException("Incomplete security update", e);
      }
    } else {
      try {
        removeBadGrantedAuthorities(badGrants, cc);
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
        throw new DatastoreFailureException("Incomplete security update", e);
      }
    }
  }

  /**
   * Method to enforce an access configuration constraining only registered
   * users, authenticated users and anonymous access.
   * 
   * @param users
   * @param anonGrants
   * @param allGroups
   * @param cc
   * @throws DatastoreFailureException
   * @throws AccessDeniedException
   */
  public static final void setStandardSiteAccessConfiguration(ArrayList<UserSecurityInfo> users,
      ArrayList<GrantedAuthorityName> allGroups, CallingContext cc)
      throws DatastoreFailureException, AccessDeniedException {

    List<String> anonGrantStrings = new ArrayList<String>();
    for (UserSecurityInfo i : users) {
      if (i.getType() == UserType.ANONYMOUS) {
        for (GrantedAuthorityName a : i.getAssignedUserGroups()) {
          if (anonAuth.getAuthority().equals(a.name()))
            continue; // avoid circularity...
          anonGrantStrings.add(a.name());
        }
        break;
      }
    }

    try {
      GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(siteAuth,
          SecurityServiceUtil.siteAdministratorGrants, cc);
      GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(administerTablesAuth,
          SecurityServiceUtil.administerTablesGrants, cc);
      GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(synchronizeTablesAuth,
          SecurityServiceUtil.synchronizeTablesGrants, cc);
      GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(dataOwnerAuth,
          SecurityServiceUtil.dataOwnerGrants, cc);
      GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(dataViewerAuth,
          SecurityServiceUtil.dataViewerGrants, cc);
      GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(dataCollectorAuth,
          SecurityServiceUtil.dataCollectorGrants, cc);

      GrantedAuthorityHierarchyTable
          .assertGrantedAuthorityHierarchy(anonAuth, anonGrantStrings, cc);

      TreeSet<String> authorities = GrantedAuthorityHierarchyTable
          .getAllPermissionsAssignableGrantedAuthorities(cc.getDatastore(), cc.getCurrentUser());
      authorities.remove(siteAuth.getAuthority());
      authorities.remove(administerTablesAuth.getAuthority());
      authorities.remove(synchronizeTablesAuth.getAuthority());
      authorities.remove(dataOwnerAuth.getAuthority());
      authorities.remove(dataViewerAuth.getAuthority());
      authorities.remove(dataCollectorAuth.getAuthority());
      authorities.remove(anonAuth.getAuthority());

      // remove anything else from database...
      List<String> empty = Collections.emptyList();
      for (String s : authorities) {
        GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(
            new SimpleGrantedAuthority(s), empty, cc);
      }

      Map<UserSecurityInfo, String> pkMap = setUsers(users, cc);
      setUsersOfGrantedAuthority(pkMap, siteAuth, cc);
      setUsersOfGrantedAuthority(pkMap, administerTablesAuth, cc);
      setUsersOfGrantedAuthority(pkMap, synchronizeTablesAuth, cc);
      setUsersOfGrantedAuthority(pkMap, dataOwnerAuth, cc);
      setUsersOfGrantedAuthority(pkMap, dataViewerAuth, cc);
      setUsersOfGrantedAuthority(pkMap, dataCollectorAuth, cc);

    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException("Incomplete update");
    } finally {
      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();
      try {
        SecurityRevisionsTable.setLastRegisteredUsersRevisionDate(ds, user);
      } catch (ODKDatastoreException e) {
        // if it fails, use RELOAD_INTERVAL to force reload.
        e.printStackTrace();
      }
      try {
        SecurityRevisionsTable.setLastRoleHierarchyRevisionDate(ds, user);
      } catch (ODKDatastoreException e) {
        // if it fails, use RELOAD_INTERVAL to force reload.
        e.printStackTrace();
      }
    }
  }

  public static final void setUserCredentials(CredentialsInfo credential, CallingContext cc)
      throws AccessDeniedException, DatastoreFailureException {
    Datastore ds = cc.getDatastore();
    User user = cc.getUserService().getCurrentUser();
    RegisteredUsersTable userDefinition = null;
    try {
      userDefinition = RegisteredUsersTable.getUserByUsername(credential.getUsername(),
          cc.getUserService(), ds);
      if (userDefinition == null) {
        throw new AccessDeniedException("User is not a registered user.");
      }
      userDefinition.setDigestAuthPassword(credential.getDigestAuthHash());
      userDefinition.setBasicAuthPassword(credential.getBasicAuthHash());
      userDefinition.setBasicAuthSalt(credential.getBasicAuthSalt());
      ds.putEntity(userDefinition, user);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e.getMessage());
    }
  }

  /**
   * Configures the server to have the default role names and role hierarchy.
   *
   * @param cc
   * @throws DatastoreFailureException
   * @throws AccessDeniedException
   */
  public static final void setDefaultRoleNamesAndHierarchy(CallingContext cc)
      throws DatastoreFailureException, AccessDeniedException {

    ArrayList<UserSecurityInfo> users = new ArrayList<UserSecurityInfo>();
    ArrayList<GrantedAuthorityName> allGroups = new ArrayList<GrantedAuthorityName>();

    // Grant the Anonymous user the ability to submit data to ODK Aggregate
    // Enables users to anonymously publish from ODK Collect into ODK Aggregate
    UserSecurityInfo anonymous = new UserSecurityInfo(User.ANONYMOUS_USER,
        User.ANONYMOUS_USER_NICKNAME, null, UserSecurityInfo.UserType.ANONYMOUS);
    TreeSet<GrantedAuthorityName> userGroups = new TreeSet<GrantedAuthorityName>();
    userGroups.add(GrantedAuthorityName.GROUP_DATA_COLLECTORS);
    userGroups.add(GrantedAuthorityName.GROUP_FORM_MANAGERS); // issue 710
    anonymous.setAssignedUserGroups(userGroups);
    users.add(anonymous);
    // NOTE: No users are defined at this point (including the superUser) see
    // superUserBootstrap below...
    setStandardSiteAccessConfiguration(users, allGroups, cc);
  }

  /**
   * Ensures that a (single) registered user record exists for the superUser,
   * adds that user to the list of site administrators, establishes that user as
   * the sole user with permanent access to the permissions management tab, and,
   * if the user is new, it sets a flag to force the user to visit the
   * permissions tab upon first access to the site (this is done inside
   * assertSuperUser).
   *
   * @param cc
   * @throws ODKDatastoreException
   */
  public static final synchronized void superUserBootstrap(CallingContext cc)
      throws ODKDatastoreException {
    // assert that the superuser exists...
    MessageDigestPasswordEncoder mde = null;
    try {
      Object obj = cc.getBean(SecurityBeanDefs.BASIC_AUTH_PASSWORD_ENCODER);
      if (obj != null) {
        mde = (MessageDigestPasswordEncoder) obj;
      }
    } catch (Exception e) {
      mde = null;
    }
    List<RegisteredUsersTable> suList = RegisteredUsersTable.assertSuperUsers(mde, cc);

    Set<String> uriUsers;

    // add the superuser to the list of site administrators
    uriUsers = UserGrantedAuthority.getUriUsers(siteAuth, cc.getDatastore(), cc.getCurrentUser());
    for (RegisteredUsersTable su : suList) {
      uriUsers.add(su.getUri());
    }
    UserGrantedAuthority.assertGrantedAuthorityMembers(siteAuth, uriUsers, cc);

    // assert that the superuser is the only one with permanent access
    // administration rights...
    uriUsers.clear();
    for (RegisteredUsersTable su : suList) {
      uriUsers.add(su.getUri());
    }
    UserGrantedAuthority.assertGrantedAuthorityMembers(new SimpleGrantedAuthority(
        GrantedAuthorityName.ROLE_SITE_ACCESS_ADMIN.name()), uriUsers, cc);
  }

}
