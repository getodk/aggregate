package org.opendatakit.aggregate.odktables.exception;

public class RowEtagMismatchException extends ODKTablesException {

  private static final long serialVersionUID = 1L;

  public RowEtagMismatchException() {
    super();
  }

  public RowEtagMismatchException(String message) {
    super(message);
  }

  public RowEtagMismatchException(Throwable cause) {
    super(cause);
  }

  public RowEtagMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

}
