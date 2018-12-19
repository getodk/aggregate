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

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opendatakit.aggregate.client.form.KmlSelection;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class KmlGenerator {

  public void createKmlTask(IForm form, PersistentResults persistentResults, long attemptCount, CallingContext cc) throws ODKDatastoreException {
    Watchdog wd = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
    KmlWorkerImpl worker = new KmlWorkerImpl(form, persistentResults.getSubmissionKey(), attemptCount, getKmlElementsToInclude(persistentResults), wd.getCallingContext());
    AggregrateThreadExecutor.getAggregateThreadExecutor().execute(worker::generateKml);
  }

  private static List<KmlSelection> getKmlElementsToInclude(PersistentResults persistentResults) throws ODKDatastoreException {
    Map<String, String> params = persistentResults.getRequestParameters();
    if (params.get("KMLSELECTIONS") == null)
      return emptyList();

    String[] kmlSelection = params.get("KMLSELECTIONS").split("###");
    return Stream.of(kmlSelection)
        .map(KmlSelection::from)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
