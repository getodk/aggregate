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

final class GoogleSpreadsheetParameterTable extends CommonFieldsBase {

	private static final String TABLE_NAME = "_google_spreadsheet";
	/*
	 * Property Names for datastore
	 */
	/****************************************************/
	private static final DataField SPREADSHEET_NAME_PROPERTY = new DataField(
			"SPREADSHEET_NAME", DataField.DataType.STRING, true, 4096L);
	private static final DataField SPREADSHEET_KEY_PROPERTY = new DataField(
			"SPREADSHEET_KEY", DataField.DataType.STRING, true, 4096L);
	private static final DataField AUTH_TOKEN_PROPERTY = new DataField(
			"AUTH_TOKEN", DataField.DataType.STRING, true, 4096L);
	private static final DataField READY_PROPERTY = new DataField("READY",
			DataField.DataType.BOOLEAN, true);

	public final DataField spreadsheetName;
	public final DataField spreadsheetKey;
	public final DataField authToken;
	public final DataField ready;

	GoogleSpreadsheetParameterTable(String schemaName) {
		super(schemaName, TABLE_NAME, CommonFieldsBase.BaseType.STATIC);
		fieldList
				.add(spreadsheetName = new DataField(SPREADSHEET_NAME_PROPERTY));
		fieldList.add(spreadsheetKey = new DataField(SPREADSHEET_KEY_PROPERTY));
		fieldList.add(authToken = new DataField(AUTH_TOKEN_PROPERTY));
		fieldList.add(ready = new DataField(READY_PROPERTY));
	}

	// for creating empty rows
	GoogleSpreadsheetParameterTable(GoogleSpreadsheetParameterTable ref) {
		super(ref);
		spreadsheetName = ref.spreadsheetName;
		spreadsheetKey = ref.spreadsheetKey;
		authToken = ref.authToken;
		ready = ref.ready;
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

	public String getAuthToken() {
		return getStringField(authToken);
	}

	public void setAuthToken(String value) {
		if (!setStringField(authToken, value)) {
			throw new IllegalArgumentException("overflow authToken");
		}
	}

	public Boolean getReady() {
		return getBooleanField(ready);
	}

	public void setReady(Boolean value) {
		setBooleanField(ready, value);
	}

	private static GoogleSpreadsheetParameterTable googleSpreadsheetParameterTable = null;

	public static GoogleSpreadsheetParameterTable createRelation(
			Datastore datastore, User user) throws ODKDatastoreException {
		if (googleSpreadsheetParameterTable == null) {
			googleSpreadsheetParameterTable = new GoogleSpreadsheetParameterTable(
					datastore.getDefaultSchemaName());
			datastore.createRelation(googleSpreadsheetParameterTable, user);
		}
		return googleSpreadsheetParameterTable;
	}

}