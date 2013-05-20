package org.opendatakit.aggregate.odktables.entity;

import java.util.List;

import org.opendatakit.aggregate.client.odktables.TableTypeClient;
import org.opendatakit.aggregate.odktables.entity.api.TableType;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Represents the XML format of a table definition. This is essentially all the
 * necessary information for a table to be defined on the server in a way taht
 * will be ODK Tables -friendly.
 * @author dylan price?
 * @author sudar.sam@gmail.com
 *
 */
@Root
@Default(value = DefaultType.FIELD, required = false)
public class TableDefinition {

  /**
   * This is based roughly on the ODK Tables Schema Google Doc. The required
   * elements are those that are not allowed to be null in
   * {@link DbTableDefinitions}.
   */

  @Element(name = "table_id", required = true)
  private String tableId;

  @Element(name = "table_key", required = true)
  private String tableKey;

  @Element(name = "db_table_name", required = true)
  private String dbTableName;

  @Element(name = "type", required = true)
  private TableType type;

  @Element(name = "table_id_access_controls", required = false)
  private String tableIdAccessControls;

  /*
   * While not defined in DbTableDefinitions, this was originally how
   * column information was uploaded to the server, and will remain
   * this way for now.
   */
  @ElementList(inline = true)
  private List<Column> columns;

// ss: trying to subsume this information into the kvs.
//  @Element(required = false)
//  private String metadata;

  protected TableDefinition() {
  }

  /**
   * Construct the table definition
   * @param tableId id of the table
   * @param columns list of {@link Column} objects
   * @param tableKey key of the table
   * @param dbTableName the db name of the table
   * @param type the string type of the table (must be one of
   *     {@link TableTypeClient#getRepresentation()})
   * @param tableIdAccessControls id of the table holding access controls
   */
  public TableDefinition(final String tableId, final List<Column> columns,
      final String tableKey, final String dbTableName, final TableType type,
      final String tableIdAccessControls) {
    this.tableId = tableId;
    this.columns = columns;
    this.tableKey = tableKey;
    this.dbTableName = dbTableName;
    this.type = type;
    this.tableIdAccessControls = tableIdAccessControls;
  }

  public String getTableId() {
    return this.tableId;
  }

  public String getTableKey() {
    return this.tableKey;
  }

  public List<Column> getColumns() {
    return this.columns;
  }

  public TableType getType() {
    return this.type;
  }

  public String getTableIdAccessControls() {
    return this.tableIdAccessControls;
  }

  public String getDbTableName() {
    return this.dbTableName;
  }

  public void setColumns(final List<Column> columns) {
    this.columns = columns;
  }

  @Override
  public String toString() {
    return "TableDefinition [tableId=" + tableId + ", columns=" + columns +
        ", tableKey=" + tableKey + ", dbTableName=" + dbTableName +
        ", type=" + type + ", tableIdAccessControls=" + tableIdAccessControls
        + "]";
  }
}