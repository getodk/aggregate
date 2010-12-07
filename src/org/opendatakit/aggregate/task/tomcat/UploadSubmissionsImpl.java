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

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.task.AbstractUploadSubmissionImpl;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UploadSubmissionsImpl extends AbstractUploadSubmissionImpl implements Runnable {

  private FormServiceCursor fsc;
  private String baseServerWebUrl;
  private User user;
  
  @Override
  public void createFormUploadTask(FormServiceCursor fsc, String baseServerWebUrl, User user)
      throws ODKExternalServiceException {
    this.fsc = fsc;
    this.baseServerWebUrl = baseServerWebUrl;
    this.user = user;
    
    System.out.println("THIS IS UPLOAD TASK IN TOMCAT");
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.execute(this);
  }

  @Override
  public void run() {
    Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
    try {
      uploadSubmissions(fsc, baseServerWebUrl, ds, user);
    } catch (Exception e) {
      e.printStackTrace();
      // just move on as the task restarting the 
    }
    
  }

}
