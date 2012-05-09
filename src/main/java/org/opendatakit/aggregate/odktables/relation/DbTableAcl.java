package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class DbTableAcl {

  public static final String TABLE_ID = "TABLE_ID";
  public static final String SCOPE_TYPE = "SCOPE_TYPE";
  public static final String SCOPE_VALUE = "SCOPE_VALUE";
  public static final String PERMISSIONS = "PERMISSIONS";

  private static final String RELATION_NAME = "TABLE_ACL";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(TABLE_ID, DataType.STRING, false));
    dataFields.add(new DataField(SCOPE_TYPE, DataType.STRING, false));
    dataFields.add(new DataField(SCOPE_VALUE, DataType.STRING, false));
    dataFields.add(new DataField(PERMISSIONS, DataType.STRING, false));
  }

  public static Relation getRelation(CallingContext cc) throws ODKDatastoreException {
    Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
    return relation;
  }
}
