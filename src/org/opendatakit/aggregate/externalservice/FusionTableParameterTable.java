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
public final class FusionTableParameterTable extends CommonFieldsBase {

  private static final String TABLE_NAME = "_fusion_table";
  /*
   * Property Names for datastore
   */
  /****************************************************/
  private static final DataField FUSION_TABLE_ID_PROPERTY = new DataField("FUSION_TABLE_ID",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField AUTH_TOKEN_PROPERTY = new DataField("AUTH_TOKEN",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField AUTH_TOKEN_SECRET_PROPERTY = new DataField("AUTH_TOKEN_SECRET",
		  DataField.DataType.STRING, true, 4096L);

  public final DataField fusionTableId;
  public final DataField authToken;
  public final DataField authTokenSecret;

	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 */
  FusionTableParameterTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(fusionTableId = new DataField(FUSION_TABLE_ID_PROPERTY));
    fieldList.add(authToken = new DataField(AUTH_TOKEN_PROPERTY));
    fieldList.add(authTokenSecret = new DataField(AUTH_TOKEN_SECRET_PROPERTY));
  }

	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
  private FusionTableParameterTable(FusionTableParameterTable ref, User user) {
    super(ref, user);
    fusionTableId = ref.fusionTableId;
    authToken = ref.authToken;
    authTokenSecret = ref.authTokenSecret;
  }

  // Only called from within the persistence layer.
  @Override
  public FusionTableParameterTable getEmptyRow(User user) {
	return new FusionTableParameterTable(this, user);
  }

  public String getFusionTableId() {
    return getStringField(fusionTableId);
  }

  public void setFusionTableId(String value) {
    if (!setStringField(fusionTableId, value)) {
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
  
  public String getAuthTokenSecret() {
	  return getStringField(authTokenSecret);
  }
  
  public void setAuthTokenSecret(String value) {
	  if (!setStringField(authTokenSecret, value)) {
		  throw new IllegalArgumentException("overflow authTokenSecret");
	  }
  }
  
  private static FusionTableParameterTable relation = null;

  public static synchronized final FusionTableParameterTable createRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      FusionTableParameterTable relationPrototype;
      relationPrototype = new FusionTableParameterTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }
}