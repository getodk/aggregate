/*
 * Copyright (C) 2011 University of Washington.
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
import org.opendatakit.common.web.CallingContext;

public class PurgeOlderSubmissions {

  public static final String PURGE_DATE = "purgeBefore";

  public final void createPurgeOlderSubmissionsTask(IForm form, SubmissionKey miscTasksKey, long attemptCount, CallingContext cc) {
    Watchdog wd = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
    PurgeOlderSubmissionsWorkerImpl worker = new PurgeOlderSubmissionsWorkerImpl(form, miscTasksKey, attemptCount, wd.getCallingContext());
    AggregrateThreadExecutor.getAggregateThreadExecutor().execute(worker::purgeOlderSubmissions);
  }
}
