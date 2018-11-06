/*
 * Copyright (C) 2012 University of Washington
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

package org.opendatakit.aggregate.task.gae;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.JsonFileGenerator;
import org.opendatakit.aggregate.task.gae.servlet.JsonGeneratorTaskServlet;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This is a singleton bean for generating Json
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class JsonFileGeneratorImpl implements JsonFileGenerator {

  @Override
  public void createJsonFileTask(IForm form, SubmissionKey persistentResultsKey, long attemptCount,
                                 CallingContext cc) {
    TaskOptionsBuilder b = new TaskOptionsBuilder(JsonGeneratorTaskServlet.ADDR);
    b.countdownMillis(PersistConsts.MAX_SETTLE_MILLISECONDS);
    b.param(ServletConsts.FORM_ID, form.getFormId());
    b.param(ServletConsts.PERSISTENT_RESULTS_KEY, persistentResultsKey.toString());
    b.param(ServletConsts.ATTEMPT_COUNT, Long.toString(attemptCount));
    b.enqueue();

  }

}
