package org.opendatakit.aggregate.odktables.entity;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class TableAcl {

  @Element
  private String tableId;

  @Element
  private Scope scope;

  @ElementList(inline = true, entry = "permission")
  private List<TablePermission> permissions;

  /**
   * @param tableId
   * @param scope
   * @param permissions
   */
  public TableAcl(String tableId, Scope scope, List<TablePermission> permissions) {
    this.tableId = tableId;
    this.scope = scope;
    this.permissions = permissions;
  }

  @SuppressWarnings("unused")
  private TableAcl() {}
  
  /**
   * @return the tableId
   */
  public String getTableId() {
    return tableId;
  }

  /**
   * @param tableId
   *          the tableId to set
   */
  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  /**
   * @return the scope
   */
  public Scope getScope() {
    return scope;
  }

  /**
   * @param scope
   *          the scope to set
   */
  public void setScope(Scope scope) {
    this.scope = scope;
  }

  /**
   * @return the permissions
   */
  public List<TablePermission> getPermissions() {
    return permissions;
  }

  /**
   * @param permissions
   *          the permissions to set
   */
  public void setPermissions(List<TablePermission> permissions) {
    this.permissions = permissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
    result = prime * result + ((scope == null) ? 0 : scope.hashCode());
    result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TableAcl))
      return false;
    TableAcl other = (TableAcl) obj;
    if (permissions == null) {
      if (other.permissions != null)
        return false;
    } else if (!permissions.equals(other.permissions))
      return false;
    if (scope == null) {
      if (other.scope != null)
        return false;
    } else if (!scope.equals(other.scope))
      return false;
    if (tableId == null) {
      if (other.tableId != null)
        return false;
    } else if (!tableId.equals(other.tableId))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TableAcl [tableId=");
    builder.append(tableId);
    builder.append(", scope=");
    builder.append(scope);
    builder.append(", permissions=");
    builder.append(permissions);
    builder.append("]");
    return builder.toString();
  }

}
