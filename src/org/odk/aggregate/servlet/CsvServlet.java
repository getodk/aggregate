/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.servlet;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.table.SubmissionCsvTable;

/**
 * Servlet to generate a CSV file for download
 * 
 *
 * @author wbrunette@gmail.com
 *
 */
public class CsvServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 1533921429476018375L;

  /**
   * URI from base
   */
  public static final String ADDR = "csv";

  /**
   * Handler for HTTP Get request that responds with CSV
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }
   
    // get parameter
    String odkId = getParameter(req, ServletConsts.ODK_ID);
   
    if(odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    EntityManager em = EMFactory.get().createEntityManager();

    try {
      resp.setContentType(ServletConsts.RESP_TYPE_ENRICHED);
      setDownloadFileName(resp, odkId + ServletConsts.CSV_FILENAME_APPEND);

      // create CSV
      SubmissionCsvTable submissions = new SubmissionCsvTable(req.getServerName(),odkId, em);
      resp.getWriter().print(submissions.generateCsv());
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    }catch (ODKIncompleteSubmissionData e) {
      errorRetreivingData(resp);
    } finally {
      em.close();
    }

  }


}
