package org.opendatakit.aggregate.odktables;

import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * This is my attempt to make a datastore that will persist on the server. In many ways
 * it appears to mirror What the.dylan.price@gmail.com has done in DataManager. This
 * wasn't working, however, and it seemed that the best way to get started was to
 * take a stab at it myself. After discussing with Mitch, it looked like ServerPreferences.java
 * was a good example of how to interact with the datastore.
 *
 * Hmm. Unless Dylan's DataManager was to manage the data of the INDIVIDUAL TABLES. Hmm.
 *
 * I believe this differs from the.dylan.price's DataManager class because it attempts to extend
 * CommonFieldsBase, which Mitch has said is a higher performing but more complicated means of
 * persisting on the server. I am using this one because of this possible benefit, although it might
 * not matter to my applications, and because there are more examples of how it works.
 *
 * CommonFieldsBase works more or less as follows, I believe: It is the way into the abstraction layer
 * that you must use to access the database. Any table must extend CommonFieldsBase, as must
 * any row. A table then has a "prototype," that is an empty row of the available columns,
 * and is basically schema for the table. You modify the table by getting an empty row, which
 * is basically the prototype. You then modify the contents and call put() to put the row back
 * into the datastore. put() is idempotent, so it's irrelevant if you are modifying, inserting,
 * have already done it and are doing it again, etc. This is all managed for you underneath the covers.
 *
 *
 * So this table will manage the users of the table. Confusingly, at the moment the "add user"
 * button is in fact an add admin button, as from what I can tell all users on the servers are
 * considered admins.
 *
 * NB: I am not sure if this is what we want the main table in the datastore to be for ODK Tables.
 * Perhaps more importantly, I should probably define an ODK USER class somewhere with all
 * the things necessary for the.dylan.price's system.
 *
 * NB: be sure to update the restrictions on the setter's params once I finalize them.
 *
 *
 * @author sudar.sam@gmail.com
 *
 */
public class DataStoreUserData extends CommonFieldsBase {

	// This will be the name of the table as it exists in the datastore.
	private static final String TABLE_NAME = "_odktables_users";

	/*
	 * So I think that this should, atm, really just house the user info...
	 * And I'll want name, UserID.
	 */

	/**
	 * Field for the aggregate userid of an OdkTables user.
	 * Must be a string less than 250 characters.
	 * Cannot be null. **can now, testing
	 * Any exceptions are assumed to be handled by the persistence layer.
	 * NB: unsure of differences with USER_ID_EXTERNAL
	 */
	//private static final DataField USER_ID_AGGREGATE = new DataField("USER_ID_AGGREGATE",
	//		DataField.DataType.STRING, true);

	/**
	 * Field for the external user id for the user.
	 * Must be a string less than 250 characters.
	 * Can be null.
	 * Any exception are assumed to be handled by the persistence layer.
	 * NB: Unsure of differences with USER_ID_AGGREGATE
	 */
	private static final DataField USER_ID_EXTERNAL = new DataField("USER_ID_EXTERNAL",
			DataField.DataType.STRING, true);

	/**
	 * Field for the user name of an OdkTables user.
	 * Must be a string less than 250 characters.
	 * Can be null.
	 * Any exceptions are assumed to be handled by the persistence layer.
	 */
	private static final DataField USER_NAME = new DataField("NAME",
			DataField.DataType.STRING, true);


	/**
	* Construct a relation prototype. It will load a table into the
	* data store layer.
	*
	* @param databaseSchema
	* @param tableName
	*/
	private DataStoreUserData(String schemaName) {
		super(schemaName, TABLE_NAME);
		//fieldList.add(USER_ID_AGGREGATE);
		fieldList.add(USER_ID_EXTERNAL);
		fieldList.add(USER_NAME);
	}


	/**
	* Construct an empty entity. Only called via {@link #getEmptyRow(User)}
	*
	* @param ref
	* @param user
	*/
	private DataStoreUserData(DataStoreUserData ref, User user) {
		super(ref, user);
	}

