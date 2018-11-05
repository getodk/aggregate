/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.exception;

import java.io.Serializable;

public class FormNotAvailableException extends RequestFailureException implements Serializable {

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
