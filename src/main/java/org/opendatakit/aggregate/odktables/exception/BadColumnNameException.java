package org.opendatakit.aggregate.odktables.exception;

public class BadColumnNameException extends ODKTablesException {

  private static final long serialVersionUID = 1L;

  public BadColumnNameException() {
    super();
  }

  public BadColumnNameException(String message) {
    super(message);
  }

  public BadColumnNameException(Throwable cause) {
    super(cause);
  }

  public BadColumnNameException(String message, Throwable cause) {
    super(message, cause);
  }

}
