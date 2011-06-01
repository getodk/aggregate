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

import java.util.Date;
import java.util.List;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.common.EmailParser.Email;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

/**
 * Table of registered users of the system.  Currently, only the 
 * password fields, the SALT and the IS_CREDENTIALS_NON_EXPIRED 
 * and IS_ENABLED fields are exposed to the user.
 * <p>
 * The table contains 3 sets of credentials:
 * <ul>
 * <li>LOCAL_USERNAME + DIGEST_AUTH_PASSWORD</li>
 * <li>LOCAL_USERNAME + BASIC_AUTH_PASSWORD + BASIC_AUTH_SALT</li>
 * <li>OPENID_EMAIL</li></ul>
 *  <p>
 *  The format of LOCAL_USERNAME is any string less than 80 characters.
 *  The format of OPENID_EMAIL must be of the form
 *   mailto:uid@domain.name and less than 80 characters.
 * <p>
 *  The LOCAL_USERNAME credential is used by ODK Collect communications.
 *  (you can configure the server to use either digest or basic auth).
 *  Note that basic-auth credentials can be used for forms-based auth.
 *  The OPENID_EMAIL is used for openid authentications.
 *  <p>
 *  Records in this table are never deleted.  Instead, they are marked
 *  with IS_REMOVED = true.  This allows audit tracking back to the username.
 *  Once marked as IS_REMOVED, that row is never reinstated.  The superuser
 *  must create a new row for the user.
 *  
 * @author mitchellsundt@gmail.com
 *
 */
public final class RegisteredUsersTable extends CommonFieldsBase {
	// prefix that identifies a user id
	// user ids are of the form uid:username-yyyyMMddTHHmmSS
	public static final String UID_PREFIX = "uid:";

	private static final String TABLE_NAME = "_registered_users";
	
	private static final DataField LOCAL_USERNAME = new DataField(
			"LOCAL_USERNAME", DataField.DataType.STRING, true, 80L )
				.setIndexable(IndexType.ORDERED);
	
	private static final DataField OPENID_EMAIL = new DataField(
			"OPENID_EMAIL", DataField.DataType.STRING, true, 80L );
	
	private static final DataField NICKNAME = new DataField(
			"NICKNAME", DataField.DataType.STRING, true );
	
	private static final DataField BASIC_AUTH_PASSWORD = new DataField(
			"BASIC_AUTH_PASSWORD", DataField.DataType.STRING, true );

	private static final DataField BASIC_AUTH_SALT = new DataField(
			"BASIC_AUTH_SALT", DataField.DataType.STRING, true, 8L );

	private static final DataField DIGEST_AUTH_PASSWORD = new DataField(
			"DIGEST_AUTH_PASSWORD", DataField.DataType.STRING, true );

	private static final DataField IS_CREDENTIALS_NON_EXPIRED = new DataField(
			"IS_CREDENTIALS_NON_EXPIRED", DataField.DataType.BOOLEAN, false );

	private static final DataField IS_ENABLED = new DataField(
			"IS_ENABLED", DataField.DataType.BOOLEAN, false );
	
	private static final DataField IS_REMOVED = new DataField(
			"IS_REMOVED", DataField.DataType.BOOLEAN, false );
	
	/**
	 * Construct a relation prototype.  Only called via {@link #assertRelation(Datastore, User)}
	 * 
	 * @param schemaName
	 */
	protected RegisteredUsersTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(LOCAL_USERNAME);
		fieldList.add(OPENID_EMAIL);
		fieldList.add(NICKNAME);
		fieldList.add(BASIC_AUTH_PASSWORD);
		fieldList.add(BASIC_AUTH_SALT);
		fieldList.add(DIGEST_AUTH_PASSWORD);
		fieldList.add(IS_CREDENTIALS_NON_EXPIRED);
		fieldList.add(IS_ENABLED);
		fieldList.add(IS_REMOVED);
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
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
	
