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
package org.opendatakit.aggregate.task.tomcat;

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.CsvGenerator;
import org.opendatakit.aggregate.task.CsvWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This is a singleton bean.  It cannot have any per-request state.
 * It uses a static inner class to encapsulate the per-request state
 * of a running background task.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class CsvGeneratorImpl implements CsvGenerator {

  @Override
  public void createCsvTask(IForm form, SubmissionKey persistentResultsKey,
                            long attemptCount, CallingContext cc)
      throws ODKDatastoreException {
    WatchdogImpl wd = (WatchdogImpl) cc.getBean(BeanDefs.WATCHDOG);
    // use watchdog's calling context in runner...
    CsvRunner runner = new CsvRunner(form, persistentResultsKey, attemptCount, wd.getCallingContext());
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.execute(runner);
  }

  static class CsvRunner implements Runnable {
    final CsvWorkerImpl impl;

    public CsvRunner(IForm form, SubmissionKey persistentResultsKey, long attemptCount, CallingContext cc) {
      impl = new CsvWorkerImpl(form, persistentResultsKey, attemptCount, cc);
    }

    @Override
    public void run() {
      impl.generateCsv();
    }
  }
}
