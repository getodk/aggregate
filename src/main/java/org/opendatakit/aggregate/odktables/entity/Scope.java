package org.opendatakit.aggregate.odktables.entity;

import org.apache.commons.lang.Validate;
import org.opendatakit.aggregate.client.odktables.ScopeClient;
import org.simpleframework.xml.Element;

public class Scope {

  public static final Scope EMPTY_SCOPE;
  static {
    EMPTY_SCOPE = new Scope();
    EMPTY_SCOPE.initFields(null, null);
  }

  public enum Type {
    DEFAULT,
    USER,
    GROUP,
  }

  @Element(required = false)
  private Type type;

  @Element(required = false)
  private String value;

  /**
   * Constructs a new Scope.
   * 
   * @param type
   *          the type of the scope. Must not be null. The empty scope may be
   *          accessed as {@link Scope#EMPTY_SCOPE}.
   * @param value
   *          the userId if type is {@link Type#USER}, or the groupId of type is
   *          {@link Type#GROUP}. If type is {@link Type#DEFAULT}, value is
   *          ignored (set to null).
   */
  public Scope(Type type, String value) {
    Validate.notNull(type);
    if (type.equals(Type.GROUP)) {
      Validate.notEmpty(value);
    } else if (type.equals(Type.DEFAULT)) {
      value = null;
    }

    initFields(type, value);
  }

  private Scope() {
  }

  private void initFields(Type type, String value) {
    this.type = type;
    this.value = value;
  }

  /**
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }
  
  
  // this is the transform to the clientside scope
  // this might be defective, so i'm commenting it out for now
  //--some issue about "this" being null sometimes? errors at the 
  // switch statement.
  public ScopeClient transform() {
	  // First get the type of this scope
	  ScopeClient sc = null;
	  switch(this.getType()) {
		  case DEFAULT:
			  sc = new ScopeClient(ScopeClient.Type.DEFAULT, this.getValue());
			  break;
		  case USER:
			  sc = new ScopeClient(ScopeClient.Type.USER, this.getValue());
			  break;
		  case GROUP:
			  sc = new ScopeClient(ScopeClient.Type.GROUP, this.getValue());
			  break;
	  }
	  if (sc == null) sc = ScopeClient.EMPTY_SCOPE;
	  
	  return sc;
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
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    if (!(obj instanceof Scope))
      return false;
    Scope other = (Scope) obj;
    if (type != other.type)
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
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
    builder.append("Scope [type=");
    builder.append(type);
    builder.append(", value=");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }

}