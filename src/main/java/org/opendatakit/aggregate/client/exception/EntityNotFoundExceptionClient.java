package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The error to be passed to the client when something requested has not been
 * found. The idea is that this is more specific than a generic 
 * DatstoreFailureException.
 * @author sudar.sam@gmail.com
 *
 */
public class EntityNotFoundExceptionClient extends Exception 
  implements Serializable, IsSerializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = -6024816637395105843L;
  
  private static final String ENTITY_NOT_FOUND_EXCEPTION_CLIENT = 
      "A requested entity was not found in the datastore";
  
  private String message;
  
  public EntityNotFoundExceptionClient() {
    super();
    message = ENTITY_NOT_FOUND_EXCEPTION_CLIENT;
  }
  
  public EntityNotFoundExceptionClient(String message, Throwable cause) {
    super(message, cause);
  }
  
  public EntityNotFoundExceptionClient(String message) {
    super(message);
    this.message = message;
  }
  
  public EntityNotFoundExceptionClient(Throwable cause) {
    super(cause);
    this.message = ENTITY_NOT_FOUND_EXCEPTION_CLIENT + " (" +
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
