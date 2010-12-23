package org.opendatakit.common.security.spring;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

public final class RegisteredUsersTable extends CommonFieldsBase {
	private static final String TABLE_NAME = "_registered_users";
	
	private static final DataField BASIC_AUTH_PASSWORD = new DataField(
			"BASIC_AUTH_PASSWORD", DataField.DataType.STRING, true );

	private static final DataField DIGEST_AUTH_PASSWORD = new DataField(
			"DIGEST_AUTH_PASSWORD", DataField.DataType.STRING, true );

	private static final DataField IS_ACCOUNT_NON_EXPIRED = new DataField(
			"IS_ACCOUNT_NON_EXPIRED", DataField.DataType.BOOLEAN, false );

	private static final DataField IS_ACCOUNT_NON_LOCKED = new DataField(
			"IS_ACCOUNT_NON_LOCKED", DataField.DataType.BOOLEAN, false );
	
	private static final DataField IS_CREDENTIALS_NON_EXPIRED = new DataField(
			"IS_CREDENTIALS_NON_EXPIRED", DataField.DataType.BOOLEAN, false );

	private static final DataField IS_ENABLED = new DataField(
			"IS_ENABLED", DataField.DataType.BOOLEAN, false );

	private final DataField basicAuthPassword;
	private final DataField digestAuthPassword;
	private final DataField isAccountNonExpired;
	private final DataField isAccountNonLocked;
	private final DataField isCredentialNonExpired;
	private final DataField isEnabled;
	
	protected RegisteredUsersTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(basicAuthPassword=new DataField(BASIC_AUTH_PASSWORD));
		fieldList.add(digestAuthPassword=new DataField(DIGEST_AUTH_PASSWORD));
		fieldList.add(isAccountNonExpired=new DataField(IS_ACCOUNT_NON_EXPIRED));
		fieldList.add(isAccountNonLocked=new DataField(IS_ACCOUNT_NON_LOCKED));
		fieldList.add(isCredentialNonExpired=new DataField(IS_CREDENTIALS_NON_EXPIRED));
		fieldList.add(isEnabled=new DataField(IS_ENABLED));
		primaryKey.setIndexable(IndexType.ORDERED);
	}
	
	protected RegisteredUsersTable(RegisteredUsersTable ref, User user) {
		super(ref, user);
		basicAuthPassword = ref.basicAuthPassword;
		digestAuthPassword = ref.digestAuthPassword;
		isAccountNonExpired = ref.isAccountNonExpired;
		isAccountNonLocked = ref.isAccountNonLocked;
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

	public String getDigestAuthPassword() {
		return getStringField(digestAuthPassword);
	}
	
	public void setDigestAuthPassword(String value) {
		if ( !setStringField(digestAuthPassword, value)) {
			throw new IllegalStateException("overflow digestAuthPassword");
		}
	}

	public Boolean getIsAccountNonExpired() {
		return getBooleanField(isAccountNonExpired);
	}

	public Boolean getIsAccountNonLocked() {
		return getBooleanField(isAccountNonLocked);
	}

	public Boolean getIsCredentialNonExpired() {
		return getBooleanField(isCredentialNonExpired);
	}

	public Boolean getIsEnabled() {
		return getBooleanField(isEnabled);
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
