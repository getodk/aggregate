/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.aggregate.odktables.relation;

import org.opendatakit.common.ermodel.AbstractBlobRelationSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This represents the datastore table that holds files for the various
 * ODKTables tables. For instance, each ODKTable might have an html file
 * with information about how to display its contents when ListView or
 * BoxView is selected. Those files will be stored on the server in
 * this table.
 * <p>
 * this is based on DbColumn.java.
 * <p>
 * These files are going to be stored using an AbstractBlobRelationSet.
 * This handles most of the mechanics of storing arbitrarily large
 * binary files. It is based on BlobRelationSetTest.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class DbTableFiles extends AbstractBlobRelationSet {

	// the name of the whole relation set, and the String that
	// precedes the underscore extensions.
	private static final String BLOB_RELATION_NAME = "TABLEFILES";

	// i had this private...why? should i keep it that way?
	public DbTableFiles(CallingContext cc) throws ODKDatastoreException {
		super(BLOB_RELATION_NAME, cc);
	}

	/*
	public static DbTableFiles getBlobRelationSet(CallingContext cc) throws
			ODKDatastoreException {
		return new DbTableFiles(cc);
	}*/




	/*
	/*** these will be the column names in the datastore table ***
	public static final String TABLE_ID = "TABLE_ENTRY_ID";
	// this will house the different key possibilities, such as
	// listview, boxview, etc.
	public static final String FILE_KEY = "FILE_KEY";
	// will house the actual file.
	public static final String FILE = "FILE";


	// name of the table
	private static final String RELATION_NAME = "TABLE_FILES";

	// the "columns" themselves. columns are fields within an
	// entity (row).
	private static final List<DataField> dataFields;

	// do the one time constructor stuff, but do it in a static block
	// so it is more efficient. a la Dylan in DbColumn.java.
	static {
		dataFields = new ArrayList<DataField>();
		dataFields.add(new DataField(TABLE_ID, DataType.STRING, false)
			.setIndexable(IndexType.HASH));
		dataFields.add(new DataField(FILE_KEY, DataType.STRING, false));
		dataFields.add(new DataField(FILE, DataType.BINARY, false));
	}

	public static Relation getRelation(CallingContext cc) throws ODKDatastoreException {
		Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
		return relation;
	}

	/**
	 * Returns the rows (entities) for a given table id.
	 * @param tableId
	 * @param cc
	 * @return
	 * @throws ODKDatastoreException
	 *
	public static List<Entity> query(String tableId, CallingContext cc) throws
			ODKDatastoreException {
		return getRelation(cc).query("DbTableFiles.query()", cc).equal(TABLE_ID, tableId)
				.execute();
	}

	*/

}
