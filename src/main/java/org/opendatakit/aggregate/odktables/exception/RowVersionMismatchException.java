package org.opendatakit.aggregate.odktables.exception;

public class RowVersionMismatchException extends ODKTablesException {

  private static final long serialVersionUID = 1L;

  public RowVersionMismatchException() {
    super();
  }

  public RowVersionMismatchException(String message) {
    super(message);
  }

  public RowVersionMismatchException(Throwable cause) {
    super(cause);
  }

  public RowVersionMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

}
