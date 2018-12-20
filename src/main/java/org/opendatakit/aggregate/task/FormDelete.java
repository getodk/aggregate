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
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormDelete {
  private static final Logger logger = LoggerFactory.getLogger(FormDelete.class);

  public void createFormDeleteTask(IForm form, SubmissionKey miscTasksKey, long attemptCount, CallingContext cc) {
    Watchdog wd = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
    FormDeleteWorkerImpl worker = new FormDeleteWorkerImpl(form, miscTasksKey, attemptCount, wd.getCallingContext());
    AggregrateThreadExecutor.getAggregateThreadExecutor().execute(() -> {
      try {
        worker.deleteForm();
      } catch (ODKDatastoreException e) {
        logger.error("Error deleting form", e);
      }
    });
  }
}
