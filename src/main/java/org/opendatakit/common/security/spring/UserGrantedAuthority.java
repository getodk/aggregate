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

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Persistence layer object for recording the authorities directly granted to 
 * a registered user.  The setters and getters of this class are private
 * because it is critically important that the security layer know about
 * any inserts/updates to the persistence layer.  
 * <p>Only use the static methods to manipulate this data!
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class UserGrantedAuthority extends CommonFieldsBase {

	private static final String TABLE_NAME = "_user_granted_authority";

	/*
	 * Property Names for datastore
	 */
	private static final DataField USER = new DataField(
			"USER", DataField.DataType.URI, false ).setIndexable(IndexType.ORDERED);
	private static final DataField GRANTED_AUTHORITY = new DataField(
			"GRANTED_AUTHORITY", DataField.DataType.URI, false );

	/**
	 * Construct a relation prototype.  Only called via {@link #assertRelation(Datastore, User)}
	 * 
	 * @param schemaName
	 */
	UserGrantedAuthority(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(USER);
		fieldList.add(GRANTED_AUTHORITY);
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	UserGrantedAuthority(UserGrantedAuthority ref, User user) {
		super(ref, user);
	}

	// Only called from within the persistence layer.
	@Override
	public UserGrantedAuthority getEmptyRow(User user) {
		return new UserGrantedAuthority(this, user);
	}

	private final String getUser() {
		return this.getStringField(USER);
	}

	private final void setUser(String value) {
		if ( !this.setStringField(USER, value)) {
			throw new IllegalStateException("overflow user");
		}
	}
	
	private final GrantedAuthority getGrantedAuthority() {
		return new SimpleGrantedAuthority(this.getStringField(GRANTED_AUTHORITY));
	}
	
	private final void setGrantedAuthority(GrantedAuthority value) {
		if ( !this.setStringField(GRANTED_AUTHORITY, value.getAuthority())) {
			throw new IllegalStateException("overflow grantedAuthority");
		}
	}

	private static UserGrantedAuthority reference = null;

	public static synchronized final UserGrantedAuthority assertRelation(Datastore ds, User user)
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

	public static final Set<GrantedAuthority> getGrantedAuthorities(String uriUser, Datastore ds, User user) throws ODKDatastoreException {
		Set<GrantedAuthority> authorized = new HashSet<GrantedAuthority>();
		
		if ( uriUser != null ) {
			Query q = ds.createQuery(assertRelation(ds, user), "UserGrantedAuthority.getGrantedAuthorities", user);
			q.addFilter(USER, FilterOperation.EQUAL, uriUser);
			List<?> values = q.executeDistinctValueForDataField(GRANTED_AUTHORITY);
			for ( Object value : values ) {
				authorized.add(new SimpleGrantedAuthority((String) value));
			}
		}
		return authorized;
	}
	
	/**
	 * Only infrequently used for group membership management.  
	 * 
	 * @param auth
	 * @param ds
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static final Set<String> getUriUsers(GrantedAuthority auth, Datastore ds, User user) throws ODKDatastoreException {
		Set<String> users = new HashSet<String>();
		if ( auth != null ) {
			Query q = ds.createQuery(assertRelation(ds, user), "UserGrantedAuthority.getUriUsers", user);
			q.addFilter(GRANTED_AUTHORITY, FilterOperation.EQUAL, auth.getAuthority());
			List<?> values = q.executeDistinctValueForDataField(USER);
			for ( Object value : values ) {
				users.add((String) value);
			}
		}
		return users;
	}
	
	/**
	 * Asserts that the given group has exactly the list of desired members and 
	 * no additional members.
	 *  
	 * @param group
	 * @param desiredMembers
	 * @param cc
	 * @throws ODKDatastoreException
	 */
	public static final void assertGrantedAuthorityMembers(GrantedAuthority group, 
			Collection<String> desiredMembers, CallingContext cc) throws ODKDatastoreException {

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		boolean hasNotChanged = true;
		
		try {
			UserGrantedAuthority relation = UserGrantedAuthority.assertRelation(ds, user);
			
			// get the members as currently defined for this group 
			List<? extends CommonFieldsBase> membersList;
			Query query = ds.createQuery(relation, "UserGrantedAuthority.assertGrantedAuthorityMembers", user);
			query.addFilter(UserGrantedAuthority.GRANTED_AUTHORITY, FilterOperation.EQUAL, 
							group.getAuthority());
			membersList = query.executeQuery();

			// OK we have the desired and actual members lists for this groupname.
			// find the set of members to remove...
			List<EntityKey> deleted = new ArrayList<EntityKey>();
			for ( CommonFieldsBase b : membersList ) {
				UserGrantedAuthority t = (UserGrantedAuthority) b;
				String uriUser = t.getUser();
				if ( desiredMembers.contains(uriUser) ) {
					desiredMembers.remove(uriUser);
				} else {
					deleted.add(t.getEntityKey());
				}
			}
			// we now have the list of desiredMembers to insert, and the list of 
			// existing records to delete...
			List<UserGrantedAuthority> added = new ArrayList<UserGrantedAuthority>();
			for ( String uriUser : desiredMembers ) {
				UserGrantedAuthority t = ds.createEntityUsingRelation(relation, user);
				t.setUser(uriUser);
				t.setGrantedAuthority(group);
				added.add(t);
			}

			// we have no changes if there are no adds and no deletes 
			hasNotChanged = added.isEmpty() && deleted.isEmpty();
			
			// we now have the list of EntityKeys to delete, and the list of records to add -- do it.
			ds.putEntities(added, user);
			ds.deleteEntities(deleted, user);
		} finally {
			if ( !hasNotChanged ) {
				// we've changed -- reload the permissions tree...
				cc.getUserService().reloadPermissions();
			}
		}
	}
	
	public static final void assertUserGrantedAuthorities(String uriUser, Collection<String> desiredGroups, CallingContext cc) throws ODKDatastoreException {

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		
		boolean hasNotChanged = true;
		
		try {
			UserGrantedAuthority relation = UserGrantedAuthority.assertRelation(ds, user);
			
			// get the members as currently defined for this group 
			List<? extends CommonFieldsBase> groupsList;
			Query query = ds.createQuery(relation, "UserGrantedAuthority.assertUserGrantedAuthorities", user);
			query.addFilter(UserGrantedAuthority.USER, FilterOperation.EQUAL, uriUser);
			groupsList = query.executeQuery();

			// OK we have the desired and actual groups lists for this username.
			// find the set of groups to remove...
			List<EntityKey> deleted = new ArrayList<EntityKey>();
			for ( CommonFieldsBase b : groupsList ) {
				UserGrantedAuthority t = (UserGrantedAuthority) b;
				String groupName = t.getGrantedAuthority().getAuthority();
				if ( desiredGroups.contains(groupName) ) {
					desiredGroups.remove(groupName);
				} else {
					deleted.add(t.getEntityKey());
				}
			}
			// we now have the list of desiredGroups to insert, and the list of 
			// existing records to delete...
			List<UserGrantedAuthority> added = new ArrayList<UserGrantedAuthority>();
			for ( String group : desiredGroups ) {
				UserGrantedAuthority t = ds.createEntityUsingRelation(relation, user);
				t.setUser(uriUser);
				t.setGrantedAuthority(new SimpleGrantedAuthority(group));
				added.add(t);
			}

			// nothing has changed if there are no adds and no deletes.
			hasNotChanged = added.isEmpty() && deleted.isEmpty();
			
			// we now have the list of EntityKeys to delete, and the list of records to add -- do it.
			ds.putEntities(added, user);
			ds.deleteEntities(deleted, user);
		} finally {
			if ( !hasNotChanged ) {
				// we've changed, so we need to reload permissions tree
				cc.getUserService().reloadPermissions();
			}
		}
	}
	
	public static final void deleteGrantedAuthoritiesForUser(String uriUser, UserService userService, Datastore datastore, User user) throws ODKDatastoreException {

		try {
			UserGrantedAuthority relation = UserGrantedAuthority.assertRelation(datastore, user);
			Query query = datastore.createQuery(relation, "UserGrantedAuthority.deleteGrantedAuthoritiesForUser", user);
			query.addFilter(UserGrantedAuthority.USER, FilterOperation.EQUAL, uriUser);
			List<?> keys = query.executeDistinctValueForDataField(relation.primaryKey);
			List<EntityKey> memberships = new ArrayList<EntityKey>();
			for ( Object o : keys ) {
				String uri = (String) o;
				// we don't have the record that we want to delete; construct
				// the entity key from the relation and the URI for the record.
				memberships.add(new EntityKey(relation, uri));
			}
			datastore.deleteEntities(memberships, user);
		} finally {
			userService.reloadPermissions();
		}
	}
}
