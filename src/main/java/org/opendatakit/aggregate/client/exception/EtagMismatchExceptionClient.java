package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

public class EtagMismatchExceptionClient extends RequestFailureException
		implements Serializable {

	  /**
	 * 
	 */
	private static final long serialVersionUID = 8450102254267803857L;

	public EtagMismatchExceptionClient() {
		    super();
		  }

		  public EtagMismatchExceptionClient(String message) {
		    super(message);
		  }

		  public EtagMismatchExceptionClient(Throwable cause) {
		    super(cause);
		  }

		  public EtagMismatchExceptionClient(String message, Throwable cause) {
		    super(message, cause);
		  }	
}
