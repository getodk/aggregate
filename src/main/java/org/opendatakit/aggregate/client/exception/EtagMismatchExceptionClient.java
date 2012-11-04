package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EtagMismatchExceptionClient extends Exception implements Serializable, IsSerializable {

  private static final String ETAG_MISMATCH_EXCEPTION_CLIENT = 
      "Etag mismatch";

  /**
	 * 
	 */
  private static final long serialVersionUID = 8450102254267803857L;

  private String message;

  public EtagMismatchExceptionClient() {
    super();
    message = ETAG_MISMATCH_EXCEPTION_CLIENT;
  }

  public EtagMismatchExceptionClient(String message) {
    super(message);
    this.message = message;
  }

  public EtagMismatchExceptionClient(Throwable cause) {
    super(cause);
    this.message = ETAG_MISMATCH_EXCEPTION_CLIENT + " (" +
        cause.getMessage() + ")";
  }

  public EtagMismatchExceptionClient(String message, Throwable cause) {
    super(message, cause);
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
