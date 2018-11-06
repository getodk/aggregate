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

package org.opendatakit.aggregate.task.tomcat;

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.JsonFileGenerator;
import org.opendatakit.aggregate.task.JsonFileWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This is a singleton bean. It cannot have any per-request state. It uses a
 * static inner class to encapsulate the per-request state of a running
 * background task.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class JsonFileGeneratorImpl implements JsonFileGenerator {

  @Override
  public void createJsonFileTask(IForm form, SubmissionKey persistentResultsKey, long attemptCount,
                                 CallingContext cc) {
    WatchdogImpl wd = (WatchdogImpl) cc.getBean(BeanDefs.WATCHDOG);
    // use watchdog's calling context in runner...
    JsonRunner runner = new JsonRunner(form, persistentResultsKey, attemptCount, wd.getCallingContext());
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.execute(runner);

  }

  static class JsonRunner implements Runnable {
    final JsonFileWorkerImpl impl;

    public JsonRunner(IForm form, SubmissionKey persistentResultsKey, long attemptCount, CallingContext cc) {
      impl = new JsonFileWorkerImpl(form, persistentResultsKey, attemptCount, cc);
    }

    @Override
    public void run() {
      impl.generateJsonFile();
    }
  }

}
