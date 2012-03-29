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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Persistence object used by {@link RoleHierarchyImpl} to maintain the hierarchy tree of
 * granted authorities.   The setters and getters of this class are private
 * because it is critically important that the security layer know about
 * any inserts/updates to the persistence layer.  
 * <p>Only use the static methods to manipulate this data!
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class GrantedAuthorityHierarchyTable extends CommonFieldsBase {
	private static final String TABLE_NAME = "_granted_authority_hierarchy";

	private static final DataField DOMINATING_GRANTED_AUTHORITY = new DataField(
			"DOMINATING_GRANTED_AUTHORITY", DataField.DataType.URI, false ).setIndexable(IndexType.HASH);
	private static final DataField SUBORDINATE_GRANTED_AUTHORITY = new DataField(
			"SUBORDINATE_GRANTED_AUTHORITY", DataField.DataType.URI, false );

	/**
	 * Construct a relation prototype. Only called via {@link #assertRelation(Datastore, User)}
	 * 
	 * @param schemaName
	 */
	GrantedAuthorityHierarchyTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(DOMINATING_GRANTED_AUTHORITY);
		fieldList.add(SUBORDINATE_GRANTED_AUTHORITY);
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	public GrantedAuthorityHierarchyTable(GrantedAuthorityHierarchyTable ref, User user) {
		super(ref, user);
	}

	// Only called from within the persistence layer.
	@Override
	public GrantedAuthorityHierarchyTable getEmptyRow(User user) {
		return new GrantedAuthorityHierarchyTable(this, user);
	}

	private final GrantedAuthority getDominatingGrantedAuthority() {
		return new SimpleGrantedAuthority(getStringField(DOMINATING_GRANTED_AUTHORITY));
	}
	
	private final void setDominatingGrantedAuthority(String name) {
		if ( ! setStringField(DOMINATING_GRANTED_AUTHORITY, name)) {
			throw new IllegalStateException("overflow dominatingGrantedAuthority");
		}
	}
	
	private final GrantedAuthority getSubordinateGrantedAuthority() {
		return new SimpleGrantedAuthority(getStringField(SUBORDINATE_GRANTED_AUTHORITY));
	}

	private final void setSubordinateGrantedAuthority(String name) {
		if ( ! setStringField(SUBORDINATE_GRANTED_AUTHORITY, name)) {
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
	
	public static final Set<GrantedAuthority> getSubordinateGrantedAuthorities( 
											GrantedAuthority dominantGrant, CallingContext cc ) throws ODKDatastoreException {
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		GrantedAuthorityHierarchyTable relation;
		List<? extends CommonFieldsBase> groupsList;
		relation = GrantedAuthorityHierarchyTable.assertRelation(ds, user);
		Query query = ds.createQuery(relation, "GrantedAuthorityHierarchyTable.getSubordinateGrantedAuthorities", user);
		query.addFilter(GrantedAuthorityHierarchyTable.DOMINATING_GRANTED_AUTHORITY, 
							FilterOperation.EQUAL, dominantGrant.getAuthority() );
		groupsList = query.executeQuery();

		// construct the set of groups that this group directly inherits from
		Set<GrantedAuthority> groups = new HashSet<GrantedAuthority>();
		for ( CommonFieldsBase b : groupsList ) {
			GrantedAuthorityHierarchyTable t = (GrantedAuthorityHierarchyTable) b;
			groups.add(t.getSubordinateGrantedAuthority());
		}
		
		return groups;
	}
	
	
	public static final TreeSet<String> getAllPermissionsAssignableGrantedAuthorities( Datastore ds, User user ) throws ODKDatastoreException {
		
		GrantedAuthorityHierarchyTable relation;
		relation = GrantedAuthorityHierarchyTable.assertRelation(ds, user);
		Query query;

		TreeSet<String> assignableGroups = new TreeSet<String>();

		{
			List<?> domGroupsList;
			query = ds.createQuery(relation, "GrantedAuthorityHierarchyTable.getAllPermissionsAssignableGrantedAuthorities", user);
			domGroupsList = query.executeDistinctValueForDataField(
					GrantedAuthorityHierarchyTable.DOMINATING_GRANTED_AUTHORITY);
	
			for ( Object o : domGroupsList ) {
				String groupName = (String) o;
				if ( !GrantedAuthorityName.permissionsCanBeAssigned(groupName) ) continue;
				assignableGroups.add(groupName);
			}
		}

		{
			List<?> subGroupsList;
			query = ds.createQuery(relation, "GrantedAuthorityHierarchyTable.getAllPermissionsAssignableGrantedAuthorities", user);
			subGroupsList = query.executeDistinctValueForDataField(
					GrantedAuthorityHierarchyTable.SUBORDINATE_GRANTED_AUTHORITY);
			
			for ( Object o : subGroupsList ) {
				String groupName = (String) o;
				if ( !GrantedAuthorityName.permissionsCanBeAssigned(groupName) ) continue;
				assignableGroups.add(groupName);
			}
		}
		
		return assignableGroups;
	}
	
	public static final TreeMap<String, TreeSet<String>> getEntireGrantedAuthorityHierarchy( Datastore ds, User user ) throws ODKDatastoreException {

		GrantedAuthorityHierarchyTable relation;
		List<? extends CommonFieldsBase> groupsList;
		relation = GrantedAuthorityHierarchyTable.assertRelation(ds, user);
		Query query = ds.createQuery(relation, "GrantedAuthorityHierarchyTable.getEntireGrantedAuthorityHierarchy", user);
		query.addSort(GrantedAuthorityHierarchyTable.DOMINATING_GRANTED_AUTHORITY, Direction.ASCENDING);
		groupsList = query.executeQuery();

		TreeMap<String, TreeSet<String>> inheritFrom = new TreeMap<String, TreeSet<String>>();
		for ( CommonFieldsBase b : groupsList ) {
			GrantedAuthorityHierarchyTable group = (GrantedAuthorityHierarchyTable) b;
			
			GrantedAuthority dom = group.getDominatingGrantedAuthority();
			GrantedAuthority sub = group.getSubordinateGrantedAuthority();
			
			if ( !GrantedAuthorityName.permissionsCanBeAssigned(dom.toString()) ) continue;
			TreeSet<String> auths = inheritFrom.get(dom.getAuthority());
			if ( auths == null ) {
				auths = new TreeSet<String>();
				inheritFrom.put(dom.getAuthority(), auths);
			}
			auths.add(sub.getAuthority());
		}

		return inheritFrom;
	}

	public static final void assertGrantedAuthorityHierarchy( GrantedAuthority dominantGrant, 
						Collection<String> desiredGrants, CallingContext cc ) throws ODKDatastoreException {
		
		if ( !GrantedAuthorityName.permissionsCanBeAssigned(dominantGrant.getAuthority()) ) {
			throw new IllegalArgumentException("Dominant grant must be permissions-assignable!");
		}

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		
		boolean hasNotChanged = true;
		
		try {
			GrantedAuthorityHierarchyTable relation = GrantedAuthorityHierarchyTable.assertRelation(ds, user);
			
			TreeSet<String> groups = new TreeSet<String>();
			TreeSet<String> roles = new TreeSet<String>();
			for ( String grant : desiredGrants ) {
				if ( !GrantedAuthorityName.permissionsCanBeAssigned(grant) ) {
					roles.add(grant);
				} else {
					groups.add(grant);
				}
			}

			// get the hierarchy as currently defined for this group 
			List<? extends CommonFieldsBase> groupsList;
			relation = GrantedAuthorityHierarchyTable.assertRelation(ds, user);
			Query query = ds.createQuery(relation, "GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy", user);
			query.addFilter(GrantedAuthorityHierarchyTable.DOMINATING_GRANTED_AUTHORITY, FilterOperation.EQUAL, dominantGrant.getAuthority());
			groupsList = query.executeQuery();

			// OK we have the groups and roles to establish for this dominantGrant.
			// AND we have the groupsList of groups and roles already established for dominantGrant.
			List<EntityKey> deleted = new ArrayList<EntityKey>();
			for ( CommonFieldsBase b : groupsList ) {
				GrantedAuthorityHierarchyTable t = (GrantedAuthorityHierarchyTable) b;
				String authority = t.getSubordinateGrantedAuthority().getAuthority();
				if ( groups.contains(authority) ) {
					groups.remove(authority);
				} else if ( roles.contains(authority) ) {
					roles.remove(authority);
				} else {
					deleted.add(t.getEntityKey());
				}
			}
			// we now have the list of groups and roles to insert, and the list of 
			// existing records to delete...
			List<GrantedAuthorityHierarchyTable> added = new ArrayList<GrantedAuthorityHierarchyTable>();
			for ( String group : groups ) {
				GrantedAuthorityHierarchyTable t = ds.createEntityUsingRelation(relation, user);
				t.setDominatingGrantedAuthority(dominantGrant.getAuthority());
				t.setSubordinateGrantedAuthority(group);
				added.add(t);
			}
			
			for ( String role : roles ) {
				GrantedAuthorityHierarchyTable t = ds.createEntityUsingRelation(relation, user);
				t.setDominatingGrantedAuthority(dominantGrant.getAuthority());
				t.setSubordinateGrantedAuthority(role);
				added.add(t);
			}
			
			hasNotChanged = added.isEmpty() && deleted.isEmpty();
			
			// we now have the list of EntityKeys to delete, and the list of records to add -- do it.
			ds.putEntities(added, user);
			ds.deleteEntities(deleted, user);
		} finally {
			if ( !hasNotChanged ) {
				// finally, since we mucked with the group hierarchies, flag that 
				// the cache of those hierarchies has changed.
				SecurityRevisionsTable.setLastRoleHierarchyRevisionDate(ds, user);
			}
		}
	}
}
