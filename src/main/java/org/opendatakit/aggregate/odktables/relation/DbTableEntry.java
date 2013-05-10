package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class DbTableEntry {

  public static final String DATA_ETAG = "DATA_ETAG";
  public static final String PROPERTIES_ETAG = "PROPERTIES_ETAG";

  private static final String RELATION_NAME = "TABLE_ENTRY";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(DATA_ETAG, DataType.STRING, false));
    dataFields.add(new DataField(PROPERTIES_ETAG, DataType.STRING, false));
  }

  public static Relation getRelation(CallingContext cc) throws ODKDatastoreException {
    Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
    return relation;
  }
}
