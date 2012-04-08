package org.opendatakit.aggregate.odktables.entity.api;

import java.util.List;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class TableDefinition {

  @ElementList(inline = true)
  private List<Column> columns;

  public TableDefinition() {
  }

  public TableDefinition(final List<Column> columns) {
    this.columns = columns;
  }

  public List<Column> getColumns() {
    return this.columns;
  }

  public void setColumns(final List<Column> columns) {
    this.columns = columns;
  }

  @Override
  public boolean equals(final java.lang.Object o) {
    if (o == this)
      return true;
    if (!(o instanceof TableDefinition))
      return false;
    final TableDefinition other = (TableDefinition) o;
    if (!other.canEqual((java.lang.Object) this))
      return false;
    if (this.getColumns() == null ? other.getColumns() != null : !this.getColumns().equals(
        (java.lang.Object) other.getColumns()))
      return false;
    return true;
  }

  public boolean canEqual(final java.lang.Object other) {
    return other instanceof TableDefinition;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = result * PRIME + (this.getColumns() == null ? 0 : this.getColumns().hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableDefinition(columns=" + this.getColumns() + ")";
  }
}