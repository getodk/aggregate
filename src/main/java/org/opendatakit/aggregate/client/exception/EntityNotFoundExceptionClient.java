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
 * The error to be passed to the client when something requested has not been
 * found. The idea is that this is more specific than a generic
 * DatstoreFailureException.
 * @author sudar.sam@gmail.com
 *
 */
public class EntityNotFoundExceptionClient extends Exception
  implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -6024816637395105843L;

  private static final String ENTITY_NOT_FOUND_EXCEPTION_CLIENT =
      "A requested entity was not found in the datastore";

  private String message;

  public EntityNotFoundExceptionClient() {
    super();
    message = ENTITY_NOT_FOUND_EXCEPTION_CLIENT;
  }

  public EntityNotFoundExceptionClient(String message, Throwable cause) {
    super(message, cause);
  }

  public EntityNotFoundExceptionClient(String message) {
    super(message);
    this.message = message;
  }

  public EntityNotFoundExceptionClient(Throwable cause) {
    super(cause);
    this.message = ENTITY_NOT_FOUND_EXCEPTION_CLIENT + " (" +
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