	/**
	 * I'm pretty sure this is returning  the prototype, or the empty row.
	 *
	 */
	@Override
	public CommonFieldsBase getEmptyRow(User user) {
		return new DataStoreUserData(this, user);
	}

	/**
	 * I copied this from ServerPreferences. I believe this is how you actually add
	 * the data into the table, making it "persist."
	 * @param cc
	 * 		so you have information about the call
	 * @throws ODKEntityPersistException
	 * @throws ODKOverQuotaException
	 */
	public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();

		ds.putEntity(this, user);
	}

	/**
	 * This is the actual prototype. This is the canonical empty row for this
	 * table that essentially serves as the schema. Therefore the table is nothing
	 * without this relation being initiated.
	 *
	 * For that reason, it is important to always access the prototype by
	 * calling assertRelation() before trying to manipulate the table.
	 * Otherwise you might end up with a table that is empty and shapeless.
	 */
	private static DataStoreUserData relation = null;

	/**
	 * This must be called to ensure that the datamodel for the table has been
	 * initiated.
	 * @param cc
	 * 		calling context that allows the datastore and user to be determined
	 * @return
	 * 		the prototype, eg the canonical empty row for the table
	 * @throws ODKDatastoreException
	 */
	public static synchronized final DataStoreUserData assertRelation(CallingContext cc)
			throws ODKDatastoreException {
		if (relation == null) {
			DataStoreUserData relationPrototype;
			Datastore ds = cc.getDatastore();
			User user = cc.getUserService().getDaemonAccountUser();
			relationPrototype = new DataStoreUserData(ds.getDefaultSchemaName());
			ds.assertRelation(relationPrototype, user); // may throw exception...
			// at this point, the prototype has become fully populated
			relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}

	// Here begin the setters and getters. Access using CommonFieldBase methods.

	/**
	 * Get the aggregate userid.
	 */
	//public String getUserIdAggregate() {
	//	return getStringField(USER_ID_AGGREGATE);
	//}

	/**
	 * Get the external userid
	 */
	public String getUserIdExternal() {
		return getStringField(USER_ID_EXTERNAL);
	}

	/**
	 * Get the user's name.
	 */
	public String getUserName() {
		return getStringField(USER_NAME);
	}

	/**
	 * Set the aggregate userid.
	 * @ throws
	 * 		IllegalArgumentException if the value cannot be set, most likely due to
	 * 		overflow
	 */
	//public void setUserIdAggregate(String aggregateUID) {
	//	if (!setStringField(USER_ID_AGGREGATE, aggregateUID)) {
	//		throw new IllegalArgumentException("overflow aggregate userid");
	//	}
	//}

	/**
	 * Set the external userid.
	 * @ throws
	 * 		IllegalArgumentException if the value cannot be set, most likely due to
	 * 		overflow.
	 */
	public void setUserIdExternal(String externalUID) {
		if (!setStringField(USER_ID_EXTERNAL, externalUID)) {
			throw new IllegalArgumentException("overflow external userid");
		}
	}

	/**
	 * Set the user name.
	 * @param userName
	 * 			The user's name. Must be less than 250 characters, can be null.
	 */
	public void setUserName(String userName) {
		if (!setStringField(USER_NAME, userName)) {
			throw new IllegalArgumentException("over flow user name");
		}
	}

	/**
	 * Add all the data for an ODK Tables User. Uses the individual setters
	 * in this class for name and external user id. Must set the aggregate user id
	 * (the key in the datastore) separately.
	 * @param newUser
	 * 			the user to be added. Although this is an OdkTablesAdmin object,
	 * 			I believe that it represents any user in the OdkTables system.
	 */
	public void setNewUserData(OdkTablesAdmin newUser) {
		setUserIdExternal(newUser.getExternalUid());
		setUserName(newUser.getName());
	}

}
