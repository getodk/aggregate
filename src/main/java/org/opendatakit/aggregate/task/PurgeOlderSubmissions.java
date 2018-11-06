/*
 * Copyright (C) 2011 University of Washington.
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
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Create the task that will purge all of a form's submissions older than the given date.
 *
 * @author mitchellsundt@gmail.com
 */
public interface PurgeOlderSubmissions {

  public static final String PURGE_DATE = "purgeBefore";

  public void createPurgeOlderSubmissionsTask(IForm form, SubmissionKey miscTasksKey,
                                              long attemptCount, CallingContext cc);
}
