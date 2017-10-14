/*
 * Copyright (C) 2009 Google Inc.
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.exception;

/**
 * Exception for the case when part of the submission is missing causing
 * problems with systems ability to display properly
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ODKIncompleteSubmissionData extends Exception {
  
  public enum Reason {
    UNKNOWN,
    TITLE_MISSING,
    ID_MISSING,
    ID_MALFORMED,
    MISSING_XML,
    BAD_JR_PARSE,
    MISMATCHED_SUBMISSION_ELEMENT;
  }
  
  private Reason reason;

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -8894929454515911356L;

  /**
   * Default constructor
   */
  @SuppressWarnings("unused")
  private ODKIncompleteSubmissionData() {
    this(Reason.UNKNOWN);
  }

  /**
   * Construct exception with the error message
   * 
   * @param message exception message
   */
  @SuppressWarnings("unused")
  private ODKIncompleteSubmissionData(String message) {
    this(message, Reason.UNKNOWN);
  }

  /**
   * Construction exception with error message and throwable cause
   * 
   * @param message exception message
   * @param cause throwable cause
   */
  @SuppressWarnings("unused")
  private ODKIncompleteSubmissionData(String message, Throwable cause) {
    this(message, cause, Reason.UNKNOWN);
  }

  /**
   * Construction exception with throwable cause
   * 
   * @param cause throwable cause
   */
  @SuppressWarnings("unused")
  private ODKIncompleteSubmissionData(Throwable cause) {
    this(cause, Reason.UNKNOWN);
  }

  /**
   * Default constructor with reason
   * 
   * @param exceptionReason exception reason
   */
  public ODKIncompleteSubmissionData(Reason exceptionReason) {
    super();
    reason = exceptionReason;
  }

  /**
   * Construct exception with the error message and reason
   * 
   * @param message exception message
   * @param exceptionReason exception reason
   */
  public ODKIncompleteSubmissionData(String message, Reason exceptionReason) {
    super(message);
    reason = exceptionReason;
  }

  /**
   * Construction exception with error message, throwable cause, and reason
   * 
   * @param message exception message
   * @param cause throwable cause
   * @param exceptionReason exception reason
   */
  public ODKIncompleteSubmissionData(String message, Throwable cause, Reason exceptionReason) {
    super(message, cause);
    reason = exceptionReason;
  }

  /**
   * Construction exception with throwable cause and reason
   * 
   * @param cause throwable cause
   * @param exceptionReason exception reason
   */
  public ODKIncompleteSubmissionData(Throwable cause, Reason exceptionReason) {
    super(cause);
    reason = exceptionReason;
  }
  
  /**
   * Get the reason why the exception was generated
   * @return the reason
   */
  public Reason getReason() {
    return reason;
  }
}
