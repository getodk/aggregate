/*
 * Copyright (C) 2010 University of Washington
 * Copyright (C) 2018 Nafundi
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

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadSubmissions {
  private static final Logger logger = LoggerFactory.getLogger(UploadSubmissions.class);

  public void createFormUploadTask(FormServiceCursor fsc, boolean onBackground, CallingContext cc) {
    Watchdog wd = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
    UploadSubmissionsWorkerImpl worker = new UploadSubmissionsWorkerImpl(fsc, wd.getFasterWatchdogCycleEnabled(), wd.getCallingContext());
    AggregrateThreadExecutor.getAggregateThreadExecutor().execute(() -> {
      try {
        worker.uploadAllSubmissions();
      } catch (ODKEntityNotFoundException | ODKExternalServiceException e) {
        logger.error("Error uploading all submissions", e);
      }
    });
  }
}
