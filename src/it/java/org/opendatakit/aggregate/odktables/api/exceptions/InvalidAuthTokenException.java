/*
 * Copyright (C) 2012 University of Washington
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
package org.opendatakit.aggregate.odktables.api.exceptions;

public class InvalidAuthTokenException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidAuthTokenException() {
    super();
  }

  /**
   * @param detailMessage
   * @param throwable
   */
  public InvalidAuthTokenException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  /**
   * @param detailMessage
   */
  public InvalidAuthTokenException(String detailMessage) {
    super(detailMessage);
  }

  /**
   * @param throwable
   */
  public InvalidAuthTokenException(Throwable throwable) {
    super(throwable);
  }

}
