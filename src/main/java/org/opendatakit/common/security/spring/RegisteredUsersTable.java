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

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

/**
 * Table of registered users of the system.  Currently, only the 
 * password fields, the SALT and the IS_CREDENTIALS_NON_EXPIRED 
 * and IS_ENABLED fields are exposed to the user.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class RegisteredUsersTable extends CommonFieldsBase {
	private static final String TABLE_NAME = "_registered_users";
	
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
	
	/**
	 * Construct a relation prototype.  Only called via {@link #assertRelation(Datastore, User)}
	 * 
	 * @param schemaName
	 */
	protected RegisteredUsersTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(NICKNAME);
		fieldList.add(BASIC_AUTH_PASSWORD);
		fieldList.add(BASIC_AUTH_SALT);
		fieldList.add(DIGEST_AUTH_PASSWORD);
		fieldList.add(IS_CREDENTIALS_NON_EXPIRED);
		fieldList.add(IS_ENABLED);
		primaryKey.setIndexable(IndexType.ORDERED);
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
		return new RegisteredUsersTable(this, user);
	}
	
	public String getUriUser() {
		return getStringField(primaryKey);
	}
	
	public void setUriUser(String value) {
		if ( !setStringField(primaryKey, value)) {
			throw new IllegalStateException("overflow primaryKey");
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

	private static RegisteredUsersTable relation = null;
	
	public static synchronized RegisteredUsersTable assertRelation(Datastore datastore, User user) throws ODKDatastoreException {
		if ( relation == null ) {
			RegisteredUsersTable relationPrototype;
			relationPrototype = new RegisteredUsersTable(datastore.getDefaultSchemaName());
			datastore.assertRelation(relationPrototype, user);
			relation = relationPrototype;
		}
		return relation;
	}
	
	public static final synchronized void bootstrap(String uriSuperUser, Datastore datastore, User user) throws ODKDatastoreException {
		RegisteredUsersTable prototype = assertRelation(datastore, user);
		RegisteredUsersTable entity = null;
		try {
			entity = datastore.getEntity(prototype, uriSuperUser, user);
			if ( !entity.getIsEnabled() ) {
				// make sure superuser can log in with OpenID
				entity.setIsEnabled(true);
				datastore.putEntity(entity, user);
			}
		} catch ( ODKEntityNotFoundException e ) {
			entity = datastore.createEntityUsingRelation(prototype, user);
			entity.setUriUser(uriSuperUser);
			entity.setIsCredentialNonExpired(true);
			entity.setIsEnabled(true);
			datastore.putEntity(entity, user);
		}
	}
}
