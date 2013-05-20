package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Exception to be passed back to the client in case there is an exception
 * importing from the CSV.
 * @author sudar.sam@gmail.com
 *
 */
public class ImportFromCSVExceptionClient extends Exception
    implements Serializable, IsSerializable {
  
  private static final String IMPORT_FROM_CSV_EXCEPTION_CLIENT = 
      "Problem importing from CSV";

  /**
   * 
   */
  private static final long serialVersionUID = -5810373535896498178L;
  
  private String message;
  
  public ImportFromCSVExceptionClient() {
    super();
    message = IMPORT_FROM_CSV_EXCEPTION_CLIENT;
  }
  
  public ImportFromCSVExceptionClient(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ImportFromCSVExceptionClient(String message) {
    super(message);
    this.message = message;
  }
  
  public ImportFromCSVExceptionClient(Throwable cause) {
    super(cause);
    this.message = IMPORT_FROM_CSV_EXCEPTION_CLIENT + " (" +
        cause.getMessage() + ")";
  }
  
  @Override
  public String getLocalizedMessage() {
    return message;
  }
  
  @Override
  public String getMessage() {
    return message;
  }

}
