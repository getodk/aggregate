/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.externalservice;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class GoogleSpreadsheetParameterTable extends CommonFieldsBase {

	private static final String TABLE_NAME = "_google_spreadsheet";
	/*
	 * Property Names for datastore
	 */
	/****************************************************/
	private static final DataField SPREADSHEET_NAME_PROPERTY = new DataField(
			"SPREADSHEET_NAME", DataField.DataType.STRING, true, 4096L);
	private static final DataField SPREADSHEET_KEY_PROPERTY = new DataField(
			"SPREADSHEET_KEY", DataField.DataType.STRING, true, 4096L);
	private static final DataField TOP_LEVEL_WORKSHEET_ID_PROPERTY = new DataField("TOP_LEVEL_WORKSHEET_ID",
		      DataField.DataType.STRING, true, 4096L);
	private static final DataField AUTH_TOKEN_PROPERTY = new DataField(
			"AUTH_TOKEN", DataField.DataType.STRING, true, 4096L);
	private static final DataField AUTH_TOKEN_SECRET_PROPERTY = new DataField(
			"AUTH_TOKEN_SECRET", DataField.DataType.STRING, true, 4096L);
	private static final DataField READY_PROPERTY = new DataField("READY",
			DataField.DataType.BOOLEAN, true);

	public final DataField spreadsheetName;
	public final DataField spreadsheetKey;
	public final DataField topLevelWorksheetId;
	public final DataField authToken;
	public final DataField authTokenSecret;
	public final DataField ready;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	GoogleSpreadsheetParameterTable(String schemaName) {
		super(schemaName, TABLE_NAME, CommonFieldsBase.BaseType.STATIC);
		fieldList.add(spreadsheetName = new DataField(SPREADSHEET_NAME_PROPERTY));
		fieldList.add(spreadsheetKey = new DataField(SPREADSHEET_KEY_PROPERTY));
		fieldList.add(topLevelWorksheetId = new DataField(TOP_LEVEL_WORKSHEET_ID_PROPERTY));
		fieldList.add(authToken = new DataField(AUTH_TOKEN_PROPERTY));
		fieldList.add(authTokenSecret = new DataField(AUTH_TOKEN_SECRET_PROPERTY));
		fieldList.add(ready = new DataField(READY_PROPERTY));
	}

	  /**
	   * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	   * 
	   * @param ref
	   * @param user
	   */
	private GoogleSpreadsheetParameterTable(GoogleSpreadsheetParameterTable ref, User user) {
		super(ref, user);
		spreadsheetName = ref.spreadsheetName;
		spreadsheetKey = ref.spreadsheetKey;
		topLevelWorksheetId = ref.topLevelWorksheetId;
		authToken = ref.authToken;
		authTokenSecret = ref.authTokenSecret;
		ready = ref.ready;
	}

	// Only called from within the persistence layer.
	@Override
	public GoogleSpreadsheetParameterTable getEmptyRow(User user) {
		return new GoogleSpreadsheetParameterTable(this, user);
	}

	public String getSpreadsheetName() {
		return getStringField(spreadsheetName);
	}

	public void setSpreadsheetName(String value) {
		if (!setStringField(spreadsheetName, value)) {
			throw new IllegalArgumentException("overflow spreadsheetName");
		}
	}

	public String getSpreadsheetKey() {
		return getStringField(spreadsheetKey);
	}

	public void setSpreadsheetKey(String value) {
		if (!setStringField(spreadsheetKey, value)) {
			throw new IllegalArgumentException("overflow spreadsheetKey");
		}
	}
	
	public void setTopLevelWorksheetId(String value) {
		if (!setStringField(topLevelWorksheetId, value)) {
			throw new IllegalArgumentException("overflow topLevelWorksheetId");
		}
	}
	
	public String getTopLevelWorksheetId() {
		return getStringField(topLevelWorksheetId);
	}

	public String getAuthToken() {
		return getStringField(authToken);
	}

	public void setAuthToken(String value) {
		if (!setStringField(authToken, value)) {
			throw new IllegalArgumentException("overflow authToken");
		}
	}
	
	public String getAuthTokenSecret(){
		return getStringField(authTokenSecret);
	}
	
	public void setAuthTokenSecret(String value) {
		if (!setStringField(authTokenSecret, value)) {
			throw new IllegalArgumentException("overflow authTokenSecret");
		}
	}

	public Boolean getReady() {
		return getBooleanField(ready);
	}

	public void setReady(Boolean value) {
		setBooleanField(ready, value);
	}

	private static GoogleSpreadsheetParameterTable relation = null;

	public static GoogleSpreadsheetParameterTable createRelation(
			Datastore datastore, User user) throws ODKDatastoreException {
		if (relation == null) {
			GoogleSpreadsheetParameterTable relationPrototype;
	        relationPrototype = new GoogleSpreadsheetParameterTable(datastore.getDefaultSchemaName());
	        datastore.createRelation(relationPrototype, user); // may throw exception...
	        // at this point, the prototype has become fully populated
	        relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}

}