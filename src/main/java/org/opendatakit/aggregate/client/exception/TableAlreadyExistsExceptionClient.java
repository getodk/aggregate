package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

public class TableAlreadyExistsExceptionClient extends RequestFailureException
		implements Serializable {

	  /**
	 * 
	 */
	private static final long serialVersionUID = -2016088929661465824L;

	public TableAlreadyExistsExceptionClient() {
		    super();
		  }

		  public TableAlreadyExistsExceptionClient(String message) {
		    super(message);
		  }

		  public TableAlreadyExistsExceptionClient(Throwable cause) {
		    super(cause);
		  }

		  public TableAlreadyExistsExceptionClient(String message, Throwable cause) {
		    super(message, cause);
		  }	
}
