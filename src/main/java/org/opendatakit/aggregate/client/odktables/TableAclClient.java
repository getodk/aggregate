package org.opendatakit.aggregate.client.odktables;

import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.TableRole;

/**
 * This is the client-side version of
 * org.opendatakit.aggregate.odktables.entity. 
 * <br>
 * The idea is that this will be the client-side object that will perform
 * the same function. Now standard caveat applies that at this point it 
 * is not yet clear if this will be necessary, or if a new non-phone
 * object performing the same function needs to be created for the server.
 * @author sudar.sam@gmail.com
 *
 */
public class TableAclClient {

  private ScopeClient scope;

  private TableRoleClient role;

  public TableAclClient(TableRoleClient role) {
    this();
    this.role = role;
  }

  public TableAclClient() {
    this.scope = ScopeClient.EMPTY_SCOPE;
  }

  /**
   * @return the scope
   */
  public ScopeClient getScope() {
    return scope;
  }

  /**
   * @param scope
   *          the scope to set
   */
  public void setScope(ScopeClient scope) {
    this.scope = scope;
  }

  /**
   * @return the role
   */
  public TableRoleClient getRole() {
    return role;
  }

  /**
   * @param role
   *          the role to set
   */
  public void setRole(TableRoleClient role) {
    this.role = role;
  }
  
  /**
   * Transforms the object into a TableAcl object.
   */
  public TableAcl transform() {
	  TableAcl ta = new TableAcl();
	  switch (this.getRole()) {
	  case NONE:
		  ta.setRole(TableRole.NONE);
		  break;
	  case FILTERED_WRITER:
		  ta.setRole(TableRole.FILTERED_WRITER);
		  break;
	  case UNFILTERED_READER_FILTERED_WRITER:
		  ta.setRole(TableRole.UNFILTERED_READER_FILTERED_WRITER);
		  break;
	  case READER:
		  ta.setRole(TableRole.READER);
		  break;
	  case WRITER:
		  ta.setRole(TableRole.WRITER);
		  break;
	  case OWNER:
		  ta.setRole(TableRole.OWNER);
		  break;
	  default:
		  throw new IllegalStateException("No assignable permissions in transforming table role."); 		
	  }
	  ta.setScope(this.getScope().transform());
	  return ta;	  
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
    if (!(obj instanceof TableAclClient))
      return false;
    TableAclClient other = (TableAclClient) obj;
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