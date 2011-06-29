package org.opendatakit.common.security.spring;

import java.util.Date;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

public class SecurityRevisionsTable extends CommonFieldsBase {

	private static final String ROW_ID = "rid:0";
	
	private static final String TABLE_NAME = "_security_revisions";
	
	private static final DataField LAST_ROLE_HIERARCHY_REVISION_DATE = new DataField(
			"LAST_ROLE_HIERARCHY_REVISION", DataField.DataType.DATETIME, true );
	
	private static final DataField LAST_REGISTERED_USERS_REVISION_DATE = new DataField(
			"LAST_REGISTERED_USER_REVISION", DataField.DataType.DATETIME, true );
	
	
	/**
	 * Construct a relation prototype.  Only called via {@link #assertRelation(Datastore, User)}
	 * 
	 * @param schemaName
	 */
	protected SecurityRevisionsTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(LAST_ROLE_HIERARCHY_REVISION_DATE);
		fieldList.add(LAST_REGISTERED_USERS_REVISION_DATE);
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

	public Date getLastRoleHierarchyRevisionDate() {
		return getDateField(LAST_ROLE_HIERARCHY_REVISION_DATE);
	}
	
	public void setLastRoleHierarchyRevisionDate(Date value) {
		setDateField(LAST_ROLE_HIERARCHY_REVISION_DATE, value);
	}
	
	public Date getLastRegisteredUsersRevisionDate() {
		return getDateField(LAST_REGISTERED_USERS_REVISION_DATE);
	}
	
	public void setLastRegisteredUsersRevisionDate(Date value) {
		setDateField(LAST_REGISTERED_USERS_REVISION_DATE, value);
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
	private static synchronized SecurityRevisionsTable assertRelation(Datastore datastore, User user) throws ODKDatastoreException {
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
	public static synchronized final SecurityRevisionsTable getSingletonRecord(Datastore datastore, User user) throws ODKDatastoreException {
		SecurityRevisionsTable prototype = assertRelation(datastore, user);
		SecurityRevisionsTable record = null;
		try {
			record = datastore.getEntity(prototype, ROW_ID, user);
		} catch ( ODKEntityNotFoundException e ) {
			record = datastore.createEntityUsingRelation(prototype, user);
			record.setLastRegisteredUsersRevisionDate(new Date());
			record.setLastRoleHierarchyRevisionDate(new Date());
			datastore.putEntity(record, user);
		}
		return record;
	}
}
