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
package org.opendatakit.aggregate.externalservice;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public final class GoogleSpreadsheet2RepeatParameterTable extends CommonFieldsBase {

	  private static final String TABLE_NAME = "_google_spreadsheet_2_repeat";

	  private static final DataField URI_GOOGLE_SPREADSHEET = new DataField("URI_GOOGLE_SPREADSHEET",
			  DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);
	  private static final DataField WORKSHEET_ID = new DataField("WORKSHEET_ID",
	      DataField.DataType.STRING, true, 4096L);
	  private static final DataField FORM_ELEMENT_KEY_PROPERTY = new DataField("FORM_ELEMENT_KEY",
	      DataField.DataType.STRING, true, 4096L);

		/**
		 * Construct a relation prototype.  Only called via {@link #assertRelation(CallingContext)}
		 *
		 * @param databaseSchema
		 * @param tableName
		 */
	  GoogleSpreadsheet2RepeatParameterTable(String schemaName) {
	    super(schemaName, TABLE_NAME);
	    fieldList.add(URI_GOOGLE_SPREADSHEET);
	    fieldList.add(WORKSHEET_ID);
	    fieldList.add(FORM_ELEMENT_KEY_PROPERTY);
	  }

	  /**
	   * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	   *
	   * @param ref
	   * @param user
	   */
	  private GoogleSpreadsheet2RepeatParameterTable(GoogleSpreadsheet2RepeatParameterTable ref, User user) {
	    super(ref, user);
	  }

	  // Only called from within the persistence layer.
	  @Override
	  public GoogleSpreadsheet2RepeatParameterTable getEmptyRow(User user) {
		return new GoogleSpreadsheet2RepeatParameterTable(this, user);
	  }

	  public String getUriGoogleSpreadsheet() {
		  return getStringField(URI_GOOGLE_SPREADSHEET);
	  }

	  public void setUriGoogleSpreadsheet(String value) {
	    if (!setStringField(URI_GOOGLE_SPREADSHEET, value)) {
	      throw new IllegalArgumentException("overflow uriGoogleSpreadsheet");
	    }
	  }

	  public String getWorksheetId() {
	    return getStringField(WORKSHEET_ID);
	  }

	  public void setWorksheetId(String value) {
	    if (!setStringField(WORKSHEET_ID, value)) {
	      throw new IllegalArgumentException("overflow worksheetId");
	    }
	  }

	  public FormElementKey getFormElementKey() {
		String key = getStringField(FORM_ELEMENT_KEY_PROPERTY);
		if ( key == null ) return null;
		return new FormElementKey(key);
	  }

	  public void setFormElementKey(FormElementKey value) {
	    if (!setStringField(FORM_ELEMENT_KEY_PROPERTY, value.toString())) {
	      throw new IllegalArgumentException("overflow formElementKey");
	    }
	  }

	  private static GoogleSpreadsheet2RepeatParameterTable relation = null;

	  public static synchronized final GoogleSpreadsheet2RepeatParameterTable assertRelation(CallingContext cc)
	      throws ODKDatastoreException {
	    if (relation == null) {
	    	GoogleSpreadsheet2RepeatParameterTable relationPrototype;
	    	Datastore ds = cc.getDatastore();
	    	User user = cc.getCurrentUser();
	        relationPrototype = new GoogleSpreadsheet2RepeatParameterTable(ds.getDefaultSchemaName());
	        ds.assertRelation(relationPrototype, user); // may throw exception...
	        // at this point, the prototype has become fully populated
	        relation = relationPrototype; // set static variable only upon success...
	    }
	    return relation;
	  }

	  public static List<GoogleSpreadsheet2RepeatParameterTable> getRepeatGroupAssociations(String uri,
			  												CallingContext cc) throws ODKDatastoreException {
		  List<GoogleSpreadsheet2RepeatParameterTable> list = new ArrayList<GoogleSpreadsheet2RepeatParameterTable> ();
		  GoogleSpreadsheet2RepeatParameterTable frpt = assertRelation(cc);

		  Datastore ds = cc.getDatastore();
		  User user = cc.getCurrentUser();
		  Query query = ds.createQuery(frpt, "GoogleSpreadsheetRepeatParameterTable.getRepeatGroupAssociations", user);
		  query.addFilter(URI_GOOGLE_SPREADSHEET, FilterOperation.EQUAL, uri);

		  List<? extends CommonFieldsBase> results = query.executeQuery();
		  for ( CommonFieldsBase b : results ) {
			  list.add((GoogleSpreadsheet2RepeatParameterTable) b);
		  }
		  return list;
	  }
}
