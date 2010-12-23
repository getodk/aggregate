package org.opendatakit.common.security.spring;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class GrantedAuthorityHierarchyTable extends CommonFieldsBase {
	private static final String TABLE_NAME = "_granted_authority_hierarchy";

	private static final DataField DOMINATING_GRANTED_AUTHORITY = new DataField(
			"DOMINATING_GRANTED_AUTHORITY", DataField.DataType.URI, false ).setIndexable(IndexType.HASH);
	private static final DataField SUBORDINATE_GRANTED_AUTHORITY = new DataField(
			"SUBORDINATE_GRANTED_AUTHORITY", DataField.DataType.URI, false );
	
	public final DataField dominatingGrantedAuthority;
	public final DataField subordinateGrantedAuthority;

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
	GrantedAuthorityHierarchyTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(dominatingGrantedAuthority = new DataField(DOMINATING_GRANTED_AUTHORITY));
		fieldList.add(subordinateGrantedAuthority = new DataField(SUBORDINATE_GRANTED_AUTHORITY));
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	public GrantedAuthorityHierarchyTable(GrantedAuthorityHierarchyTable ref, User user) {
		super(ref, user);
		dominatingGrantedAuthority = ref.dominatingGrantedAuthority;
		subordinateGrantedAuthority = ref.subordinateGrantedAuthority;
	}

	// Only called from within the persistence layer.
	@Override
	public GrantedAuthorityHierarchyTable getEmptyRow(User user) {
		return new GrantedAuthorityHierarchyTable(this, user);
	}

	public final GrantedAuthority getDominatingGrantedAuthority() {
		return new GrantedAuthorityImpl(getStringField(dominatingGrantedAuthority));
	}
	
	public final void setDominatingGrantedAuthority(GrantedAuthorityNames name) {
		if ( ! setStringField(dominatingGrantedAuthority, name.name())) {
			throw new IllegalStateException("overflow dominatingGrantedAuthority");
		}
	}
	
	public final GrantedAuthority getSubordinateGrantedAuthority() {
		return new GrantedAuthorityImpl(getStringField(subordinateGrantedAuthority));
	}

	public final void setSubordinateGrantedAuthority(GrantedAuthorityNames name) {
		if ( ! setStringField(subordinateGrantedAuthority, name.name())) {
			throw new IllegalStateException("overflow subordinateGrantedAuthority");
		}
	}
	
	private static GrantedAuthorityHierarchyTable reference = null;

	public static final synchronized GrantedAuthorityHierarchyTable assertRelation(Datastore ds, User user)
			throws ODKDatastoreException {
		if (reference == null) {
			GrantedAuthorityHierarchyTable referencePrototype;
			// create the reference prototype using the schema of the form data
			// model object
			referencePrototype = new GrantedAuthorityHierarchyTable(ds.getDefaultSchemaName());
			ds.assertRelation(referencePrototype, user);
			reference = referencePrototype;
		}
		return reference;
	}

	/**
	 * The table is empty.  Populate it with the baseline hierarchy...
	 * This allows everyone to download and submit data, but requires
	 * authentication to create and delete forms, view data, and publish
	 * results.
	 * 
	 * @param datastore
	 * @param user
	 * @throws ODKDatastoreException
	 */
	public static void bootstrap(Datastore datastore, User user) throws ODKDatastoreException {
		GrantedAuthorityHierarchyTable tRelation = assertRelation(datastore, user);
		
		GrantedAuthorityHierarchyTable t;
		List<GrantedAuthorityHierarchyTable> tList = new ArrayList<GrantedAuthorityHierarchyTable>();
		
		GrantedAuthorityNames ra = GrantedAuthorityNames.USER_IS_AUTHENTICATED;
		GrantedAuthorityNames ranon = GrantedAuthorityNames.USER_IS_ANONYMOUS;
		
		// the anonymous user can download forms and upload submissions
		for ( GrantedAuthorityNames name : new GrantedAuthorityNames[] {
					GrantedAuthorityNames.ROLE_FORM_DOWNLOAD,
					GrantedAuthorityNames.ROLE_FORM_LIST,
					GrantedAuthorityNames.ROLE_SUBMISSION_UPLOAD
				} ) {
			t = datastore.createEntityUsingRelation(tRelation, user);
			t.setDominatingGrantedAuthority(ranon);
			t.setSubordinateGrantedAuthority(name);
			tList.add(t);
		}
		// the authenticated user can do everything the
		// anonymous user can, and everything else...
		for ( GrantedAuthorityNames name : new GrantedAuthorityNames[] {
				GrantedAuthorityNames.USER_IS_ANONYMOUS,
				GrantedAuthorityNames.ROLE_USER,
				GrantedAuthorityNames.ROLE_ANALYST,
				GrantedAuthorityNames.ROLE_SERVICES_ADMIN,
				GrantedAuthorityNames.ROLE_XFORMS_ADMIN,
				GrantedAuthorityNames.ROLE_ACCESS_ADMIN
			} ) {
			t = datastore.createEntityUsingRelation(tRelation, user);
			t.setDominatingGrantedAuthority(ra);
			t.setSubordinateGrantedAuthority(name);
			tList.add(t);
		}
		datastore.putEntities(tList, user);
	}
}
