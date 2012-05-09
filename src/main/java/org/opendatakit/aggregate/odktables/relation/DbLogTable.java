package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class DbLogTable {

  public static final String ROW_ID = "ROW_ID";
  public static final String ROW_VERSION = "ROW_VERSION";
  public static final String MODIFICATION_NUMBER = "MODIFICATION_NUMBER";
  public static final String CREATE_USER = "CREATE_USER";
  public static final String LAST_UPDATE_USER = "LAST_UPDATE_USER";
  public static final String FILTER_TYPE = "FILTER_TYPE";
  public static final String FILTER_VALUE = "FILTER_VALUE";
  public static final String DELETED = "DELETED";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(ROW_ID, DataType.STRING, false));
    dataFields.add(new DataField(ROW_VERSION, DataType.STRING, false));
    dataFields.add(new DataField(MODIFICATION_NUMBER, DataType.INTEGER, false));
    dataFields.add(new DataField(CREATE_USER, DataType.STRING, false));
    dataFields.add(new DataField(LAST_UPDATE_USER, DataType.STRING, false));
    dataFields.add(new DataField(FILTER_TYPE, DataType.STRING, true));
    dataFields.add(new DataField(FILTER_VALUE, DataType.STRING, true));
    dataFields.add(new DataField(DELETED, DataType.BOOLEAN, false));
  }

  private static final EntityConverter converter = new EntityConverter();

  public static Relation getRelation(String tableId, CallingContext cc)
      throws ODKDatastoreException {
    List<DataField> fields = getDynamicFields(tableId, cc);
    fields.addAll(getStaticFields());
    return getRelation(tableId, fields, cc);
  }

  private static Relation getRelation(String tableId, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    tableId += "_LOG";
    Relation relation = new Relation(RUtil.NAMESPACE, RUtil.convertIdentifier(tableId), fields, cc);
    return relation;
  }

  private static List<DataField> getDynamicFields(String tableId, CallingContext cc)
      throws ODKDatastoreException {
    List<Entity> entities = DbColumn.query(tableId, cc);
    return converter.toFields(entities);
  }

  private static List<DataField> getStaticFields() {
    return Collections.unmodifiableList(dataFields);
  }

}
