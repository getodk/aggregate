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
package org.opendatakit.common.datamodel;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.security.User;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public abstract class DynamicDocumentBase extends DynamicCommonFieldsBase {

  /**
   * key into the top level dynamic table that is our ancestor
   */
  private static final DataField TOP_LEVEL_AURI = new DataField("_TOP_LEVEL_AURI", DataField.DataType.URI, true, PersistConsts.URI_STRING_LEN);

  public final DataField topLevelAuri;

  protected DynamicDocumentBase(String databaseSchema, String tableName) {
    super(databaseSchema, tableName);
    fieldList.add(topLevelAuri = new DataField(TOP_LEVEL_AURI));
  }

  protected DynamicDocumentBase(DynamicDocumentBase ref, User user) {
    super(ref, user);
    topLevelAuri = ref.topLevelAuri;
  }

  public final void setTopLevelAuri(String value) {
    if (!setStringField(topLevelAuri, value)) {
      throw new IllegalStateException("overflow on topLevelAuri");
    }
  }
}
