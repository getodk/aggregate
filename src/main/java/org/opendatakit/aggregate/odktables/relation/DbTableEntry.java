package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class DbTableEntry {

  public static final String MODIFICATION_NUMBER = "MODIFICATION_NUMBER";
  public static final String PROPERTIES_MOD_NUM = "PROPERTIES_MOD_NUM";

  private static final String RELATION_NAME = "TABLE_ENTRY";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(MODIFICATION_NUMBER, DataType.INTEGER, false));
    dataFields.add(new DataField(PROPERTIES_MOD_NUM, DataType.INTEGER, false));
  }

  public static Relation getRelation(CallingContext cc) throws ODKDatastoreException {
    Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
    return relation;
  }
}
