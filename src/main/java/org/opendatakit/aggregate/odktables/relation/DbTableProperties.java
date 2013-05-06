package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Query;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SS: I THINK THIS CLASS IS BEING PHASED OUT, AND INSTEAD WILL BE BASICALLY
 * THE DBKEYVALUESTORE.
 * @author dylan price?
 *
 */
public class DbTableProperties {
  public static final String TABLE_ID = "TABLE_ID";
  public static final String TABLE_NAME = "TABLE_NAME";
  public static final String TABLE_METADATA = "TABLE_METADATA";

  private static final String RELATION_NAME = "TABLE_PROPERTIES";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(TABLE_ID, DataType.STRING, false).setIndexable(IndexType.HASH));
    dataFields.add(new DataField(TABLE_NAME, DataType.STRING, false));
    dataFields.add(new DataField(TABLE_METADATA, DataType.LONG_STRING, true));
  }

  public static Relation getRelation(CallingContext cc) throws ODKDatastoreException {
    Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
    return relation;
  }

  /**
   * Retrieves the properties entity for a table.
   * 
   * @param tableId
   * @param cc
   * @return the properties for the table, or null if none was found (most
   *         likely means the table does not exist)
   * @throws ODKDatastoreException
   */
  public static Entity getProperties(String tableId, CallingContext cc)
      throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbTableProperties.getProperties()", cc);
    query.equal(TABLE_ID, tableId);
    Entity properties = query.get();
    return properties;
  }
}
