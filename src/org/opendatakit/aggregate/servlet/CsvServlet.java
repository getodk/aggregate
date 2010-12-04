/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.table.CsvFormatter;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Servlet to generate a CSV file for download
 * 
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
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

    UserService userService = (UserService) ContextFactory.get().getBean(BeanDefs.USER_BEAN);
    User user = userService.getCurrentUser();

    // get parameter
    String formId = getParameter(req, ServletConsts.FORM_ID);

    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
    Form form = null;
    try {
      form = Form.retrieveForm(formId, ds, user);
    } catch (ODKFormNotFoundException e1) {
      odkIdNotFoundError(resp);
      return;
    }

    try {
      resp.setContentType(HtmlConsts.RESP_TYPE_ENRICHED);
      setDownloadFileName(resp, formId + ServletConsts.CSV_FILENAME_APPEND);

      // create CSV
      QueryByDate query = new QueryByDate(form, BasicConsts.EPOCH, false,
          ServletConsts.FETCH_LIMIT, ds, user);
      SubmissionFormatter formatter = new CsvFormatter(form, getServerURL(req), resp.getWriter(),
          null);
      formatter.processSubmissions(query.getResultSubmissions());

    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    } catch (ODKDatastoreException e) {
      errorRetreivingData(resp);
    } catch (ODKIncompleteSubmissionData e) {
      errorRetreivingData(resp);
    }
  }

}
