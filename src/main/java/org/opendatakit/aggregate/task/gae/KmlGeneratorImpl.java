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

import java.util.Map;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.task.KmlGenerator;
import org.opendatakit.aggregate.task.gae.servlet.KmlGeneratorTaskServlet;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
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
public class KmlGeneratorImpl implements KmlGenerator {

  @Override
  public void createKmlTask(IForm form, PersistentResults persistentResults, long attemptCount,
      CallingContext cc) throws ODKDatastoreException, ODKFormNotFoundException {
    Map<String, String> params = persistentResults.getRequestParameters();
    
    TaskOptionsBuilder b = new TaskOptionsBuilder(KmlGeneratorTaskServlet.ADDR);
    b.countdownMillis(PersistConsts.MAX_SETTLE_MILLISECONDS);
    b.param(ServletConsts.FORM_ID, form.getFormId());
    b.param(ServletConsts.PERSISTENT_RESULTS_KEY, persistentResults.getSubmissionKey().toString());
    b.param(ServletConsts.ATTEMPT_COUNT, Long.toString(attemptCount));
    b.param(KmlGenerator.GEOPOINT_FIELD, params.get(KmlGenerator.GEOPOINT_FIELD));
    b.param(KmlGenerator.TITLE_FIELD, params.get(KmlGenerator.TITLE_FIELD));
    b.param(KmlGenerator.IMAGE_FIELD, params.get(KmlGenerator.IMAGE_FIELD));
    b.enqueue();
  }
}
