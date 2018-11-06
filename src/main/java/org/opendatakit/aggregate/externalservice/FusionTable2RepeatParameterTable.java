/*
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.externalservice;

import java.util.ArrayList;
import java.util.List;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class FusionTable2RepeatParameterTable extends CommonFieldsBase {

  static final DataField URI_FUSION_TABLE_PROPERTY = new DataField("URI_FUSION_TABLE", DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN);
  static final DataField FORM_ELEMENT_KEY_PROPERTY = new DataField("FORM_ELEMENT_KEY", DataField.DataType.STRING, true, 4096L);
  private static final String TABLE_NAME = "_fusion_table_2_repeat";
  private static final DataField FUSION_TABLE_ID_PROPERTY = new DataField("FUSION_TABLE_ID", DataField.DataType.STRING, true, 4096L);
  private static FusionTable2RepeatParameterTable relation = null;

  FusionTable2RepeatParameterTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(URI_FUSION_TABLE_PROPERTY);
    fieldList.add(FUSION_TABLE_ID_PROPERTY);
    fieldList.add(FORM_ELEMENT_KEY_PROPERTY);
  }

  private FusionTable2RepeatParameterTable(FusionTable2RepeatParameterTable ref, User user) {
    super(ref, user);
  }

  public static synchronized final FusionTable2RepeatParameterTable assertRelation(CallingContext cc) throws ODKDatastoreException {
    if (relation == null) {
      FusionTable2RepeatParameterTable relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new FusionTable2RepeatParameterTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  public static List<FusionTable2RepeatParameterTable> getRepeatGroupAssociations(String uri, CallingContext cc) throws ODKDatastoreException {
    List<FusionTable2RepeatParameterTable> list = new ArrayList<FusionTable2RepeatParameterTable>();
    FusionTable2RepeatParameterTable frpt = assertRelation(cc);

    Query query = cc.getDatastore().createQuery(frpt, "FusionTableRepeatParameterTable.getRepeatGroupAssociations", cc.getCurrentUser());
    query.addFilter(URI_FUSION_TABLE_PROPERTY, FilterOperation.EQUAL, uri);

    List<? extends CommonFieldsBase> results = query.executeQuery();
    for (CommonFieldsBase b : results) {
      list.add((FusionTable2RepeatParameterTable) b);
    }
    return list;
  }

  @Override
  public FusionTable2RepeatParameterTable getEmptyRow(User user) {
    return new FusionTable2RepeatParameterTable(this, user);
  }

  public void setUriFusionTable(String value) {
    if (!setStringField(URI_FUSION_TABLE_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow uriFusionTable");
    }
  }

  public String getFusionTableId() {
    return getStringField(FUSION_TABLE_ID_PROPERTY);
  }

  public void setFusionTableId(String value) {
    if (!setStringField(FUSION_TABLE_ID_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow fusionTableId");
    }
  }

  public FormElementKey getFormElementKey() {
    String key = getStringField(FORM_ELEMENT_KEY_PROPERTY);
    if (key == null) return null;
    return new FormElementKey(key);
  }

  public void setFormElementKey(FormElementKey value) {
    if (!setStringField(FORM_ELEMENT_KEY_PROPERTY, value.toString())) {
      throw new IllegalArgumentException("overflow formElementKey");
    }
  }
}
