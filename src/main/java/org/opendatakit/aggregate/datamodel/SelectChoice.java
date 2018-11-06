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
package org.opendatakit.aggregate.datamodel;

import org.opendatakit.common.datamodel.DynamicBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.security.User;

/**
 * Selection choices are stored in separate tables for each
 * instance data field. Multiple-choice selections have
 * multiple values for a given <code>PARENT_AURI</code>.
 * The order of these values as they were submitted is given
 * by the <code>ORDINAL_NUMBER</code> of the selection.
 * Thus, this representation can support ordered lists of
 * selections as well as simple sets of selections (where the
 * order is just a side-effect of the serialization mechanism).
 * Having all selections, even single-choice selections broken
 * out into separate tables allows for transparent modification
 * of a selection from a select-1 to a select-many, or an
 * ordered selection as revisions occur without impacting the
 * data representation (note, however, that data interpretation
 * would need to account for the changing value-set of the field).
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 */
public final class SelectChoice extends DynamicBase {
  private static final DataField VALUE = new DataField("VALUE", DataField.DataType.STRING, false);
  public final DataField value;

  public SelectChoice(String databaseSchema, String tableName) {
    super(databaseSchema, tableName);
    fieldList.add(value = new DataField(VALUE));
  }

  private SelectChoice(SelectChoice ref, User user) {
    super(ref, user);
    value = ref.value;
  }

  @Override
  public SelectChoice getEmptyRow(User user) {
    return new SelectChoice(this, user);
  }

  public String getValue() {
    return getStringField(value);
  }

  public void setValue(String v) {
    if (!setStringField(value, v)) {
      throw new IllegalArgumentException("overflow choice value");
    }
  }
}
