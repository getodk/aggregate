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
package org.opendatakit.aggregate;

import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebStartup;
import org.opendatakit.common.web.CallingContext;

public class AggregateStartupImpl implements WebStartup {

	public AggregateStartupImpl() {
	}
	
	@Override
	public void doStartupAction(CallingContext bootstrapCc) {
		// be sure the FormInfo record is created.
		// note that this is called for every webserver instance.
		// additional logic is required to detect whether this 
		// is the first webserver instance.  We don't care.
		try {
			Form.initialize(bootstrapCc);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
		}
	}

}
