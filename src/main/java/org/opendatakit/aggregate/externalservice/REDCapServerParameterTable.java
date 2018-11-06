/*
  Copyright (C) 2013 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.aggregate.externalservice;

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
public final class REDCapServerParameterTable extends CommonFieldsBase {

  private static final String TABLE_NAME = "_redcap_server";

  private static final DataField API_KEY_PROPERTY = new DataField("API_KEY",
      DataField.DataType.STRING, true, 4096L);

  private static final DataField URL_PROPERTY = new DataField("URL", DataField.DataType.STRING,
      true, 4096L);

  private static final DataField OWNER_EMAIL_PROPERTY = new DataField("OWNER_EMAIL",
      DataField.DataType.STRING, true, 4096L);
  private static REDCapServerParameterTable relation = null;

  /**
   * Construct a relation prototype. Only called via
   * {@link #assertRelation(CallingContext)}
   *
   * @param databaseSchema
   * @param tableName
   */
  REDCapServerParameterTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(API_KEY_PROPERTY);
    fieldList.add(URL_PROPERTY);
    fieldList.add(OWNER_EMAIL_PROPERTY);
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   *
   * @param ref
   * @param user
   */
  private REDCapServerParameterTable(REDCapServerParameterTable ref, User user) {
    super(ref, user);
  }

  public static synchronized final REDCapServerParameterTable assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      REDCapServerParameterTable relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new REDCapServerParameterTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  // Only called from within the persistence layer.
  @Override
  public REDCapServerParameterTable getEmptyRow(User user) {
    return new REDCapServerParameterTable(this, user);
  }

  public String getApiKey() {
    return getStringField(API_KEY_PROPERTY);
  }

  public void setApiKey(String value) {
    if (!setStringField(API_KEY_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow of apiKey");
    }
  }

  public String getUrl() {
    return getStringField(URL_PROPERTY);
  }

  public void setUrl(String value) {
    if (!setStringField(URL_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow of url");
    }
  }

  public String getOwnerEmail() {
    return getStringField(OWNER_EMAIL_PROPERTY);
  }

  public void setOwnerEmail(String value) {
    if (!setStringField(OWNER_EMAIL_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow ownerEmail");
    }
  }

}