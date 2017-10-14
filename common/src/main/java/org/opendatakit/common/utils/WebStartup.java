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
package org.opendatakit.common.utils;

import org.opendatakit.common.web.CallingContext;

public interface WebStartup {
	/**
	 * Guaranteed to be called by one webserver at a time during the
	 * initialization of that webserver container.  The CallingContext
	 * does not support getBean(), just getDatastore() and getCurrentUser().
	 * Implementors must have all beans they need assigned during their
	 * bean initialization.
	 * 
	 * There may be other webservers already running -- this does not 
	 * guarantee any first-to-run behavior.
	 * 
	 * @param bootstrapCc
	 */
	public void doStartupAction( CallingContext bootstrapCc );

}
