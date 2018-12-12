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

import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.common.web.CallingContext;

/**
 * API for creation of form upload (to an external service) tasks.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface UploadSubmissions {

  void createFormUploadTask(FormServiceCursor fsc, boolean onBackground, CallingContext cc)
      throws ODKExternalServiceException;
}
