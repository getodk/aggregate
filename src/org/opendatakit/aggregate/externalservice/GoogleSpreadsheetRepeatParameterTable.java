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
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.StaticAssociationBase;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class GoogleSpreadsheetRepeatParameterTable extends StaticAssociationBase {

	  private static final String TABLE_NAME = "_google_spreadsheet_repeat";
	  /*
	   * Property Names for datastore
	   */
	  /****************************************************/
	  private static final DataField WORKSHEET_ID = new DataField("WORKSHEET_ID",
	      DataField.DataType.STRING, true, 4096L);
	  private static final DataField FORM_ELEMENT_KEY_PROPERTY = new DataField("FORM_ELEMENT_KEY",
	      DataField.DataType.STRING, true, 4096L);

	  public final DataField worksheetId;
	  public final DataField formElementKey;

		/**
		 * Construct a relation prototype.
		 * 
		 * @param databaseSchema
		 * @param tableName
		 */
	  GoogleSpreadsheetRepeatParameterTable(String schemaName) {
	    super(schemaName, TABLE_NAME);
	    fieldList.add(worksheetId = new DataField(WORKSHEET_ID));
	    fieldList.add(formElementKey = new DataField(FORM_ELEMENT_KEY_PROPERTY));
	  }

	  /**
	   * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	   * 
	   * @param ref
	   * @param user
	   */
	  private GoogleSpreadsheetRepeatParameterTable(GoogleSpreadsheetRepeatParameterTable ref, User user) {
	    super(ref, user);
	    worksheetId = ref.worksheetId;
	    formElementKey = ref.formElementKey;
	  }

	  // Only called from within the persistence layer.
	  @Override
	  public GoogleSpreadsheetRepeatParameterTable getEmptyRow(User user) {
		return new GoogleSpreadsheetRepeatParameterTable(this, user);
	  }

	  public String getWorksheetId() {
	    return getStringField(worksheetId);
	  }

	  public void setWorksheetId(String value) {
	    if (!setStringField(worksheetId, value)) {
	      throw new IllegalArgumentException("overflow googleSpreadsheetName");
	    }
	  }

	  public FormElementKey getFormElementKey() {
		String key = getStringField(formElementKey);
		if ( key == null ) return null;
		return new FormElementKey(key);
	  }

	  public void setFormElementKey(FormElementKey value) {
	    if (!setStringField(formElementKey, value.toString())) {
	      throw new IllegalArgumentException("overflow formElementKey");
	    }
	  }

	  private static GoogleSpreadsheetRepeatParameterTable relation = null;

	  public static synchronized final GoogleSpreadsheetRepeatParameterTable createRelation(Datastore datastore, User user)
	      throws ODKDatastoreException {
	    if (relation == null) {
	    	GoogleSpreadsheetRepeatParameterTable relationPrototype;
	        relationPrototype = new GoogleSpreadsheetRepeatParameterTable(datastore.getDefaultSchemaName());
	        datastore.createRelation(relationPrototype, user); // may throw exception...
	        // at this point, the prototype has become fully populated
	        relation = relationPrototype; // set static variable only upon success...
	    }
	    return relation;
	  }
	  
	  public static List<GoogleSpreadsheetRepeatParameterTable> getRepeatGroupAssociations(EntityKey googleSpreadsheetParameterTable,
			  												Datastore datastore, User user) throws ODKDatastoreException {
		  List<GoogleSpreadsheetRepeatParameterTable> list = new ArrayList<GoogleSpreadsheetRepeatParameterTable> ();
		  GoogleSpreadsheetRepeatParameterTable frpt = createRelation(datastore,user);

		  Query query = datastore.createQuery(frpt, user);
		  query.addFilter(frpt.domAuri, FilterOperation.EQUAL, googleSpreadsheetParameterTable.getKey());

		  List<? extends CommonFieldsBase> results = query.executeQuery(0);
		  for ( CommonFieldsBase b : results ) {
			  list.add((GoogleSpreadsheetRepeatParameterTable) b);
		  }
		  return list;
	  }
}
