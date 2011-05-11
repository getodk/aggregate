package org.opendatakit.common.security.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

public final class GroupFormIdGrantedAuthority extends CommonFieldsBase {

	private static final String TABLE_NAME = "_group_form_id_granted_authority";
	
	private static final DataField GROUP = new DataField(
			"GROUP", DataField.DataType.URI, false ).setIndexable(IndexType.HASH);
	private static final DataField FORM_ID = new DataField(
			"FORM_ID", DataField.DataType.STRING, false, 4096L );
	private static final DataField GRANTED_AUTHORITY = new DataField(
			"GRANTED_AUTHORITY", DataField.DataType.URI, false );
	
	/**
	 * Construct a relation prototype. Only called via {@link #assertRelation(Datastore, User)}
	 * 
	 * @param schemaName
	 */
	GroupFormIdGrantedAuthority(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(GROUP);
		fieldList.add(FORM_ID);
		fieldList.add(GRANTED_AUTHORITY);
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	private GroupFormIdGrantedAuthority(GroupFormIdGrantedAuthority ref, User user) {
		super(ref, user);
	}

	// Only called from within the persistence layer.
	@Override
	public GroupFormIdGrantedAuthority getEmptyRow(User user) {
		return new GroupFormIdGrantedAuthority(this, user);
	}

	public final String getGroup() {
		return this.getStringField(GROUP);
	}
	
	public final String getFormId() {
		return this.getStringField(FORM_ID);
	}
	public final GrantedAuthority getGrantedAuthority() {
		return new GrantedAuthorityImpl(this.getStringField(GRANTED_AUTHORITY));
	}

	private static GroupFormIdGrantedAuthority reference = null;

	public static final synchronized GroupFormIdGrantedAuthority assertRelation(Datastore ds, User user)
			throws ODKDatastoreException {
		if (reference == null) {
			// create the reference prototype using the schema of the form data
			// model object
			GroupFormIdGrantedAuthority referencePrototype;
			referencePrototype = new GroupFormIdGrantedAuthority(ds.getDefaultSchemaName());
			ds.assertRelation(referencePrototype, user);
			reference = referencePrototype;
		}
		return reference;
	}

	static final Map<String, Set<GrantedAuthority>> 
	  getAllGrantedAuthorities(Set<GrantedAuthority> groups, Datastore ds, User user) {

		Map<String, Set<GrantedAuthority> > authorized 
				= new HashMap<String, Set<GrantedAuthority>>();
		if ( !groups.isEmpty() ) {
			try {
				Query q = ds.createQuery(assertRelation(ds, user), user);
				List<String> groupStrings = new ArrayList<String>();
				for ( GrantedAuthority g : groups ) {
					groupStrings.add(g.getAuthority());
				}
				q.addValueSetFilter(GROUP, groupStrings);
				List<? extends CommonFieldsBase> values = q.executeQuery(0);
				for ( CommonFieldsBase b : values ) {
					GroupFormIdGrantedAuthority g = (GroupFormIdGrantedAuthority) b;
					String formId = g.getFormId();
					Set<GrantedAuthority> s = authorized.get(formId);
					if ( s == null ) {
						s = new HashSet<GrantedAuthority>();
						authorized.put(formId, s);
					}
					s.add(g.getGrantedAuthority());
				}
			} catch (ODKDatastoreException e) {
				e.printStackTrace();
			}
		}
		return authorized;
	}
}
