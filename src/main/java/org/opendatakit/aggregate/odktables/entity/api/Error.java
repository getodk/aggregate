package org.opendatakit.aggregate.odktables.entity.api;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(DefaultType.FIELD)
public class Error {
  private ErrorType type;
  private String message;

  @SuppressWarnings("unused")
  private Error() {
  }

  /**
   * @param type
   * @param message
   */
  public Error(ErrorType type, String message) {
    this.type = type;
    this.message = message;
  }

  /**
   * @return the type
   */
  public ErrorType getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(ErrorType type) {
    this.type = type;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message
   *          the message to set
   */
  public void setMessage(String message) {
    this.message = message;
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
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    if (!(obj instanceof Error))
      return false;
    Error other = (Error) obj;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    if (type != other.type)
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
    builder.append("Error [type=");
    builder.append(type);
    builder.append(", message=");
    builder.append(message);
    builder.append("]");
    return builder.toString();
  }

  public enum ErrorType {
    ETAG_MISMATCH,
    TABLE_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_NOT_FOUND,
    BAD_COLUMN_NAME,
    INTERNAL_ERROR,
    BAD_REQUEST,
    LOCK_TIMEOUT,
  }
}
