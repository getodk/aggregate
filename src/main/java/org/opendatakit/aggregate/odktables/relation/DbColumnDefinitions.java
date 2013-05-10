package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Comments by sudar.sam@gmail.com, so there may be discrepancies between
 * the comments and what was actually intended.
 * <p>
 * This table stores the immutable definitions of each column in the datastore.
 * It is based on the eponymous table in ODK Tables, so that there should be
 * a mirrored architecture. It is based on the ODK Tables Schema Google doc.
 * <p>
 * This is the table (i.e. "relation") that appears in the datastore as the
 * "_ODKTABLES_COLUMN" table. This is the table that stores the 
 * information about the "columns" that are in the different odktables 
 * tables. It has the type and name information. Therefore this is the 
 * datastore table that you query to get all the column names for a certain
 * table. Each entity (row in this "relation") has the table id that the
 * column belongs to, along with the column type and name.
 * 
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 *
 */
public class DbColumnDefinitions {
	
	// these are the column names in the COLUMN table
  public static final String TABLE_ID = "TABLE_ID";
  public static final String ELEMENT_KEY = "ELEMENT_KEY";
  public static final String ELEMENT_NAME = "ELEMENT_NAME";
  public static final String ELEMENT_TYPE = "ELEMENT_TYPE";
  public static final String LIST_CHILD_ELEMENT_KEYS = 
      "LIST_CHILD_ELEMENT_KEYS";
  public static final String IS_PERSISTED = "IS_PERSISTED";
  public static final String JOINS = "JOINS";

  private static final String RELATION_NAME = "COLUMN_DEFINITIONS";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(TABLE_ID, DataType.STRING, false)
        .setIndexable(IndexType.HASH));
    dataFields.add(new DataField(ELEMENT_KEY, DataType.STRING, false));
    dataFields.add(new DataField(ELEMENT_NAME, DataType.STRING, false));
    dataFields.add(new DataField(ELEMENT_TYPE, DataType.STRING, true));
    dataFields.add(
        new DataField(LIST_CHILD_ELEMENT_KEYS, DataType.STRING, true));
    dataFields.add(new DataField(IS_PERSISTED, DataType.INTEGER, false));
    dataFields.add(new DataField(JOINS, DataType.STRING, true));
  }

  public static Relation getRelation(CallingContext cc) 
      throws ODKDatastoreException {
    Relation relation = 
        new Relation(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
    return relation;
  }

  /**
   * Gets all of the columns in the column definitions table. This will not
   * include metadata columns present in the data tables, like last_mod_time
   * or rowid.
   * @param tableId
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static List<Entity> query(String tableId, CallingContext cc) 
      throws ODKDatastoreException {
    return getRelation(cc).query("DbColumnDefinitions.query()", cc)
        .equal(TABLE_ID, tableId).execute();
  }

  /**
   * Return the ELEMENT_NAMEs for the given table. Currently returns all, even
   * the non-persisted ones.
   * @param tableId
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static List<String> queryForColumnNames(String tableId, 
      CallingContext cc) throws ODKDatastoreException {
    @SuppressWarnings("unchecked")
    List<String> columnNames = (List<String>) getRelation(cc)
        .query("DbColumn.queryForColumnNames()", cc).equal(TABLE_ID, tableId)
        .getDistinct(ELEMENT_NAME);
    return columnNames;
  }

}