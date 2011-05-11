/*
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
 * Thrown if you attempt to delete a form that has external 
 * services configured for forwarding.  The user should 
 * manually delete the external service associations before
 * deleting the form.  Prevents the user from deleting a 
 * form before all data has been uploaded off the server.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public class ODKExternalServiceDependencyException extends Exception {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 2319914089199375319L;

  /**
   * Default constructor
   */
  public ODKExternalServiceDependencyException() {
    super();
  }

  /**
   * Construct exception with the error message
   * 
   * @param message exception message
   */
  public ODKExternalServiceDependencyException(String message) {
    super(message);
  }

  /**
   * Construction exception with error message and throwable cause
   * 
   * @param message exception message
   * @param cause throwable cause
   */
  public ODKExternalServiceDependencyException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construction exception with throwable cause
   * 
   * @param cause throwable cause
   */
  public ODKExternalServiceDependencyException(Throwable cause) {
    super(cause);
  }
}

