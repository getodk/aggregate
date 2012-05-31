package org.opendatakit.aggregate.odktables.exception;

public class TableAlreadyExistsException extends ODKTablesException {
  private static final long serialVersionUID = 1L;

  public TableAlreadyExistsException() {
    super();
  }

  public TableAlreadyExistsException(String message) {
    super(message);
  }

  public TableAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  public TableAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
