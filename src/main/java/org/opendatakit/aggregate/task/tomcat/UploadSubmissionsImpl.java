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
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.aggregate.task.UploadSubmissionsWorkerImpl;
import org.opendatakit.common.web.CallingContext;

/**
 * This is a singleton bean.  It cannot have any per-request state.
 * It uses a static inner class to encapsulate the per-request state
 * of a running background task.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class UploadSubmissionsImpl implements UploadSubmissions {

  @Override
  public void createFormUploadTask(FormServiceCursor fsc, boolean onBackground, CallingContext cc) {
    WatchdogImpl wd = (WatchdogImpl) cc.getBean(BeanDefs.WATCHDOG);
    // use watchdog's calling context in runner...
    UploadSubmissionsRunner ur = new UploadSubmissionsRunner(fsc, wd.getFasterWatchdogCycleEnabled(), wd.getCallingContext());
    System.out.println("UPLOAD TASK IN TOMCAT");
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.execute(ur);
  }

  static class UploadSubmissionsRunner implements Runnable {
    final UploadSubmissionsWorkerImpl impl;

    public UploadSubmissionsRunner(FormServiceCursor fsc, boolean useLargerBatchSize, CallingContext cc) {
      impl = new UploadSubmissionsWorkerImpl(fsc, useLargerBatchSize, cc);
    }

    @Override
    public void run() {
      try {
        impl.uploadAllSubmissions();
      } catch (Exception e) {
        e.printStackTrace();
        // TODO: Problem - decide what to do if an exception occurs
      }
    }
  }
}
