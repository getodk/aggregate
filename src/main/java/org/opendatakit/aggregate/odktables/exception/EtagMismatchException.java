package org.opendatakit.aggregate.odktables.exception;

public class EtagMismatchException extends ODKTablesException {

  private static final long serialVersionUID = 1L;

  public EtagMismatchException() {
    super();
  }

  public EtagMismatchException(String message) {
    super(message);
  }

  public EtagMismatchException(Throwable cause) {
    super(cause);
  }

  public EtagMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

}
