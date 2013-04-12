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
 *
 */
public interface UploadSubmissions {

  /**
   * Fire off an action to publish data.  The onBackground argument
   * provides a hint to the implementation as to whether to run this
   * request using the background instance or the frontend instance.
   *
   * If this is triggered via a submission or is the result of
   * creating a streaming-only publisher, then the request should
   * be run on the frontend instance to minimize GAE quota.
   *
   * If it is publishing data from the beginning of time, then it
   * should be run on the background thread.
   *
   * @param fsc
   * @param onBackground
   * @param cc
   * @throws ODKExternalServiceException
   */
  public void createFormUploadTask(FormServiceCursor fsc, boolean onBackground, CallingContext cc)
      throws ODKExternalServiceException;
}
