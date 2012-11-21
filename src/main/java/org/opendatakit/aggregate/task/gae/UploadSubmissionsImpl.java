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
package org.opendatakit.aggregate.task.gae;

import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.aggregate.task.gae.servlet.UploadSubmissionsTaskServlet;
import org.opendatakit.aggregate.util.BackendActionsTable;
import org.opendatakit.common.web.CallingContext;

/**
 * This is a singleton bean. It cannot have any per-request state. It uses a
 * static inner class to encapsulate the per-request state of a running
 * background task.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UploadSubmissionsImpl implements UploadSubmissions {

  @Override
  public void createFormUploadTask(FormServiceCursor fsc, CallingContext cc)
      throws ODKExternalServiceException {

    try {
      System.out.println("Creating "
          + fsc.getExternalServicePublicationOption().toString().toLowerCase() + " upload task: "
          + fsc.getExternalServiceType());
      
      TaskOptionsBuilder b = new TaskOptionsBuilder(UploadSubmissionsTaskServlet.ADDR);
      b.countdownMillis(BackendActionsTable.PUBLISHING_DELAY_MILLISECONDS);
      b.param(ExternalServiceConsts.FSC_URI_PARAM, fsc.getUri());
      b.enqueue(TaskOptionsBuilder.FRONTEND_QUEUE);
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }

  }

}
