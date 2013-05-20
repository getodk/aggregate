package org.opendatakit.aggregate.odktables.exception;

import java.io.Serializable;

public class PermissionDeniedException extends ODKTablesException
	implements Serializable {

  // private static final long serialVersionUID = 1L;

  /**
	 * 
	 */
	private static final long serialVersionUID = -6853618331957236951L;

public PermissionDeniedException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public PermissionDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public PermissionDeniedException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public PermissionDeniedException(Throwable cause) {
    super(cause);
  }

}
