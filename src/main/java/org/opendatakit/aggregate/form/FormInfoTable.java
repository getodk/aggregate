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
package org.opendatakit.aggregate.form;

import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class FormInfoTable extends TopLevelDynamicBase {
  public static final DataField FORM_ID = new DataField("FORM_ID",
      DataField.DataType.STRING, true, IForm.MAX_FORM_ID_LENGTH);
  static final String TABLE_NAME = "_form_info";
  private static FormInfoTable relation = null;

  private FormInfoTable(String databaseSchema) {
    super(databaseSchema, TABLE_NAME);
    fieldList.add(FORM_ID);

    fieldValueMap.put(primaryKey, CommonFieldsBase.newMD5HashUri(FormInfo.FORM_ID));
    fieldValueMap.put(FORM_ID, FormInfo.FORM_ID);
  }

  private FormInfoTable(FormInfoTable ref, User user) {
    super(ref, user);
  }

  static synchronized final FormInfoTable assertRelation(CallingContext cc) throws ODKDatastoreException {
    if (relation == null) {
      FormInfoTable relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new FormInfoTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  @Override
  public FormInfoTable getEmptyRow(User user) {
    return new FormInfoTable(this, user);
  }
}
