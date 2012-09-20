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
 * <br>
 * This is the table ("relation") that appears in the datastore as the
 * "_ODKTABLES_COLUMN" table. This is the table that stores the 
 * information about the "columns" that are in the different odktables 
 * tables. It has the type and name information. Therefore this is the 
 * datastore table that you query to get all the column names for a certain
 * table. Each entity (row in this "relation") has the table id that the
 * column belongs to, along with the column type and name.
 * 
 * @author the.dylan.price@gmail.com
 *
 */
public class DbColumn {
	
	// these are the column names in the COLUMN table
  public static final String TABLE_ID = "TABLE_ENTRY_ID";
  public static final String COLUMN_NAME = "COLUMN_NAME";
  public static final String COLUMN_TYPE = "COLUMN_TYPE";

  private static final String RELATION_NAME = "COLUMN";

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(TABLE_ID, DataType.STRING, false).setIndexable(IndexType.HASH));
    dataFields.add(new DataField(COLUMN_NAME, DataType.STRING, false));
    dataFields.add(new DataField(COLUMN_TYPE, DataType.STRING, false));
  }

  public static Relation getRelation(CallingContext cc) throws ODKDatastoreException {
    Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
    return relation;
  }

  public static List<Entity> query(String tableId, CallingContext cc) throws ODKDatastoreException {
    return getRelation(cc).query("DbColumn.query()", cc).equal(TABLE_ID, tableId).execute();
  }

  public static List<String> queryForColumnNames(String tableId, CallingContext cc)
      throws ODKDatastoreException {
    @SuppressWarnings("unchecked")
    List<String> columnNames = (List<String>) getRelation(cc)
        .query("DbColumn.queryForColumnNames", cc).equal(TABLE_ID, tableId)
        .getDistinct(COLUMN_NAME);
    return columnNames;
  }

}