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
package org.opendatakit.aggregate.task;

import java.util.List;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public abstract class AbstractWorksheetCreatorImpl implements WorksheetCreator {

  private final GoogleSpreadsheet getGoogleSpreadsheetWithName(Form form, String spreadsheetName,
      String baseWebServerUrl, Datastore datastore, User user) throws ODKDatastoreException {
    List<ExternalService> remoteServers = FormServiceCursor.getExternalServicesForForm(form,
        baseWebServerUrl, datastore, user);

    if (remoteServers == null) {
      return null;
    }

    // find spreadsheet with name
    for (ExternalService rs : remoteServers) {
      if (rs instanceof GoogleSpreadsheet) {
        GoogleSpreadsheet sheet = (GoogleSpreadsheet) rs;

        if (sheet.getSpreadsheetName().equals(spreadsheetName)) {
          return sheet;
        }
      }
    }

    return null;
  }

  public final void worksheetCreator(String baseWebServerUrl, String spreadsheetName,
      ExternalServiceOption esType, Form form, Datastore ds, User user)
      throws ODKExternalServiceException, ODKDatastoreException {

    // get spreadsheet
    GoogleSpreadsheet spreadsheet = getGoogleSpreadsheetWithName(form, spreadsheetName,
        baseWebServerUrl, ds, user);

    // verify form has a spreadsheet element
    if (spreadsheet == null) {
      throw new ODKExternalServiceException("unable to find spreadsheet");
    }

    // generate worksheets
    try {
      spreadsheet.generateWorksheets();
    } catch (Exception e) {
      throw new ODKExternalServiceException(e);
    }
    
    // if we need to upload submissions, start a task to do so
    if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
      UploadSubmissions uploadTask = (UploadSubmissions) ContextFactory.get().getBean(
          BeanDefs.UPLOAD_TASK_BEAN);
      uploadTask.createFormUploadTask(spreadsheet.getFormServiceCursor(), baseWebServerUrl, user);
    }
  }
}
