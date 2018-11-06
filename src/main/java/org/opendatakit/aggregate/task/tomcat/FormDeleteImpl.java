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
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.FormDelete;
import org.opendatakit.aggregate.task.FormDeleteWorkerImpl;
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
public class FormDeleteImpl implements FormDelete {

  @Override
  public final void createFormDeleteTask(IForm form, SubmissionKey miscTasksKey,
                                         long attemptCount, CallingContext cc) {
    WatchdogImpl wd = (WatchdogImpl) cc.getBean(BeanDefs.WATCHDOG);
    // use watchdog's calling context in runner...
    FormDeleteRunner dr = new FormDeleteRunner(form, miscTasksKey, attemptCount, wd.getCallingContext());
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.execute(dr);
  }

  static class FormDeleteRunner implements Runnable {
    final FormDeleteWorkerImpl impl;

    public FormDeleteRunner(IForm form, SubmissionKey miscTasksKey,
                            long attemptCount, CallingContext cc) {
      impl = new FormDeleteWorkerImpl(form, miscTasksKey, attemptCount, cc);
    }

    @Override
    public void run() {
      try {
        impl.deleteForm();
      } catch (Exception e) {
        e.printStackTrace();
        // TODO: Problem - decide what to do if an exception occurs
      }
    }
  }
}
