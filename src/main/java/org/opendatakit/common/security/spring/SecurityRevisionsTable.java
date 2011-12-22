package org.opendatakit.common.security.spring;

import java.util.Date;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

public class SecurityRevisionsTable extends CommonFieldsBase {

	private static final String ROLE_HIERARCHY_ROW_ID = "rid:role_hierarchy";
	private static final String REGISTERED_USERS_ROW_ID = "rid:registered_users";
	private static final String SUPER_USER_ID_ROW_ID = "rid:super_user_id";
	private static final String PERMISSIONS_VIEW_ROW_ID = "rid:permissions_view";
	
	private static final String TABLE_NAME = "_security_revisions";
	
	private static final DataField LAST_REVISION_DATE = new DataField(
			"LAST_REVISION", DataField.DataType.DATETIME, true );

	/**
	 * Construct a relation prototype.  Only called via {@link #assertRelation(Datastore, User)}
	 * 
	 * @param schemaName
	 */
	protected SecurityRevisionsTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(LAST_REVISION_DATE);
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	protected SecurityRevisionsTable(SecurityRevisionsTable ref, User user) {
		super(ref, user);
	}

	// Only called from within the persistence layer.
	@Override
	public CommonFieldsBase getEmptyRow(User user) {
		SecurityRevisionsTable t = new SecurityRevisionsTable(this, user);
		return t;
	}

	private Date getLastRevisionDate() {
		return getDateField(LAST_REVISION_DATE);
	}
	
	private void setLastRevisionDate(Date value) {
		setDateField(LAST_REVISION_DATE, value);
	}

	private static SecurityRevisionsTable relation = null;
	
	/**
	 * This is private because this table implements a singleton pattern.
	 * 
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	private static synchronized final SecurityRevisionsTable assertRelation(Datastore datastore, User user) throws ODKDatastoreException {
		if ( relation == null ) {
			SecurityRevisionsTable relationPrototype;
			relationPrototype = new SecurityRevisionsTable(datastore.getDefaultSchemaName());
			datastore.assertRelation(relationPrototype, user);
			relation = relationPrototype;
		}
		return relation;
	}
	
	/**
	 * This retrieves the singleton record.
	 * 
	 * @param uri
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	private static synchronized final SecurityRevisionsTable getSingletonRecord(String uri, Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable prototype = assertRelation(datastore, user);
		SecurityRevisionsTable record = null;
		try {
			record = datastore.getEntity(prototype, uri, user);
		} catch ( ODKEntityNotFoundException e ) {
			record = datastore.createEntityUsingRelation(prototype, user);
			record.setStringField(prototype.primaryKey, uri);
			record.setLastRevisionDate(new Date());
			datastore.putEntity(record, user);
		}
		return record;
	}
	
	public static final long getLastRoleHierarchyRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable t = getSingletonRecord( ROLE_HIERARCHY_ROW_ID, datastore, user );
		return t.getLastRevisionDate().getTime();
	}
	
	public static final void setLastRoleHierarchyRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable t = getSingletonRecord( ROLE_HIERARCHY_ROW_ID, datastore, user );
		t.setLastRevisionDate(new Date());
		datastore.putEntity(t, user);
	}
	
	public static final long getLastRegisteredUsersRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable t = getSingletonRecord( REGISTERED_USERS_ROW_ID, datastore, user );
		return t.getLastRevisionDate().getTime();
	}
	
	public static final void setLastRegisteredUsersRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable t = getSingletonRecord( REGISTERED_USERS_ROW_ID, datastore, user );
		t.setLastRevisionDate(new Date());
		datastore.putEntity(t, user);
	}
	
	public static final long getLastSuperUserIdRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable t = getSingletonRecord( SUPER_USER_ID_ROW_ID, datastore, user );
		return t.getLastRevisionDate().getTime();
	}
	
	public static final void setLastSuperUserIdRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable t = getSingletonRecord( SUPER_USER_ID_ROW_ID, datastore, user );
		t.setLastRevisionDate(new Date());
		datastore.putEntity(t, user);
	}
	
	public static final long getLastPermissionsViewRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable t = getSingletonRecord( PERMISSIONS_VIEW_ROW_ID, datastore, user );
		return t.getLastRevisionDate().getTime();
	}
	
	public static final void setLastPermissionsViewRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable t = getSingletonRecord( PERMISSIONS_VIEW_ROW_ID, datastore, user );
		t.setLastRevisionDate(new Date());
		datastore.putEntity(t, user);
	}
}
