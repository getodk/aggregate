package org.opendatakit.common.security.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class GroupGrantedAuthority extends CommonFieldsBase {

	private static final String TABLE_NAME = "_group_granted_authority";

	private static final DataField GROUP = new DataField(
			"GROUP", DataField.DataType.URI, false ).setIndexable(IndexType.HASH);
	private static final DataField GRANTED_AUTHORITY = new DataField(
			"GRANTED_AUTHORITY", DataField.DataType.URI, false );
	
	public final DataField group;
	public final DataField grantedAuthority;

	/*
	 * Property Names for datastore
	 * 
	 * GROUP
	 * GRANTED
	 */

	/**
	 * Construct a relation prototype.
	 * 
	 * @param schemaName
	 */
	GroupGrantedAuthority(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(group = new DataField(GROUP));
		fieldList.add(grantedAuthority = new DataField(GRANTED_AUTHORITY));
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	public GroupGrantedAuthority(GroupGrantedAuthority ref, User user) {
		super(ref, user);
		group = ref.group;
		grantedAuthority = ref.grantedAuthority;
	}

	// Only called from within the persistence layer.
	@Override
	public GroupGrantedAuthority getEmptyRow(User user) {
		return new GroupGrantedAuthority(this, user);
	}

	public final String getGroup() {
		return this.getStringField(group);
	}
	
	public final GrantedAuthority getGrantedAuthority() {
		return new GrantedAuthorityImpl(this.getStringField(grantedAuthority));
	}

	private static GroupGrantedAuthority reference = null;

	public static final synchronized GroupGrantedAuthority createRelation(Datastore ds, User user)
			throws ODKDatastoreException {
		if (reference == null) {
			GroupGrantedAuthority referencePrototype;
			// create the reference prototype using the schema of the form data
			// model object
			referencePrototype = new GroupGrantedAuthority(ds.getDefaultSchemaName());
			ds.assertRelation(referencePrototype, user);
			reference = referencePrototype;
		}
		return reference;
	}

	static final Set<GrantedAuthority> getGrantedAuthorities(Collection<GrantedAuthority> groups, Datastore ds, User user) {
		Set<GrantedAuthority> authorized = new HashSet<GrantedAuthority>();
		
		if ( !groups.isEmpty() ) {
			try {
				Query q = ds.createQuery(createRelation(ds, user), user);
				List<String> groupStrings = new ArrayList<String>();
				for ( GrantedAuthority g : groups ) {
					groupStrings.add(g.getAuthority());
				}
				q.addValueSetFilter(reference.group, groupStrings);
				List<?> values= q.executeDistinctValueForDataField(reference.grantedAuthority);
				for ( Object value : values ) {
					authorized.add(new GrantedAuthorityImpl((String) value));
				}
			} catch (ODKDatastoreException e) {
				e.printStackTrace();
			}
		}
		return authorized;
	}
}
