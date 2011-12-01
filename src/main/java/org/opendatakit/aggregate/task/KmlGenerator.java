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
package org.opendatakit.aggregate.task;

import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * API for creating and restarting Kml generation tasks.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public interface KmlGenerator {

	public static final String NONE = "None";
	public static final String TITLE_FIELD = "titleField";
	public static final String IMAGE_FIELD = "imageField";
	public static final String GEOPOINT_FIELD = "geopointField";

	public void createKmlTask(IForm form, PersistentResults persistentResult,
			long attemptCount, CallingContext cc) throws ODKDatastoreException, ODKFormNotFoundException;
}
