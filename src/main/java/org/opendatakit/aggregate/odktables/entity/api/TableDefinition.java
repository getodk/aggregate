package org.opendatakit.aggregate.odktables.entity.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.Column;

@XmlRootElement
public class TableDefinition {
  private List<Column> columns;

  public TableDefinition() {
  }

  /**
   * @param columns
   */
  public TableDefinition(List<Column> columns) {
    this.columns = columns;
  }

  /**
   * @return the columns
   */
  public List<Column> getColumns() {
    return columns;
  }

  /**
   * @param columns
   *          the columns to set
   */
  public void setColumns(List<Column> columns) {
    this.columns = columns;
  }

}
