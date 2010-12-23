package org.opendatakit.common.security.spring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public final class UserGrantedAuthority extends CommonFieldsBase {

	private static final String TABLE_NAME = "_user_granted_authority";

	/*
	 * Property Names for datastore
	 */
	private static final DataField USER = new DataField(
			"USER", DataField.DataType.URI, false ).setIndexable(IndexType.ORDERED);
	private static final DataField GRANTED_AUTHORITY = new DataField(
			"GRANTED_AUTHORITY", DataField.DataType.URI, false );
	
	public final DataField user;
	public final DataField grantedAuthority;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param schemaName
	 */
	UserGrantedAuthority(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(user = new DataField(USER));
		fieldList.add(grantedAuthority = new DataField(GRANTED_AUTHORITY));
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	UserGrantedAuthority(UserGrantedAuthority ref, User user) {
		super(ref, user);
		this.user = ref.user;
		grantedAuthority = ref.grantedAuthority;
	}

	// Only called from within the persistence layer.
	@Override
	public UserGrantedAuthority getEmptyRow(User user) {
		return new UserGrantedAuthority(this, user);
	}

	public final String getUser() {
		return this.getStringField(user);
	}
	
	public final GrantedAuthority getGrantedAuthority() {
		return new GrantedAuthorityImpl(this.getStringField(grantedAuthority));
	}

	private static UserGrantedAuthority reference = null;

	public static synchronized final UserGrantedAuthority createRelation(Datastore ds, User user)
			throws ODKDatastoreException {
		if (reference == null) {
			UserGrantedAuthority referencePrototype;
			// create the reference prototype using the schema of the form data
			// model object
			referencePrototype = new UserGrantedAuthority(ds.getDefaultSchemaName());
			ds.assertRelation(referencePrototype, user);
			reference = referencePrototype;
		}
		return reference;
	}

	static final Set<GrantedAuthority> getGrantedAuthorities(String uriUser, Datastore ds, User user) throws ODKDatastoreException {
		Set<GrantedAuthority> authorized = new HashSet<GrantedAuthority>();
		
		if ( uriUser != null ) {
			Query q = ds.createQuery(createRelation(ds, user), user);
			q.addFilter(reference.user, FilterOperation.EQUAL, uriUser);
			List<?> values= q.executeDistinctValueForDataField(reference.grantedAuthority);
			for ( Object value : values ) {
				authorized.add(new GrantedAuthorityImpl((String) value));
			}
		}
		return authorized;
	}
}
