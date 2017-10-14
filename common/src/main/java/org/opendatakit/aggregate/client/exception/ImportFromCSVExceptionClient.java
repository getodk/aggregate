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

/**
 * Exception to be passed back to the client in case there is an exception
 * importing from the CSV.
 * @author sudar.sam@gmail.com
 *
 */
public class ImportFromCSVExceptionClient extends Exception
    implements Serializable {

  private static final String IMPORT_FROM_CSV_EXCEPTION_CLIENT =
      "Problem importing from CSV";

  /**
   *
   */
  private static final long serialVersionUID = -5810373535896498178L;

  private String message;

  public ImportFromCSVExceptionClient() {
    super();
    message = IMPORT_FROM_CSV_EXCEPTION_CLIENT;
  }

  public ImportFromCSVExceptionClient(String message, Throwable cause) {
    super(message, cause);
  }

  public ImportFromCSVExceptionClient(String message) {
    super(message);
    this.message = message;
  }

  public ImportFromCSVExceptionClient(Throwable cause) {
    super(cause);
    this.message = IMPORT_FROM_CSV_EXCEPTION_CLIENT + " (" +
        cause.getMessage() + ")";
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
