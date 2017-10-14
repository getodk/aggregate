/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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
 * Exception for the case that a form already exists with the ODK specified
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ODKFormAlreadyExistsException extends Exception {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -5169305644093039120L;

  /**
   * Default constructor
   */
  public ODKFormAlreadyExistsException() {
    super();
  }
  
  /**
   * Construct exception with the error message
   * 
   * @param message
   *    exception message
   */
  public ODKFormAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Construction exception with error message and throwable cause
   * 
   * @param message
   *    exception message
   * @param cause
   *    throwable cause
   */
  public ODKFormAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construction exception with throwable cause
   * 
   * @param cause
   *    throwable cause
   */
  public ODKFormAlreadyExistsException(Throwable cause) {
    super(cause);
  }

}
