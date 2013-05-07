package org.opendatakit.aggregate.odktables.entity.api;

import java.util.List;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.TableDefinition;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(DefaultType.FIELD)
public class TableDefinitionResource extends TableDefinition {
  
  private String selfUri;
  private String tableUri;
  
  @SuppressWarnings("unused")
  private TableDefinitionResource() {
  }
  
  public TableDefinitionResource(TableDefinition definition) {
    this(definition.getTableId(), definition.getColumns(), 
        definition.getTableKey(), definition.getDbTableName(), 
        definition.getType(), definition.getTableIdAccessControls());
  }
  
  public TableDefinitionResource(String tableId, List<Column> columns,
      String tableKey, String dbTableName, TableType type, 
      String tableIdAccessControls) {
    super(tableId, columns, tableKey, dbTableName, type, 
        tableIdAccessControls);
  }
  
  public String getSelfUri() {
    return this.selfUri;
  }
  
  public String getTableUri() {
    return this.tableUri;
  }
  
  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }
  
  public void setTableUri(final String tableUri) {
    this.tableUri = tableUri;
  }

}
