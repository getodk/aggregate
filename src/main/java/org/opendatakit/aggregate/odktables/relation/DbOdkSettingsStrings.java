package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This will be a table in the datastore that stores strings
 * to be associated with tables. This includes the settings string
 * that will hold information about how to display the spreadsheet
 * information on the phone.
 * <br>
 * This is modeled off of DbColumn.java.
 * ...although in actuality this might be exactly what 
 * DbTableProperties currently accomplishes, so for now
 * I'm going to let this sit.
 * @author sudar.sam@gmail.com
 *
 */
public class DbOdkSettingsStrings {

	// these are the column names in the table
	public static final String TABLE_ID = "TABLE_ID";
	public static final String KEY = "KEY";
	// is this how i want to be calling this?
	public static final String ETAG = "ETAG";
	public static final String STRING = "STRING";
	
	// this will be appended to the end of the table as the name
	public static final String RELATION_NAME = "TABLE_STRINGS";
	
	private static final List<DataField> dataFields;
	
	/*
	 * the static block does it all once. supposed to be more efficient.
	 */
	static {
		dataFields = new ArrayList<DataField>();
		dataFields.add(new DataField(TABLE_ID, DataType.STRING, false).setIndexable(IndexType.HASH));
		dataFields.add(new DataField(KEY, DataType.STRING, false));
		dataFields.add(new DataField(ETAG, DataType.STRING, false));
		dataFields.add(new DataField(STRING, DataType.STRING, false));
	}
	
	public static Relation getRelation(CallingContext cc) throws ODKDatastoreException {
		Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
		return relation;
	}
	
	/**
	 * Get all the entities (rows) in this table for the given tableId.
	 * @param tableId
	 * @param cc
	 * @return
	 * @throws ODKDatastoreException
	 */
	public static List<Entity> query(String tableId, CallingContext cc) throws
			ODKDatastoreException {
		// the magic string is just for logging.
		return getRelation(cc).query("DbOdkTablesSTring.query()", cc).equal(TABLE_ID, tableId)
				.execute();
	}

	
}
