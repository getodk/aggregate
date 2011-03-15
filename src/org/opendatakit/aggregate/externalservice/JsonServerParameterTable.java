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

import org.opendatakit.aggregate.CallingContext;
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
public final class JsonServerParameterTable extends CommonFieldsBase {

	private static final String TABLE_NAME = "_json_server";

	private static final DataField SERVER_URL_PROPERTY = new DataField(
			"SERVER_URL", DataField.DataType.STRING, true, 4096L);

	/**
	 * Construct a relation prototype. Only called via {@link #assertRelation(CallingContext)}
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
	JsonServerParameterTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(SERVER_URL_PROPERTY);
	}

	  /**
	   * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	   * 
	   * @param ref
	   * @param user
	   */
	private JsonServerParameterTable(JsonServerParameterTable ref, User user) {
		super(ref, user);
	}

	// Only called from within the persistence layer.
	@Override
	public JsonServerParameterTable getEmptyRow(User user) {
		return new JsonServerParameterTable(this, user);
	}

	public String getServerUrl() {
		return getStringField(SERVER_URL_PROPERTY);
	}

	public void setServerUrl(String value) {
		if (!setStringField(SERVER_URL_PROPERTY, value)) {
			throw new IllegalArgumentException("overflow of serverUrl");
		}
	}

	private static JsonServerParameterTable relation = null;

	public static synchronized final JsonServerParameterTable assertRelation(CallingContext cc)
			throws ODKDatastoreException {
		if (relation == null) {
			JsonServerParameterTable relationPrototype;
			Datastore ds = cc.getDatastore();
			User user = cc.getUserService().getDaemonAccountUser();
	        relationPrototype = new JsonServerParameterTable(ds.getDefaultSchemaName());
	        ds.assertRelation(relationPrototype, user); // may throw exception...
	        // at this point, the prototype has become fully populated
	        relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}

}