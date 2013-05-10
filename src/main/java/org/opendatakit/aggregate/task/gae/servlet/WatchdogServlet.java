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
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.task.WatchdogWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class WatchdogServlet extends ServletUtilBase{
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 4295412985320942609L;

  private static final Log logger = LogFactory.getLog(WatchdogServlet.class);

  /**
   * URI from base
   */
  public static final String ADDR = "gae/watchdog";

  /**
   * Handler for HTTP Get request to run watchdog task
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);
	cc.setAsDaemon(true);

    logger.info("Beginning servlet processing");
    WatchdogWorkerImpl worker = new WatchdogWorkerImpl();
    try {
      worker.checkTasks(cc);
      logger.info("ending successful servlet processing");
      resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      logger.error(e.toString());
      resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, e.toString());
      return;
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      logger.error(e.toString());
      odkIdNotFoundError(resp);
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      logger.error(e.toString());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    } catch (ODKIncompleteSubmissionData e) {
      e.printStackTrace();
      logger.error(e.toString());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    }
  }
}
