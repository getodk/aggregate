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
 * Modified for use under OAuth2 using a service account (vs. OAuth).
 * <p>
 * A critical piece of information is the user e-mail account that should
 * be granted ownership rights to the table, its sub-tables, and overall view.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class FusionTable2ParameterTable extends CommonFieldsBase {

  private static final String TABLE_NAME = "_fusion_table_2";

  private static final DataField FUSION_TABLE_ID_PROPERTY = new DataField("FUSION_TABLE_ID", DataField.DataType.STRING, true, 4096L);
  private static final DataField OWNER_EMAIL_PROPERTY = new DataField("OWNER_EMAIL", DataField.DataType.STRING, true, 4096L);
  private static final DataField FUSION_TABLE_VIEW_ID_PROPERTY = new DataField("FUSION_TABLE_VIEW_ID", DataField.DataType.STRING, true, 4096L);
  private static FusionTable2ParameterTable relation = null;

  FusionTable2ParameterTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(FUSION_TABLE_ID_PROPERTY);
    fieldList.add(OWNER_EMAIL_PROPERTY);
    fieldList.add(FUSION_TABLE_VIEW_ID_PROPERTY);
  }

  private FusionTable2ParameterTable(FusionTable2ParameterTable ref, User user) {
    super(ref, user);
  }

  public static synchronized final FusionTable2ParameterTable assertRelation(CallingContext cc) throws ODKDatastoreException {
    if (relation == null) {
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      FusionTable2ParameterTable relationPrototype;
      relationPrototype = new FusionTable2ParameterTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  // Only called from within the persistence layer.
  @Override
  public FusionTable2ParameterTable getEmptyRow(User user) {
    return new FusionTable2ParameterTable(this, user);
  }

  public String getFusionTableId() {
    return getStringField(FUSION_TABLE_ID_PROPERTY);
  }

  public void setFusionTableId(String value) {
    if (!setStringField(FUSION_TABLE_ID_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow fusionTableName");
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

  public String getFusionTableViewId() {
    return getStringField(FUSION_TABLE_VIEW_ID_PROPERTY);
  }

  public void setFusionTableViewId(String value) {
    if (!setStringField(FUSION_TABLE_VIEW_ID_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow fusionTableViewId");
    }
  }
}