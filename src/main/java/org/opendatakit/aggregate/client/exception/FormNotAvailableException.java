package org.opendatakit.aggregate.client.exception;

public class FormNotAvailableException extends RequestFailureException {

  private static final String DEFAULT_MSG = "FormNoLongerAvailableException: Form is no longer available from Aggregate";

  private static final long serialVersionUID = 4767530079905737361L;

  private String message;

  /**
   * 
   */
  public FormNotAvailableException() {
     super();
     message = DEFAULT_MSG;
  }

  /**
   * @param arg0
   * @param arg1
   */
  public FormNotAvailableException(String arg0, Throwable arg1) {
     super(arg0, arg1);
     message = arg0 + "(" + arg1.getMessage() + ")";
  }

  /**
   * @param arg0
   */
  public FormNotAvailableException(String arg0) {
     super(arg0);
     message = arg0;
  }

  /**
   * @param arg0
   */
  public FormNotAvailableException(Throwable arg0) {
     super(arg0);
     message = DEFAULT_MSG + "(" + arg0.getMessage() + ")";
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
