package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

/**
 * Client-side permission denied exception for use with ODKTables.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class PermissionDeniedExceptionClient extends RequestFailureException
		implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5069058480939683008L;
	
	public PermissionDeniedExceptionClient() {
	    super();
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
	  }

	  /**
	   * @param cause
	   */
	  public PermissionDeniedExceptionClient(Throwable cause) {
	    super(cause);
	  }

}	
