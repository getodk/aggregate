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

package org.opendatakit.common.persistence.client.exception;

import java.io.Serializable;

/**
 * Datastore failure exception that can be returned through GWT.
 *
 * @author mitchellsundt@gmail.com
 */
public class DatastoreFailureException extends Exception implements Serializable {
  private static final String DATASTORE_FAILURE_EXCEPTION = "Problem persisting data or accessing data";

  private static final long serialVersionUID = 3102327639058143399L;

  private String message;

  public DatastoreFailureException() {
    super();
    message = DATASTORE_FAILURE_EXCEPTION;
  }

  public DatastoreFailureException(String arg0, Throwable arg1) {
    super(arg0, arg1);
    message = arg0 + "(" + arg1.getMessage() + ")";
  }

  public DatastoreFailureException(String arg0) {
    super(arg0);
    message = arg0;
  }

  public DatastoreFailureException(Throwable arg0) {
    super(arg0);
    message = DATASTORE_FAILURE_EXCEPTION + " (" + arg0.getMessage() + ")";
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
