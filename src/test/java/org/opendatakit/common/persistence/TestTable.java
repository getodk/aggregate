package org.opendatakit.common.persistence;

import static org.opendatakit.common.persistence.DataField.DataType.BOOLEAN;
import static org.opendatakit.common.persistence.DataField.DataType.DATETIME;
import static org.opendatakit.common.persistence.DataField.DataType.DECIMAL;
import static org.opendatakit.common.persistence.DataField.DataType.INTEGER;
import static org.opendatakit.common.persistence.DataField.DataType.STRING;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

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
