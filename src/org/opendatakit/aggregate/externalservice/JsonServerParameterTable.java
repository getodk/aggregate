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

final class JsonServerParameterTable extends CommonFieldsBase {

	private static final String TABLE_NAME = "_json_server";
	/*
	 * Property Names for datastore
	 */
	/****************************************************/
	private static final DataField SERVER_URL_PROPERTY = new DataField(
			"SERVER_URL", DataField.DataType.STRING, true, 4096L);

	public final DataField serverUrl;

	JsonServerParameterTable(String schemaName) {
		super(schemaName, TABLE_NAME, CommonFieldsBase.BaseType.STATIC);
		fieldList.add(serverUrl = new DataField(SERVER_URL_PROPERTY));
	}

	// for creating empty rows
	JsonServerParameterTable(JsonServerParameterTable ref) {
		super(ref);
		serverUrl = ref.serverUrl;
	}

	public String getServerUrl() {
		return getStringField(serverUrl);
	}

	public void setServerUrl(String value) {
		if (!setStringField(serverUrl, value)) {
			throw new IllegalArgumentException("overflow of serverUrl");
		}
	}

	private static JsonServerParameterTable jsonServerParameterTable = null;

	public static JsonServerParameterTable createRelation(Datastore datastore, User user)
			throws ODKDatastoreException {
		if (jsonServerParameterTable == null) {
			jsonServerParameterTable = new JsonServerParameterTable(
					datastore.getDefaultSchemaName());
			datastore.createRelation(jsonServerParameterTable, user);
		}
		return jsonServerParameterTable;
	}

}