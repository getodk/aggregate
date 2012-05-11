package org.opendatakit.aggregate.odktables.entity;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class TableAcl {

  @Element
  private Scope scope;

  @Element
  private TableRole role;

  public TableAcl(TableRole role) {
    this();
    this.role = role;
  }

  public TableAcl() {
    this.scope = Scope.EMPTY_SCOPE;
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
   * @return the role
   */
  public TableRole getRole() {
    return role;
  }

  /**
   * @param role
   *          the role to set
   */
  public void setRole(TableRole role) {
    this.role = role;
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
    result = prime * result + ((role == null) ? 0 : role.hashCode());
    result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
    if (role != other.role)
      return false;
    if (scope == null) {
      if (other.scope != null)
        return false;
    } else if (!scope.equals(other.scope))
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
    builder.append("TableAcl [scope=");
    builder.append(scope);
    builder.append(", role=");
    builder.append(role);
    builder.append("]");
    return builder.toString();
  }

}