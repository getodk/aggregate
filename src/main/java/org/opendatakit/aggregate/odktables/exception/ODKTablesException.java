package org.opendatakit.aggregate.odktables.exception;

/**
 * @author the.dylan.price@gmail.com
 */
public class ODKTablesException extends Exception {

  private static final long serialVersionUID = 1L;

  public ODKTablesException() {
  }

  public ODKTablesException(String message) {
    super(message);
  }

  public ODKTablesException(Throwable cause) {
    super(cause);
  }

  public ODKTablesException(String message, Throwable cause) {
    super(message, cause);
  }

}
