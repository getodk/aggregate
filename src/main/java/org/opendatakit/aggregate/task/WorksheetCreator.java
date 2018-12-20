/*
 * Copyright (C) 2010 University of Washington
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

import static org.opendatakit.aggregate.constants.ServletConsts.EXTERNAL_SERVICE_TYPE;
import static org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts.EXT_SERV_ADDRESS;

import java.util.Map;
import java.util.Objects;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class WorksheetCreator {

  public final void createWorksheetTask(IForm form, MiscTasks miscTasks, long attemptCount, CallingContext cc) throws ODKDatastoreException {
    Map<String, String> params = miscTasks.getRequestParameters();
    String spreadsheetName = Objects.requireNonNull(
        params.get(EXT_SERV_ADDRESS),
        "Spreadsheet name is null in create worksheet task"
    );
    ExternalServicePublicationOption publicationOption = ExternalServicePublicationOption.valueOf(Objects.requireNonNull(
        params.get(EXTERNAL_SERVICE_TYPE),
        "No external service type specified on create worksheet task"
    ));
    Watchdog wd = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
    WorksheetCreatorWorkerImpl worker = new WorksheetCreatorWorkerImpl(
        form,
        miscTasks.getSubmissionKey(),
        attemptCount,
        spreadsheetName,
        publicationOption,
        wd.getCallingContext()
    );
    AggregrateThreadExecutor.getAggregateThreadExecutor().execute(worker::worksheetCreator);
  }

}
