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
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.task.UploadSubmissionsWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.backends.BackendServiceFactory;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class UploadSubmissionsTaskServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 4295412985320942608L;

  private static final Log logger = LogFactory.getLog(UploadSubmissionsTaskServlet.class);

  /**
   * URI from base
   */
  public static final String ADDR = "gae/uploadSubmissionsTask";

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
    String fscUri = getParameter(req, ExternalServiceConsts.FSC_URI_PARAM);
    if (fscUri == null) {
      logger.error("Missing " + ExternalServiceConsts.FSC_URI_PARAM + " key");
      errorMissingParam(resp);
      return;
    }

    logger.info("Beginning servlet processing");
    FormServiceCursor fsc;
    try {
      fsc = FormServiceCursor.getFormServiceCursor(fscUri, cc);
    } catch (ODKEntityNotFoundException e) {
      // TODO: fix bug we should not be generating tasks for fsc that don't
      // exist
      // however not critical bug as execution path dies with this try/catch
      logger.error("BUG: we generated an task for a form service cursor that didn't exist"
          + e.toString());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    } catch (ODKOverQuotaException e) {
      logger.error("Over quota." + e.toString());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      logger.error("Datastore failure." + e.toString());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    }

    // determine whether we are running on a backend or not...
    BackendService bes = BackendServiceFactory.getBackendService();
    boolean isBackend = (bes.getCurrentBackend() != null);
    logger.info("Request is running on " + (isBackend ? "backend" : "frontend"));

    try {
      UploadSubmissionsWorkerImpl worker = new UploadSubmissionsWorkerImpl(fsc, isBackend, cc);
      worker.uploadAllSubmissions();
      logger.info("ending successful servlet processing");
      resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      logger.error(e.toString());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    } catch (ODKExternalServiceException e) {
      logger.error(e.toString());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    } catch (ODKFormNotFoundException e) {
      logger.error(e.toString());
      odkIdNotFoundError(resp);
      return;
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.toString());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    }
  }
}
