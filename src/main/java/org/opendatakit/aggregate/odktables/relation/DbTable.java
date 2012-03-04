package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Query;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

public class DbTable {

  public static final String ROW_VERSION = "ROW_VERSION";
  public static final String MODIFICATION_NUMBER = "MODIFICATION_NUMBER";
  public static final String GROUP_OR_USER_ID = "GROUP_OR_USER_ID";
  public static final String DELETED = "DELETED";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(ROW_VERSION, DataType.STRING, false));
    dataFields.add(new DataField(MODIFICATION_NUMBER, DataType.INTEGER, false));
    dataFields.add(new DataField(GROUP_OR_USER_ID, DataType.STRING, true));
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

  /**
   * Retrieve a list of {@link DbTable} row entities.
   * 
   * @param table
   *          the {@link DbTable} relation.
   * @param rowIds
   *          the ids of the rows to get.
   * @param cc
   * @return
   * @throws ODKEntityNotFoundException
   *           if one of the rows does not exist
   * @throws ODKDatastoreException
   */
  public static List<Entity> query(Relation table, List<String> rowIds, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    Validate.notNull(table);
    Validate.noNullElements(rowIds);
    Validate.notNull(cc);

    Query query = table.query("DbTable.query", cc);
    query.include(CommonFieldsBase.URI_COLUMN_NAME, rowIds);
    List<Entity> entities = query.execute();
    return entities;
  }

}