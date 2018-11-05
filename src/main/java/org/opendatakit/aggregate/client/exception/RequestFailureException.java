/*
 * Copyright (C) 2011 University of Washington
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

/**
 * Generic request failure exception for reporting server failures up to user.
 * These would include server consistency-failures, unexpected missing forms, etc.
 * (e.g., when accessing data while a delete-form task is running).
 * <p>
 * See also: AccessDeniedException, DatastoreFailureException
 *
 * @author mitchellsundt@gmail.com
 */
public class RequestFailureException extends Exception implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -2361548638923668909L;

  private String message;

  /**
   *
   */
  public RequestFailureException() {
    super();
    message = "RequestFailureException";
  }

  /**
   * @param arg0
   * @param arg1
   */
  public RequestFailureException(String arg0, Throwable arg1) {
    super(arg0, arg1);
    message = arg0 + "(" + arg1.getMessage() + ")";
  }

  /**
   * @param arg0
   */
  public RequestFailureException(String arg0) {
    super(arg0);
    message = arg0;
  }

  /**
   * @param arg0
   */
  public RequestFailureException(Throwable arg0) {
    super(arg0);
    message = "RequestFailureException (" + arg0.getMessage() + ")";
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
