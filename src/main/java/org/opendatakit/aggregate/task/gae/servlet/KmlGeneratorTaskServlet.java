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
package org.opendatakit.aggregate.task.gae.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.KmlGenerator;
import org.opendatakit.aggregate.task.KmlWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class KmlGeneratorTaskServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 8647919526257827291L;

  private static final Log logger = LogFactory.getLog(KmlGeneratorTaskServlet.class);

  /**
   * URI from base
   */
  public static final String ADDR = "gae/kmlGeneratorTask";

  /**
   * Handler for HTTP Get request to create xform upload page
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    cc.setAsDaemon(true);

    // get parameter
    final String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      logger.error("Missing " + ServletConsts.FORM_ID + " key");
      errorMissingKeyParam(resp);
      return;
    }
    final String persistentResultsString = getParameter(req, ServletConsts.PERSISTENT_RESULTS_KEY);
    if (persistentResultsString == null) {
      logger.error("Missing " + ServletConsts.PERSISTENT_RESULTS_KEY + " key");
      errorBadParam(resp);
      return;
    }
    SubmissionKey persistentResultsKey = new SubmissionKey(persistentResultsString);
    final String attemptCountString = getParameter(req, ServletConsts.ATTEMPT_COUNT);
    if (attemptCountString == null) {
      logger.error("Missing " + ServletConsts.ATTEMPT_COUNT + " key");
      errorBadParam(resp);
      return;
    }
    Long attemptCount = 1L;
    try {
      attemptCount = Long.valueOf(attemptCountString);
    } catch (Exception e) {
      logger.error("Invalid " + ServletConsts.ATTEMPT_COUNT + " value: " + attemptCountString
          + " exception: " + e.toString());
      errorBadParam(resp);
      return;
    }
    // optional fields
    String geopointFieldName = getParameter(req, KmlGenerator.GEOPOINT_FIELD);
    String titleFieldName = getParameter(req, KmlGenerator.TITLE_FIELD);
    String imageFieldName = getParameter(req, KmlGenerator.IMAGE_FIELD);

    IForm form = null;
    FormElementModel titleField = null;
    FormElementModel geopointField = null;
    FormElementModel imageField = null;
    try {
      form = FormFactory.retrieveFormByFormId(formId, cc);

      if (!form.hasValidFormDefinition()) {
        logger.error("Unable to retrieve formId: " + formId + " invalid form definition");
        errorRetreivingData(resp);
        return; // ill-formed definition
      }

      if (titleFieldName != null) {
        FormElementKey titleKey = new FormElementKey(titleFieldName);
        titleField = FormElementModel.retrieveFormElementModel(form, titleKey);
      }
      if (geopointFieldName != null) {
        FormElementKey geopointKey = new FormElementKey(geopointFieldName);
        geopointField = FormElementModel.retrieveFormElementModel(form, geopointKey);
      }
      if (imageFieldName != null) {
        if (!imageFieldName.equals(KmlGenerator.NONE)) {
          FormElementKey imageKey = new FormElementKey(imageFieldName);
          imageField = FormElementModel.retrieveFormElementModel(form, imageKey);
        }
      }
    } catch (ODKFormNotFoundException e) {
      logger.error("Unable to retrieve formId: " + formId + " exception: " + e.toString());
      e.printStackTrace();
      odkIdNotFoundError(resp);
      return;
    } catch (ODKOverQuotaException e) {
      logger.error("Unable to retrieve formId: " + formId + " exception: " + e.toString());
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    } catch (ODKDatastoreException e) {
      logger.error("Unable to retrieve formId: " + formId + " exception: " + e.toString());
      e.printStackTrace();
      datastoreError(resp);
      return;
    } catch (Exception e) {
      logger.error("Unable to prepare formId: " + formId + " exception: " + e.toString());
      errorRetreivingData(resp);
      return;
    }

    KmlWorkerImpl worker = new KmlWorkerImpl(form, persistentResultsKey, attemptCount, titleField,
        geopointField, imageField, cc);
    worker.generateKml();
  }
}