/*
  Copyright (C) 2010 University of Washington
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
public final class GoogleSpreadsheet2ParameterTable extends CommonFieldsBase {

  private static final String TABLE_NAME = "_google_spreadsheet_2";

  private static final DataField SPREADSHEET_NAME_PROPERTY = new DataField(
      "SPREADSHEET_NAME", DataField.DataType.STRING, true, 4096L);
  private static final DataField SPREADSHEET_KEY_PROPERTY = new DataField(
      "SPREADSHEET_KEY", DataField.DataType.STRING, true, 4096L);
  private static final DataField TOP_LEVEL_WORKSHEET_ID_PROPERTY = new DataField(
      "TOP_LEVEL_WORKSHEET_ID", DataField.DataType.STRING, true, 4096L);
  private static final DataField OWNER_EMAIL_PROPERTY = new DataField(
      "OWNER_EMAIL", DataField.DataType.STRING, true, 4096L);
  private static final DataField READY_PROPERTY = new DataField("READY",
      DataField.DataType.BOOLEAN, true);
  private static GoogleSpreadsheet2ParameterTable relation = null;

  /**
   * Construct a relation prototype. Only called via {@link #assertRelation(CallingContext)}
   *
   * @param databaseSchema
   * @param tableName
   */
  GoogleSpreadsheet2ParameterTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(SPREADSHEET_NAME_PROPERTY);
    fieldList.add(SPREADSHEET_KEY_PROPERTY);
    fieldList.add(TOP_LEVEL_WORKSHEET_ID_PROPERTY);
    fieldList.add(OWNER_EMAIL_PROPERTY);
    fieldList.add(READY_PROPERTY);
  }

  /**
   * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
   *
   * @param ref
   * @param user
   */
  private GoogleSpreadsheet2ParameterTable(GoogleSpreadsheet2ParameterTable ref, User user) {
    super(ref, user);
  }

  public static synchronized final GoogleSpreadsheet2ParameterTable assertRelation(
      CallingContext cc) throws ODKDatastoreException {
    if (relation == null) {
      GoogleSpreadsheet2ParameterTable relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();
      relationPrototype = new GoogleSpreadsheet2ParameterTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  // Only called from within the persistence layer.
  @Override
  public GoogleSpreadsheet2ParameterTable getEmptyRow(User user) {
    return new GoogleSpreadsheet2ParameterTable(this, user);
  }

  public String getSpreadsheetName() {
    return getStringField(SPREADSHEET_NAME_PROPERTY);
  }

  public void setSpreadsheetName(String value) {
    if (!setStringField(SPREADSHEET_NAME_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow spreadsheetName");
    }
  }

  public String getSpreadsheetKey() {
    return getStringField(SPREADSHEET_KEY_PROPERTY);
  }

  public void setSpreadsheetKey(String value) {
    if (!setStringField(SPREADSHEET_KEY_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow spreadsheetKey");
    }
  }

  public String getTopLevelWorksheetId() {
    return getStringField(TOP_LEVEL_WORKSHEET_ID_PROPERTY);
  }

  public void setTopLevelWorksheetId(String value) {
    if (!setStringField(TOP_LEVEL_WORKSHEET_ID_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow topLevelWorksheetId");
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

  public Boolean getReady() {
    return getBooleanField(READY_PROPERTY);
  }

  public void setReady(Boolean value) {
    setBooleanField(READY_PROPERTY, value);
  }

}