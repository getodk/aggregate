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
 * Client-side permission denied exception for use with ODKTables.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class PermissionDeniedExceptionClient extends Exception
		implements Serializable {

  private static final String PERMISSION_DENIED_EXCEPTION_CLIENT =
      "Permission denied for accessing data.";

	/**
	 *
	 */
	private static final long serialVersionUID = -5069058480939683008L;

	private String message;

	public PermissionDeniedExceptionClient() {
	    super();
	    message = PERMISSION_DENIED_EXCEPTION_CLIENT;
	  }

	  /**
	   * @param message
	   * @param cause
	   */
	  public PermissionDeniedExceptionClient(String message, Throwable cause) {
	    super(message, cause);
	  }

	  /**
	   * @param message
	   */
	  public PermissionDeniedExceptionClient(String message) {
	    super(message);
	    this.message = message;
	  }

	  /**
	   * @param cause
	   */
	  public PermissionDeniedExceptionClient(Throwable cause) {
	    super(cause);
	    this.message = PERMISSION_DENIED_EXCEPTION_CLIENT + " (" +
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
