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

public class TableAlreadyExistsExceptionClient extends RequestFailureException
		implements Serializable {

	  /**
	 *
	 */
	private static final long serialVersionUID = -2016088929661465824L;

	public TableAlreadyExistsExceptionClient() {
		    super();
		  }

		  public TableAlreadyExistsExceptionClient(String message) {
		    super(message);
		  }

		  public TableAlreadyExistsExceptionClient(Throwable cause) {
		    super(cause);
		  }

		  public TableAlreadyExistsExceptionClient(String message, Throwable cause) {
		    super(message, cause);
		  }
}
