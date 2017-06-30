/*
 * Copyright (C) 2010 University of Washington
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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.RealmSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.common.EmailParser.Email;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

/**
 * Table of registered users of the system. Currently, only the password fields,
 * the SALT and the FULL_NAME are exposed to the user.
 * <p>
 * The table contains 3 sets of credentials:
 * <ul>
 * <li>LOCAL_USERNAME + DIGEST_AUTH_PASSWORD</li>
 * <li>LOCAL_USERNAME + BASIC_AUTH_PASSWORD + BASIC_AUTH_SALT</li>
 * <li>OAUTH2_EMAIL</li>
 * </ul>
 * <p>
 * The format of LOCAL_USERNAME is any string less than 80 characters. The
 * format of OAUTH2_EMAIL must be of the form mailto:uid@domain.name and less
 * than 80 characters.
 * <p>
 * The LOCAL_USERNAME credential is used by ODK Collect communications. (you can
 * configure the server to use either digest or basic auth). Note that
 * basic-auth credentials can be used for forms-based auth. The OAUTH2_EMAIL is
 * used for OAuth2 authentications.
 * <p>
 * Records in this table are never deleted. Instead, they are marked with
 * IS_REMOVED = true. This allows audit tracking back to the username. Once
 * marked as IS_REMOVED, that row is never reinstated. The superuser must create
 * a new row for the user.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class RegisteredUsersTable extends CommonFieldsBase {

  private static final Log logger = LogFactory.getLog(RegisteredUsersTable.class);

  // prefix that identifies a user id
  // user ids are of the form uid:username|yyyyMMddTHHmmSS
  public static final String UID_PREFIX = "uid:";

  private static final String TABLE_NAME = "_registered_users";

  // Unique key (disregarding removed) or null
  private static final DataField LOCAL_USERNAME = new DataField("LOCAL_USERNAME",
      DataField.DataType.STRING, true, 80L).setIndexable(IndexType.ORDERED); 

  // Unique key (disregarding removed) or null
  // NOTE: the column name in the database is not changed. This was 
  // used for OpenID authentication originally, but now is used for 
  // OAuth2 authentication.
  private static final DataField OAUTH2_EMAIL = new DataField("OPENID_EMAIL",
      DataField.DataType.STRING, true, 80L).setIndexable(IndexType.ORDERED);

  private static final DataField FULL_NAME = new DataField("FULL_NAME", DataField.DataType.STRING,
      true);

  private static final DataField BASIC_AUTH_PASSWORD = new DataField("BASIC_AUTH_PASSWORD",
      DataField.DataType.STRING, true);

  private static final DataField BASIC_AUTH_SALT = new DataField("BASIC_AUTH_SALT",
      DataField.DataType.STRING, true, 8L);

  private static final DataField DIGEST_AUTH_PASSWORD = new DataField("DIGEST_AUTH_PASSWORD",
      DataField.DataType.STRING, true);

  private static final DataField IS_REMOVED = new DataField("IS_REMOVED",
      DataField.DataType.BOOLEAN, false);

  /**
   * Construct a relation prototype. Only called via
   * {@link #assertRelation(Datastore, User)}
   * 
   * @param schemaName
   */
  protected RegisteredUsersTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(LOCAL_USERNAME);
    fieldList.add(OAUTH2_EMAIL);
    fieldList.add(FULL_NAME);
    fieldList.add(BASIC_AUTH_PASSWORD);
    fieldList.add(BASIC_AUTH_SALT);
    fieldList.add(DIGEST_AUTH_PASSWORD);
    fieldList.add(IS_REMOVED);
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   * 
   * @param ref
   * @param user
   */
  protected RegisteredUsersTable(RegisteredUsersTable ref, User user) {
    super(ref, user);
  }

  // Only called from within the persistence layer.
  @Override
  public CommonFieldsBase getEmptyRow(User user) {
    RegisteredUsersTable t = new RegisteredUsersTable(this, user);
    t.setIsRemoved(false); // start with this field being false...
    return t;
  }

  public static Query createQuery(Datastore ds, String loggingContextTag, User user)
      throws ODKDatastoreException {
    Query q = ds
        .createQuery(RegisteredUsersTable.assertRelation(ds, user), loggingContextTag, user);
    q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false);
    return q;
  }

  public static void applyNaturalOrdering(Query q, CallingContext cc) throws ODKDatastoreException {
    RegisteredUsersTable prototype = RegisteredUsersTable.assertRelation(cc.getDatastore(),
        cc.getCurrentUser());
    q.addSort(prototype.primaryKey, Direction.ASCENDING);
  }

  public String getUsername() {
    return getStringField(LOCAL_USERNAME);
  }

  public void setUsername(String value) {
    if (!setStringField(LOCAL_USERNAME, value)) {
      throw new IllegalStateException("overflow username");
    }
  }

  public String getEmail() {
    return getStringField(OAUTH2_EMAIL);
  }

  public void setEmail(String value) {
    if (!setStringField(OAUTH2_EMAIL, value)) {
      throw new IllegalStateException("overflow email");
    }
  }

  public String getFullName() {
    return getStringField(FULL_NAME);
  }

  public void setFullName(String value) {
    if (!setStringField(FULL_NAME, value)) {
      throw new IllegalStateException("overflow nickname");
    }
  }

  public String getDisplayName() {
    if (getEmail() == null) {
      return getUsername();
    } else {
      return getEmail();
    }
  }

  public String getBasicAuthPassword() {
    return getStringField(BASIC_AUTH_PASSWORD);
  }

  public void setBasicAuthPassword(String value) {
    if (!setStringField(BASIC_AUTH_PASSWORD, value)) {
      throw new IllegalStateException("overflow basicAuthPassword");
    }
  }

  public String getBasicAuthSalt() {
    return getStringField(BASIC_AUTH_SALT);
  }

  public void setBasicAuthSalt(String value) {
    if (!setStringField(BASIC_AUTH_SALT, value)) {
      throw new IllegalStateException("overflow basicAuthSalt");
    }
  }

  public String getDigestAuthPassword() {
    return getStringField(DIGEST_AUTH_PASSWORD);
  }

  public void setDigestAuthPassword(String value) {
    if (!setStringField(DIGEST_AUTH_PASSWORD, value)) {
      throw new IllegalStateException("overflow digestAuthPassword");
    }
  }

  public Boolean getIsRemoved() {
    return getBooleanField(IS_REMOVED);
  }

  public void setIsRemoved(Boolean value) {
    setBooleanField(IS_REMOVED, value);
  }

  private static RegisteredUsersTable relation = null;

  /**
   * This is private because this table has a no-deletions policy represented by
   * the IS_REMOVED flag. Depending upon the semantics of the usage, return
   * values should be filtered by the value of that flag to retrieve only the
   * active users in the system.
   * 
   * @param datastore
   * @param user
   * @return
   * @throws ODKDatastoreException
   */
  private static synchronized final RegisteredUsersTable assertRelation(Datastore datastore,
      User user) throws ODKDatastoreException {
    if (relation == null) {
      RegisteredUsersTable relationPrototype;
      relationPrototype = new RegisteredUsersTable(datastore.getDefaultSchemaName());
      datastore.assertRelation(relationPrototype, user);
      relation = relationPrototype;
    }
    return relation;
  }

  /**
   * This retrieves the given user record. NOTE: the user may have been
   * "deleted" from the system, as indicated by IS_REMOVED = true.
   * 
   * @param uri
   * @param datastore
   * @param user
   * @return
   * @throws ODKDatastoreException
   */
  public static final RegisteredUsersTable getUserByUri(String uri, Datastore datastore, User user)
      throws ODKDatastoreException {
    RegisteredUsersTable prototype = assertRelation(datastore, user);
    return datastore.getEntity(prototype, uri, user);
  }

  public static final String generateUniqueUri(String username, String email) {
    String uri;
    if (username == null) {
      uri = UID_PREFIX + email.substring(SecurityUtils.MAILTO_COLON.length()) + "|"
          + WebUtils.iso8601Date(new Date());
    } else {
      uri = UID_PREFIX + username + "|" + WebUtils.iso8601Date(new Date());
    }
    return uri;
  }

  /**
   * Used in the bowels of the security layer. Others should call
   * getUserByUsername. Returns null if there is not exactly one record for the
   * specified username.
   * 
   * @param username
   * @param datastore
   * @param user
   * @return
   * @throws ODKDatastoreException
   */
  public static final RegisteredUsersTable getUniqueUserByUsername(String username,
      Datastore datastore, User user) throws ODKDatastoreException {
    RegisteredUsersTable prototype = assertRelation(datastore, user);
    Query q = RegisteredUsersTable.createQuery(datastore,
        "RegisteredUsersTable.getUniqueUserByUsername", user);
    // already applied: q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false);
    q.addFilter(LOCAL_USERNAME, FilterOperation.EQUAL, username);
    q.addSort(LOCAL_USERNAME, Direction.ASCENDING); // GAE work-around
    q.addSort(prototype.lastUpdateDate, Direction.DESCENDING);
    @SuppressWarnings("unchecked")
    List<RegisteredUsersTable> l = (List<RegisteredUsersTable>) q.executeQuery();
    if (l.size() != 1) {
      return null;
    } else {
      return l.get(0);
    }
  }

  /**
   * Retrieve the user identified by the specified username.
   * <p>
   * This is generally a read-only activity, but if the datastore is corrupted
   * by the presence of two or more active records for this one username, the
   * older records will be marked with IS_REMOVED=true and any privileges
   * assigned to them will be removed.
   * 
   * @param username
   * @param datastore
   * @param user
   * @return
   * @throws ODKDatastoreException
   */
  public static final RegisteredUsersTable getUserByUsername(String username,
      UserService userService, Datastore datastore) throws ODKDatastoreException {
    User user = userService.getDaemonAccountUser();
    RegisteredUsersTable prototype = assertRelation(datastore, user);
    Query q = RegisteredUsersTable.createQuery(datastore, "RegisteredUsersTable.getUserByUsername",
        user);
    // already applied: q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false);
    q.addFilter(LOCAL_USERNAME, FilterOperation.EQUAL, username);
    q.addSort(LOCAL_USERNAME, Direction.ASCENDING); // GAE work-around
    q.addSort(prototype.lastUpdateDate, Direction.DESCENDING);
    @SuppressWarnings("unchecked")
    List<RegisteredUsersTable> l = (List<RegisteredUsersTable>) q.executeQuery();
    if (l.size() > 1) {
      // two or more active records with the same username.
      // remove the older ones, keeping only the newest.
      RegisteredUsersTable t = l.get(0);
      for (int i = 1; i < l.size(); ++i) {
        RegisteredUsersTable tt = l.get(i);
        // delete all the group memberships of the entity being removed...
        UserGrantedAuthority.deleteGrantedAuthoritiesForUser(tt.getUri(), userService, datastore,
            user);
        // flag the duplicate as removed...
        tt.setIsRemoved(true);
        datastore.putEntity(tt, user);
        logger.warn("duplicate username records for " + username + " - marking as removed: "
            + tt.getUri());
      }
      l.clear();
      l.add(t);
    }

    if (l.size() == 0) {
      return null;
    } else {
      return l.get(0);
    }
  }

  /**
   * Used in the bowels of the security layer. Others should call
   * getUserByEmail. Returns null if there is not exactly one record for the
   * specified email.
   * 
   * @param email
   * @param datastore
   * @param user
   * @return
   * @throws ODKDatastoreException
   */
  public static final RegisteredUsersTable getUniqueUserByEmail(String email, Datastore datastore,
      User user) throws ODKDatastoreException {
    RegisteredUsersTable prototype = assertRelation(datastore, user);
    Query q = RegisteredUsersTable.createQuery(datastore,
        "RegisteredUsersTable.getUniqueUserByEmail", user);
    // already applied: q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false);
    q.addFilter(OAUTH2_EMAIL, FilterOperation.EQUAL, email);
    q.addSort(OAUTH2_EMAIL, Direction.ASCENDING); // GAE work-around
    q.addSort(prototype.lastUpdateDate, Direction.DESCENDING);
    @SuppressWarnings("unchecked")
    List<RegisteredUsersTable> l = (List<RegisteredUsersTable>) q.executeQuery();
    if (l.size() != 1) {
      return null;
    } else {
      return l.get(0);
    }
  }

  public static final RegisteredUsersTable getUserByEmail(String email, UserService userService,
      Datastore datastore) throws ODKDatastoreException {
    User user = userService.getDaemonAccountUser();
    RegisteredUsersTable prototype = assertRelation(datastore, user);
    Query q = datastore.createQuery(prototype, "RegisteredUsersTable.getUserByEmail", user);
    q.addFilter(OAUTH2_EMAIL, FilterOperation.EQUAL, email);
    q.addSort(OAUTH2_EMAIL, Direction.ASCENDING); // GAE work-around
    q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false);
    q.addSort(prototype.lastUpdateDate, Direction.DESCENDING);
    @SuppressWarnings("unchecked")
    List<RegisteredUsersTable> l = (List<RegisteredUsersTable>) q.executeQuery();
    if (l.size() > 1) {
      // two or more active records with the same email.
      // remove the older ones, keeping only the newest.
      RegisteredUsersTable t = l.get(0);
      for (int i = 1; i < l.size(); ++i) {
        RegisteredUsersTable tt = l.get(i);
        // delete all the group memberships of the entity being removed...
        UserGrantedAuthority.deleteGrantedAuthoritiesForUser(tt.getUri(), userService, datastore,
            user);
        // flag the duplicate as removed...
        tt.setIsRemoved(true);
        datastore.putEntity(tt, user);
        logger.warn("duplicate OAuth2 email records for " + email + " - marking as removed: "
            + tt.getUri());
      }
      l.clear();
      l.add(t);
    }

    if (l.size() == 0) {
      return null;
    } else {
      return l.get(0);
    }
  }

  /**
   * If the given username is not present, this will create a record for the
   * user, marking them as active (able to log in via OAuth2 or Aggregate
   * password). Otherwise, this will just update the nickname and e-mail address
   * of the existing record and return it.</p>
   * <p>
   * NOTE: Once a user is defined, changing the active status of the user (their
   * ability to log in using OAuth2 or their Aggregate password) must be done as
   * a separate step.
   * </p>
   * <p>
   * NOTE: users won't be able to log in with OAuth2 if no e-mail address is
   * supplied; and they won't be able to log in with an Aggregate password until
   * one is defined.
   * </p>
   * 
   * @param u
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static RegisteredUsersTable assertActiveUserByUserSecurityInfo(UserSecurityInfo u,
      CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    RegisteredUsersTable prototype = RegisteredUsersTable.assertRelation(ds, user);
    RegisteredUsersTable t;
    if (u.getUsername() == null) {
      t = RegisteredUsersTable.getUserByEmail(u.getEmail(), cc.getUserService(), ds);
    } else {
      t = RegisteredUsersTable.getUserByUsername(u.getUsername(), cc.getUserService(), ds);
    }
    if (t == null) {
      // new user
      RegisteredUsersTable r = ds.createEntityUsingRelation(prototype, user);
      String uri = generateUniqueUri(u.getUsername(), u.getEmail());
      r.setStringField(prototype.primaryKey, uri);
      r.setUsername(u.getUsername());
      r.setEmail(u.getEmail());
      r.setFullName(u.getFullName());
      r.setIsRemoved(false);
      ds.putEntity(r, user);
      return r;
    } else {
      t.setFullName(u.getFullName());
      ds.putEntity(t, user);
      return t;
    }
  }

  private static final boolean resetSuperUserPasswordIfNecessary(RegisteredUsersTable t,
      boolean newUser, MessageDigestPasswordEncoder mde, CallingContext cc)
      throws ODKEntityPersistException, ODKOverQuotaException, ODKEntityNotFoundException {
    String localSuperUser = t.getUsername();
    String currentRealmString = cc.getUserService().getCurrentRealm().getRealmString();
    String lastKnownRealmString = ServerPreferencesProperties.getLastKnownRealmString(cc);
    if (!newUser && lastKnownRealmString != null && lastKnownRealmString.equals(currentRealmString)) {
      // no need to reset the passwords
      return false;
    }
    // The realm string has changed, so we need to reset the password.
    RealmSecurityInfo r = new RealmSecurityInfo();
    r.setRealmString(currentRealmString);
    r.setBasicAuthHashEncoding(mde.getAlgorithm());

    CredentialsInfo credential;
    try {
      credential = CredentialsInfoBuilderInternal.build(localSuperUser, r, "aggregate");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new IllegalStateException("unrecognized algorithm");
    }
    t.setDigestAuthPassword(credential.getDigestAuthHash());
    t.setBasicAuthPassword(credential.getBasicAuthHash());
    t.setBasicAuthSalt(credential.getBasicAuthSalt());
    // done setting the password...persist it...
    t.setIsRemoved(false);
    cc.getDatastore().putEntity(t, cc.getCurrentUser());
    // remember the current realm string
    ServerPreferencesProperties.setLastKnownRealmString(cc, currentRealmString);
    logger.warn("Reset password of the local superuser record: " + t.getUri() + " identified by: "
        + t.getUsername());
    return true;
  }

  /**
   * Attempts to find user records matching the local ODK Aggregate username and
   * the e-mail address of the super-user. There can only be at most one of
   * each. For each case:
   * <ol>
   * <li>If it finds a single such record, returns that user.</li>
   * <li>If it finds multiple records, it removes all but the most recently
   * created one, sets a flag to drive the super-user to the permissions page
   * upon first login, and returns that user.</li>
   * <li>If it finds no records, it creates one and sets a flag to drive the
   * super-user to the permissions page upon first login.</li>
   * </ol>
   * 
   * @param cc
   * @return list of the superUsers of record.
   * @throws ODKDatastoreException
   */
  public static final List<RegisteredUsersTable> assertSuperUsers(MessageDigestPasswordEncoder mde,
      CallingContext cc) throws ODKDatastoreException {
    List<RegisteredUsersTable> tList = new ArrayList<RegisteredUsersTable>();

    UserService userService = cc.getUserService();
    Datastore datastore = cc.getDatastore();
    boolean changesMade = false;
    try {
      // deal with the superUserEmail...
      String superUserEmail = userService.getSuperUserEmail();
      if (superUserEmail != null && superUserEmail.length() != 0) {
        User user = userService.getDaemonAccountUser();
        RegisteredUsersTable t = getUserByEmail(superUserEmail, userService, datastore);
        if (t != null) {
          tList.add(t);
        } else {
          RegisteredUsersTable prototype = assertRelation(datastore, user);

          Email e = new Email(null, superUserEmail);
          t = datastore.createEntityUsingRelation(prototype, user);
          String uri = generateUniqueUri(null, e.getEmail());
          t.setStringField(prototype.primaryKey, uri);
          t.setUsername(null);
          t.setFullName(e.getFullName());
          t.setEmail(e.getEmail());
          datastore.putEntity(t, user);
          logger.warn("Created a new superuser email record: " + t.getUri() + " identified by: "
              + t.getEmail());
          changesMade = true;
          tList.add(t);
        }
      }
      // deal with the superUserUsername...
      String localSuperUser = userService.getSuperUserUsername();
      if (localSuperUser != null && localSuperUser.length() != 0) {
        User user = userService.getDaemonAccountUser();
        RegisteredUsersTable t = RegisteredUsersTable.getUserByUsername(localSuperUser,
            userService, datastore);
        if (t != null) {
          changesMade = resetSuperUserPasswordIfNecessary(t, false, mde, cc);
          tList.add(t);
        } else {
          RegisteredUsersTable prototype = assertRelation(datastore, user);

          // new user
          t = datastore.createEntityUsingRelation(prototype, user);
          String uri = generateUniqueUri(localSuperUser, null);
          t.setStringField(prototype.primaryKey, uri);
          t.setUsername(localSuperUser);
          t.setEmail(null);
          t.setFullName(localSuperUser);
          datastore.putEntity(t, user);
          logger.warn("Created a new local superuser record: " + t.getUri() + " identified by: "
              + t.getUsername());
          changesMade = resetSuperUserPasswordIfNecessary(t, true, mde, cc);
          tList.add(t);
        }
      }
    } finally {
      if (changesMade) {
        SecurityRevisionsTable.setLastSuperUserIdRevisionDate(datastore,
            userService.getDaemonAccountUser());
      }
    }
    return tList;
  }
}
