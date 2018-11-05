/**
 * Copyright (C) 2010-2013 University of Washington
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.externalservice;

import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public final class JsonServer3ParameterTable extends CommonFieldsBase {

  private static final String TABLE_NAME = "_json_server3";

  private static final DataField AUTH_KEY_PROPERTY = new DataField("AUTH_KEY",
      DataField.DataType.STRING, true, 4096L);

  private static final DataField OWNER_EMAIL_PROPERTY = new DataField("OWNER_EMAIL",
      DataField.DataType.STRING, true, 4096L);

  private static final DataField SERVER_URL_PROPERTY = new DataField("SERVER_URL",
      DataField.DataType.STRING, true, 4096L);

  private static final DataField BINARY_OPTION_PROPERTY = new DataField("BINARY_OPTION",
      DataField.DataType.STRING, true, 4096L);
  private static JsonServer3ParameterTable relation = null;

  /**
   * Construct a relation prototype. Only called via
   * {@link #assertRelation(CallingContext)}
   *
   * @param databaseSchema
   * @param tableName
   */
  JsonServer3ParameterTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(AUTH_KEY_PROPERTY);
    fieldList.add(OWNER_EMAIL_PROPERTY);
    fieldList.add(SERVER_URL_PROPERTY);
    fieldList.add(BINARY_OPTION_PROPERTY);
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   *
   * @param ref
   * @param user
   */
  private JsonServer3ParameterTable(JsonServer3ParameterTable ref, User user) {
    super(ref, user);
  }

  public static synchronized final JsonServer3ParameterTable assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      JsonServer3ParameterTable relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new JsonServer3ParameterTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  // Only called from within the persistence layer.
  @Override
  public JsonServer3ParameterTable getEmptyRow(User user) {
    return new JsonServer3ParameterTable(this, user);
  }

  public String getOwnerEmail() {
    return getStringField(OWNER_EMAIL_PROPERTY);
  }

  public void setOwnerEmail(String value) {
    if (!setStringField(OWNER_EMAIL_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow ownerEmail");
    }
  }

  public String getAuthKey() {
    return getStringField(AUTH_KEY_PROPERTY);
  }

  public void setAuthKey(String authKey) {
    if (!setStringField(AUTH_KEY_PROPERTY, authKey)) {
      throw new IllegalArgumentException("overflow of authKey");
    }
  }

  public String getServerUrl() {
    return getStringField(SERVER_URL_PROPERTY);
  }

  public void setServerUrl(String value) {
    if (!setStringField(SERVER_URL_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow of serverUrl");
    }
  }

  public BinaryOption getBinaryOption() {
    String type = getStringField(BINARY_OPTION_PROPERTY);
    return BinaryOption.valueOf(type);
  }

  public void setBinaryOption(BinaryOption value) {
    if (!setStringField(BINARY_OPTION_PROPERTY, value.name())) {
      throw new IllegalArgumentException("overflow BinaryOption");
    }
  }

}