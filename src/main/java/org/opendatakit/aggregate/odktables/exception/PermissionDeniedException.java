package org.opendatakit.aggregate.odktables.exception;

public class PermissionDeniedException extends ODKTablesException {

  private static final long serialVersionUID = 1L;

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
