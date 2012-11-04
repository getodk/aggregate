package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Client-side permission denied exception for use with ODKTables.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class PermissionDeniedExceptionClient extends Exception
		implements Serializable, IsSerializable {
  
  private static final String PERMISSION_DENIED_EXCEPTION_CLIENT = 
      "Permission denied for accessing data.";

	/**
	 * 
	 */
	private static final long serialVersionUID = -5069058480939683008L;
	
	private String message;
	
	public PermissionDeniedExceptionClient() {
	    super();
	    message = PERMISSION_DENIED_EXCEPTION_CLIENT;
	  }

	  /**
	   * @param message
	   * @param cause
	   */
	  public PermissionDeniedExceptionClient(String message, Throwable cause) {
	    super(message, cause);
	  }

	  /**
	   * @param message
	   */
	  public PermissionDeniedExceptionClient(String message) {
	    super(message);
	    this.message = message;
	  }

	  /**
	   * @param cause
	   */
	  public PermissionDeniedExceptionClient(Throwable cause) {
	    super(cause);
	    this.message = PERMISSION_DENIED_EXCEPTION_CLIENT + " (" + 
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
