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
 */
public class ODKIncompleteSubmissionData extends Exception {
  private Reason reason;

  public ODKIncompleteSubmissionData(Reason exceptionReason) {
    super();
    reason = exceptionReason;
  }

  public ODKIncompleteSubmissionData(String message, Reason exceptionReason) {
    super(message);
    reason = exceptionReason;
  }

  public ODKIncompleteSubmissionData(String message, Throwable cause, Reason exceptionReason) {
    super(message, cause);
    reason = exceptionReason;
  }

  public ODKIncompleteSubmissionData(Throwable cause, Reason exceptionReason) {
    super(cause);
    reason = exceptionReason;
  }

  public Reason getReason() {
    return reason;
  }

  public enum Reason {
    UNKNOWN,
    TITLE_MISSING,
    ID_MISSING,
    ID_MALFORMED,
    MISSING_XML,
    BAD_JR_PARSE,
    MISMATCHED_SUBMISSION_ELEMENT
  }
}
