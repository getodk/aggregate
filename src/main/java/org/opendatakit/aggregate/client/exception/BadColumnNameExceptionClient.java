package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

/**
 * Client-side bad column name exception for ODKTables.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class BadColumnNameExceptionClient extends RequestFailureException
		implements Serializable {

	  /**
	 * 
	 */
	private static final long serialVersionUID = -8226079661579405847L;

	public BadColumnNameExceptionClient() {
		    super();
		  }

		  public BadColumnNameExceptionClient(String message) {
		    super(message);
		  }

		  public BadColumnNameExceptionClient(Throwable cause) {
		    super(cause);
		  }

		  public BadColumnNameExceptionClient(String message, Throwable cause) {
		    super(message, cause);
		  }	
}