	public static Query createQuery(Datastore ds, User user) throws ODKDatastoreException {
		Query q = ds.createQuery(RegisteredUsersTable.assertRelation(ds, user), user);
		q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false );
		return q;
	}
	
	public static void applyNaturalOrdering(Query q) {
		q.addSort(LOCAL_USERNAME, Direction.ASCENDING);
	}
	
	public String getUsername() {
		return getStringField(LOCAL_USERNAME);
	}
	
	public void setUsername(String value) {
		if ( !setStringField(LOCAL_USERNAME, value)) {
			throw new IllegalStateException("overflow username");
		}
	}
	
	public String getEmail() {
		return getStringField(OPENID_EMAIL);
	}
	
	public void setEmail(String value) {
		if ( !setStringField(OPENID_EMAIL, value)) {
			throw new IllegalStateException("overflow email");
		}
	}
	
	public String getNickname() {
		return getStringField(NICKNAME);
	}
	
	public void setNickname(String value) {
		if ( !setStringField(NICKNAME, value)) {
			throw new IllegalStateException("overflow nickname");
		}
	}
	
	public String getDisplayName() {
		if ( getEmail() == null ) {
			return getUsername();
		} else {
			return getUsername() + " [" + getEmail() + "]";
		}
	}
	
	public String getBasicAuthPassword() {
		return getStringField(BASIC_AUTH_PASSWORD);
	}

	public void setBasicAuthPassword(String value) {
		if ( !setStringField(BASIC_AUTH_PASSWORD, value)) {
			throw new IllegalStateException("overflow basicAuthPassword");
		}
	}
	
	public String getBasicAuthSalt() {
		return getStringField(BASIC_AUTH_SALT);
	}

	public void setBasicAuthSalt(String value) {
		if ( !setStringField(BASIC_AUTH_SALT, value)) {
			throw new IllegalStateException("overflow basicAuthSalt");
		}
	}

	public String getDigestAuthPassword() {
		return getStringField(DIGEST_AUTH_PASSWORD);
	}
	
	public void setDigestAuthPassword(String value) {
		if ( !setStringField(DIGEST_AUTH_PASSWORD, value)) {
			throw new IllegalStateException("overflow digestAuthPassword");
		}
	}

	public Boolean getIsCredentialNonExpired() {
		return getBooleanField(IS_CREDENTIALS_NON_EXPIRED);
	}

	public void setIsCredentialNonExpired(Boolean value) {
		setBooleanField(IS_CREDENTIALS_NON_EXPIRED, value);
	}

	public Boolean getIsEnabled() {
		return getBooleanField(IS_ENABLED);
	}

	public void setIsEnabled(Boolean value) {
		setBooleanField(IS_ENABLED, value);
	}

	public Boolean getIsRemoved() {
		return getBooleanField(IS_REMOVED);
	}

	public void setIsRemoved(Boolean value) {
		setBooleanField(IS_REMOVED, value);
	}

	private static RegisteredUsersTable relation = null;
	
	/**
	 * This is private because this table has a no-deletions policy represented by the 
	 * IS_REMOVED flag.  Depending upon the semantics of the usage, return values should
	 * be filtered by the value of that flag to retrieve only the active users in the system.
	 * 
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	private static synchronized RegisteredUsersTable assertRelation(Datastore datastore, User user) throws ODKDatastoreException {
		if ( relation == null ) {
			RegisteredUsersTable relationPrototype;
			relationPrototype = new RegisteredUsersTable(datastore.getDefaultSchemaName());
			datastore.assertRelation(relationPrototype, user);
			relation = relationPrototype;
		}
		return relation;
	}
	
	/**
	 * This retrieves the given user record.  NOTE: the user may have been "deleted" from the 
	 * system, as indicated by IS_REMOVED = true.
	 * 
	 * @param uri
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static final RegisteredUsersTable getUserByUri(String uri, Datastore datastore, User user) throws ODKDatastoreException {
		RegisteredUsersTable prototype = assertRelation(datastore, user);
		return datastore.getEntity(prototype, uri, user);
	}
	
	public static final String generateUniqueUri( String username ) {
		String uri = UID_PREFIX + username + "|" + WebUtils.iso8601Date(new Date());
		return uri;
	}

	/**
	 * Used in the bowels of the security layer.  Others should call getUserByUsername.  
	 * Returns null if there is not exactly one record for the specified username.
	 * 
	 * @param username
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static final RegisteredUsersTable getUniqueUserByUsername(String username, Datastore datastore, User user) throws ODKDatastoreException {
		RegisteredUsersTable prototype = assertRelation(datastore, user);
		Query q = RegisteredUsersTable.createQuery(datastore, user);
		q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false);
		q.addFilter(LOCAL_USERNAME, FilterOperation.EQUAL, username);
		q.addSort(prototype.lastUpdateDate, Direction.DESCENDING);
		@SuppressWarnings("unchecked")
		List<RegisteredUsersTable> l = (List<RegisteredUsersTable>) q.executeQuery(0);
		if ( l.size() != 1 ) {
			return null;
		} else {
			return l.get(0);
		}
	}
	
	/**
	 * Retrieve the user identified by the specified username. 
	 * <p>This is generally a read-only activity, but if the datastore is corrupted by the 
	 * presence of two or more active records for this one username, the older records will
	 * be marked with IS_REMOVED=true and any privileges assigned to them will be removed.
	 * 
	 * @param username
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static final RegisteredUsersTable getUserByUsername(String username, CallingContext cc) throws ODKDatastoreException {
		Datastore datastore = cc.getDatastore();
		User user = cc.getCurrentUser();
		RegisteredUsersTable prototype = assertRelation(datastore, user);
		Query q = RegisteredUsersTable.createQuery(datastore, user);
		q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false);
		q.addFilter(LOCAL_USERNAME, FilterOperation.EQUAL, username);
		q.addSort(prototype.lastUpdateDate, Direction.DESCENDING);
		@SuppressWarnings("unchecked")
		List<RegisteredUsersTable> l = (List<RegisteredUsersTable>) q.executeQuery(0);
		if ( l.size() > 1 ) {
			// two or more active records with the same username.
			// remove the older ones, keeping only the newest.
			RegisteredUsersTable t = l.get(0);
			for ( int i = 1 ; i < l.size() ; ++i ) {
				RegisteredUsersTable tt = l.get(i);
				// delete all the group memberships of the entity being removed...
				UserGrantedAuthority.deleteGrantedAuthoritiesForUser(tt.getUri(), cc);
				// flag the duplicate as removed...
				tt.setIsRemoved(true);
				datastore.putEntity(tt, user);
			}
			l.clear();
			l.add(t);
		}
		
		if ( l.size() == 0 ) {
			return null;
		} else {
			return l.get(0);
		}
	}
	
	/**
	 * If the given username is not present, this will create a record for the user, 
	 * marking them as active (able to log in via OpenID or Aggregate password). Otherwise,
	 * this will just selectively update the nickname and e-mail address of the existing 
	 * record and return it.</p><p>
	 * The nickname is updated if the supplied Email object has a revised e-mail address
	 * or if the supplied Email object has a nickname that is different from the username
	 * of the e-mail address.  The e-mail address is updated if it is non-null and
	 * different from the address on file.</p><p>
	 * NOTE: If the intent is to clear the e-mail address, this must be done in a separate
	 * step.  Similarly, once a user is defined, changing the active status of the user 
	 * (their ability to log in using OpenID or their Aggregate password) must be done as
	 * a separate step.</p><p>
	 * NOTE: users won't be able to log in with OpenID if no e-mail address is supplied;
	 * and they won't be able to log in with an Aggregate password until one is defined.</p><p>
	 * In all cases, the Uri of the database record is saved in the Email object
	 * and the database record is returned to the caller.</p>
	 * 
	 * @param e
	 * @param ds
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static final RegisteredUsersTable assertActiveUserByUsername( Email e, 
							CallingContext cc ) throws ODKDatastoreException {
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		RegisteredUsersTable prototype = RegisteredUsersTable.assertRelation(ds, user);
		RegisteredUsersTable t = RegisteredUsersTable.getUserByUsername(e.getUsername(), cc);
		if ( t == null ) {
			// new user
			RegisteredUsersTable r = ds.createEntityUsingRelation(prototype, user);
			String uri = generateUniqueUri(e.getUsername());
			r.setStringField(prototype.primaryKey, uri);
			e.setUri(r.getUri());
			r.setUsername(e.getUsername());
			r.setEmail(e.getEmail());
			r.setNickname(e.getNickname());
			r.setIsCredentialNonExpired(true);
			r.setIsEnabled(true);
			r.setIsRemoved(false);
			ds.putEntity(r, user);
			return r;
		} else {
			e.setUri(t.getUri());
			if ( t.getEmail() == null ) {
				// set nickname and e-mail
				t.setNickname(e.getNickname());
				t.setEmail(e.getEmail());
			} else if ( e.getEmail() != null ) {
				// update specifies an e-mail...
				Email ref = new Email(t.getNickname(), t.getEmail());
				if ( (!ref.hasDistinctNickname() && e.hasDistinctNickname()) ||
						!ref.getEmail().equals(e.getEmail()) ) {
					// Either:
					// (1) recorded nickname is just the username of the email
					//     and new nickname is the quoted string of the email.
					// or
					// (2) emails are not the same.
					t.setNickname(e.getNickname());
					t.setEmail(e.getEmail());
				}
			}
			ds.putEntity(t, user);
			return t;
		}
	}

	/**
	 * If the given username is not present, this will create a record for the user, 
	 * marking them as active (able to log in via OpenID or Aggregate password). Otherwise,
	 * this will just update the nickname and e-mail address of the existing 
	 * record and return it.</p><p>
	 * NOTE: Once a user is defined, changing the active status of the user 
	 * (their ability to log in using OpenID or their Aggregate password) must be done as
	 * a separate step.</p><p>
	 * NOTE: users won't be able to log in with OpenID if no e-mail address is supplied;
	 * and they won't be able to log in with an Aggregate password until one is defined.</p>
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
		RegisteredUsersTable t = RegisteredUsersTable.getUserByUsername(u.getUsername(), cc);
		if ( t == null ) {
			// new user
			RegisteredUsersTable r = ds.createEntityUsingRelation(prototype, user);
			String uri = generateUniqueUri(u.getUsername());
			r.setStringField(prototype.primaryKey, uri);
			r.setUsername(u.getUsername());
			r.setEmail(u.getEmail());
			r.setNickname(u.getNickname());
			r.setIsCredentialNonExpired(true);
			r.setIsEnabled(true);
			r.setIsRemoved(false);
			ds.putEntity(r, user);
			return r;
		} else {
			t.setEmail(u.getEmail());
			t.setNickname(u.getNickname());
			ds.putEntity(t, user);
			return t;
		}
	}
	
	public static final List<RegisteredUsersTable> getUsersByEmail(String email, Datastore datastore, User user) throws ODKDatastoreException {
		RegisteredUsersTable prototype = assertRelation(datastore, user);
		Query q = datastore.createQuery(prototype, user);
		q.addFilter(IS_REMOVED, FilterOperation.EQUAL, false);
		q.addFilter(OPENID_EMAIL, FilterOperation.EQUAL, email);
		@SuppressWarnings("unchecked")
		List<RegisteredUsersTable> l = (List<RegisteredUsersTable>) q.executeQuery(0);
		return l;
	}

	/**
	 * Attempts to find a super-user record with the PK of the registered user equal to the 
	 * super-user e-mail address. If it can't, it then 
	 * @param cc
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static final RegisteredUsersTable assertSuperUser(CallingContext cc) throws ODKDatastoreException {
		return assertSuperUser(cc.getUserService().getSuperUserEmail(), cc.getDatastore(), cc.getUserService().getDaemonAccountUser());
	}
	
	private static synchronized final RegisteredUsersTable assertSuperUser(String superUserEmail, Datastore datastore, User user) throws ODKDatastoreException {
		RegisteredUsersTable t = null;
		
		List<RegisteredUsersTable> l = getUsersByEmail(superUserEmail, datastore, user);
		if ( l.size() == 1 ) {
			t = l.get(0);
			if ( !t.getIsCredentialNonExpired() || !t.getIsEnabled() ) {
				t.setIsCredentialNonExpired(true);
				t.setIsEnabled(true);
				datastore.putEntity(t, user);
			}
			return t;
		}

		RegisteredUsersTable prototype = assertRelation(datastore, user);
		
		try {
			t = datastore.getEntity(prototype, superUserEmail, user);
		} catch ( ODKEntityNotFoundException e) {
		}
		
		Email e = new Email(null, superUserEmail);
		if ( t != null ) {
			// must have been deleted -- resurrect it...
			t.setIsCredentialNonExpired(true);
			t.setIsEnabled(true);
			t.setIsRemoved(false);
			t.setBasicAuthPassword(null);
			t.setBasicAuthSalt(null);
			t.setDigestAuthPassword(null);
			t.setUsername(e.getUsername());
			t.setNickname(e.getNickname());
			t.setEmail(e.getEmail());
			datastore.putEntity(t, user);
			return t;
		}
		
		t = datastore.createEntityUsingRelation(prototype, user);
		t.setStringField(t.primaryKey, superUserEmail);
		t.setIsCredentialNonExpired(true);
		t.setIsEnabled(true);
		t.setUsername(e.getUsername());
		t.setNickname(e.getNickname());
		t.setEmail(e.getEmail());
		datastore.putEntity(t, user);
		return t;
	}
}
