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

final class FusionTableParameterTable extends CommonFieldsBase {

	private static final String TABLE_NAME = "_fusion_table";
	/*
	 * Property Names for datastore
	 */
	/****************************************************/
	private static final DataField FUSION_TABLE_NAME_PROPERTY = new DataField(
			"FUSION_TABLE_NAME", DataField.DataType.STRING, true, 4096L);
	private static final DataField AUTH_TOKEN_PROPERTY = new DataField(
			"AUTH_TOKEN", DataField.DataType.STRING, true, 4096L);

	public final DataField fusionTableName;
	public final DataField authToken;

	FusionTableParameterTable(String schemaName) {
		super(schemaName, TABLE_NAME, CommonFieldsBase.BaseType.STATIC);
		fieldList.add(fusionTableName = new DataField(
				FUSION_TABLE_NAME_PROPERTY));
		fieldList.add(authToken = new DataField(AUTH_TOKEN_PROPERTY));
	}

	// for creating empty rows
	FusionTableParameterTable(FusionTableParameterTable ref) {
		super(ref);
		fusionTableName = ref.fusionTableName;
		authToken = ref.authToken;
	}

	public String getFusionTableName() {
		return getStringField(fusionTableName);
	}

	public void setFusionTableName(String value) {
		if (!setStringField(fusionTableName, value)) {
			throw new IllegalArgumentException("overflow fusionTableName");
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

	private static FusionTableParameterTable fusionTableParameterTable = null;

	public static FusionTableParameterTable createRelation(Datastore datastore, User user)
			throws ODKDatastoreException {
		if (fusionTableParameterTable == null) {
			fusionTableParameterTable = new FusionTableParameterTable(
					datastore.getDefaultSchemaName());
			datastore.createRelation(fusionTableParameterTable, user);
		}
		return fusionTableParameterTable;
	}
}