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
package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.HtmlLinkElementFormatter;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ResultServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -2603483889357461903L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/result";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Result Data Reports";
  
  /**
   * Handler for HTTP Get request to create blank page that is navigable
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);
	 
	// get parameter
	String formId = PersistentResults.FORM_ID_PERSISTENT_RESULT;
	
	try {
	  Form form = Form.retrieveForm(formId, cc);

      // header info
      beginBasicHtmlResponse(TITLE_INFO, resp, true, cc);
      PrintWriter out = resp.getWriter();

      QueryByDate query = new QueryByDate(form, new Date(), true,
               ServletConsts.FETCH_LIMIT, cc);
      query.addFilter(PersistentResults.getRequestingUserKey(), FilterOperation.EQUAL, cc.getCurrentUser().getUriUser());
      List<Submission> submissions = query.getResultSubmissions(cc);

      List<FormElementModel> columns = new ArrayList<FormElementModel>();
      columns.add(PersistentResults.getResultTypeKey());
      columns.add(PersistentResults.getRequestDateKey());
      columns.add(PersistentResults.getStatusKey());
      columns.add(PersistentResults.getLastRetryDateKey());
      columns.add(PersistentResults.getCompletionDateKey());
      columns.add(PersistentResults.getResultFileKey());

      List<Row> formattedElements = new ArrayList<Row>();
      List<String> headers = new ArrayList<String>();
      headers.add("File Type");
      headers.add("Time Requested");
      headers.add("Status");
      headers.add("Time of last retry");
      headers.add("Time Completed");
      headers.add("Result File");
      ElementFormatter elemFormatter = new HtmlLinkElementFormatter(cc.getServerURL(), true, true, true, true);
      
      // format row elements 
      for (SubmissionSet sub : submissions) {
        Row row = sub.getFormattedValuesAsRow(columns, elemFormatter, false, cc);
        formattedElements.add(row);
      }
      
      // format into html table
      out.append(HtmlUtil.wrapResultTableWithHtmlTags(false, null, headers, formattedElements));
      out.print(HtmlConsts.LINE_BREAK);
      out.print(HtmlConsts.LINE_BREAK);
      
      out.print(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(ADDR), null, HtmlConsts.GET));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, "Refresh"));
      out.print(HtmlConsts.FORM_CLOSE);

      // footer info
      finishBasicHtmlResponse(resp);

    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    } catch (ODKDatastoreException e) {
      errorRetreivingData(resp);
    }

  }
}
