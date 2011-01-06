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
	private static final String TABLE_NAME = "_registered__users";
	
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

	private final DataField basicAuthPassword;
	private final DataField basicAuthSalt;
	private final DataField digestAuthPassword;
	private final DataField isCredentialNonExpired; // relates to the above credentials
	private final DataField isEnabled;
	
	protected RegisteredUsersTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(basicAuthPassword=new DataField(BASIC_AUTH_PASSWORD));
		fieldList.add(basicAuthSalt=new DataField(BASIC_AUTH_SALT));
		fieldList.add(digestAuthPassword=new DataField(DIGEST_AUTH_PASSWORD));
		fieldList.add(isCredentialNonExpired=new DataField(IS_CREDENTIALS_NON_EXPIRED));
		fieldList.add(isEnabled=new DataField(IS_ENABLED));
		primaryKey.setIndexable(IndexType.ORDERED);
	}
	
	protected RegisteredUsersTable(RegisteredUsersTable ref, User user) {
		super(ref, user);
		basicAuthPassword = ref.basicAuthPassword;
		basicAuthSalt = ref.basicAuthSalt;
		digestAuthPassword = ref.digestAuthPassword;
		isCredentialNonExpired = ref.isCredentialNonExpired;
		isEnabled = ref.isEnabled;
	}

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
	
	public String getBasicAuthPassword() {
		return getStringField(basicAuthPassword);
	}

	public void setBasicAuthPassword(String value) {
		if ( !setStringField(basicAuthPassword, value)) {
			throw new IllegalStateException("overflow basicAuthPassword");
		}
	}
	
	public String getBasicAuthSalt() {
		return getStringField(basicAuthSalt);
	}

	public void setBasicAuthSalt(String value) {
		if ( !setStringField(basicAuthSalt, value)) {
			throw new IllegalStateException("overflow basicAuthSalt");
		}
	}

	public String getDigestAuthPassword() {
		return getStringField(digestAuthPassword);
	}
	
	public void setDigestAuthPassword(String value) {
		if ( !setStringField(digestAuthPassword, value)) {
			throw new IllegalStateException("overflow digestAuthPassword");
		}
	}

	public Boolean getIsCredentialNonExpired() {
		return getBooleanField(isCredentialNonExpired);
	}

	public void setIsCredentialNonExpired(Boolean value) {
		setBooleanField(isCredentialNonExpired, value);
	}

	public Boolean getIsEnabled() {
		return getBooleanField(isEnabled);
	}

	public void setIsEnabled(Boolean value) {
		setBooleanField(isEnabled, value);
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

}
