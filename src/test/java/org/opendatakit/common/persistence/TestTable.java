/*
 * Copyright (C) 2018 Nafundi
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
package org.opendatakit.common.persistence;

import static org.opendatakit.common.persistence.DataField.DataType.BOOLEAN;
import static org.opendatakit.common.persistence.DataField.DataType.DATETIME;
import static org.opendatakit.common.persistence.DataField.DataType.DECIMAL;
import static org.opendatakit.common.persistence.DataField.DataType.INTEGER;
import static org.opendatakit.common.persistence.DataField.DataType.STRING;

import org.opendatakit.common.security.User;

class TestTable extends CommonFieldsBase {
  static final DataField stringField = new DataField("STRING_FIELD", STRING, true, 90L);
  static final DataField integerField = new DataField("INTEGER_FIELD", INTEGER, true);
  static final DataField doubleField = new DataField("DOUBLE_FIELD", DECIMAL, true);
  static final DataField dateField = new DataField("DATE_FIELD", DATETIME, true);
  static final DataField booleanField = new DataField("BOOLEAN_FIELD", BOOLEAN, true);

  /**
   * Construct a relation prototype.
   */
  TestTable(String schema) {
    super(schema, "TEST_TABLE");
    fieldList.add(stringField);
    fieldList.add(integerField);
    fieldList.add(doubleField);
    fieldList.add(dateField);
    fieldList.add(booleanField);
  }

  /**
   * Construct an empty entity.
   */
  private TestTable(TestTable ref, User user) {
    super(ref, user);
  }

  @Override
  public CommonFieldsBase getEmptyRow(User user) {
    return new TestTable(this, user);
  }

  public void print() {
    System.out.println("PK " + getUri() + " "
        + stringField.getName() + " "
        + getStringField(stringField) + " "
        + getLongField(integerField) + " "
        + getNumericField(doubleField) + " "
        + getDateField(dateField) + " "
        + getBooleanField(booleanField));
  }


}
