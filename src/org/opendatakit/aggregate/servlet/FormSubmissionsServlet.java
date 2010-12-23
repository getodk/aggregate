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
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.table.HtmlFormatter;
import org.opendatakit.aggregate.process.ProcessType;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * Servlet generates a webpage with a list of submissions from a specified form
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormSubmissionsServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 2514025048848108669L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/formSubmissions";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Submissions for ";

  /**
   * Handler for HTTP Get request that responds list of submissions from a
   * specified form
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

    // get parameter
    String formId = getParameter(req, ServletConsts.FORM_ID);
    String backwardString = getParameter(req, ServletConsts.BACKWARD);
    String indexString = getParameter(req, ServletConsts.INDEX);

    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    try {
      Boolean backward = false;
      if (backwardString != null) {
        backward = Boolean.parseBoolean(backwardString);
      }

      Date indexDate = BasicConsts.EPOCH;
      if (indexString != null) {
        try {
          indexDate = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).parse(
              indexString);
          if (!backward) {
            indexDate = new Date(indexDate.getTime() + 1000); // TODO: worse hack EVER!
          }
        } catch (ParseException e) {
          // ignore exception as if we can't parse the string then keep the
          // default
        }
      }
      
      Form form = Form.retrieveForm(formId, cc);

      // header info
      beginBasicHtmlResponse(TITLE_INFO + form.getViewableName(), resp, true, cc);
      PrintWriter out = resp.getWriter();

      QueryByDate query = new QueryByDate(form, BasicConsts.EPOCH, false,
              ServletConsts.FETCH_LIMIT, cc);
      HtmlFormatter formatter = new HtmlFormatter(form, cc.getServerURL(), resp.getWriter(), null, true);
      List<Submission> submissions = query.getResultSubmissions();

      boolean createBack = false;
      boolean createForward = false;

      if (indexString != null) {
        if (query.moreRecordsAvailable()) {
          // create both directions
          createBack = true;
          createForward = true;
        } else {
          // create only the direction opposite of the previous move
          if (backward) {
            createForward = true;
          } else {
            createBack = true;
          }
        }
      } else {
        if (query.moreRecordsAvailable()) {
          // create forward
          createForward = true;
        }
      }

      if (createBack) {
        Date firstDate = submissions.get(0).getSubmittedTime();
        
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ServletConsts.FORM_ID, formId);
        properties.put(ServletConsts.BACKWARD, Boolean.TRUE.toString());
        properties.put(ServletConsts.INDEX, firstDate.toString());
    
        String link = HtmlUtil.createHrefWithProperties(req.getRequestURI(), properties,
            ServletConsts.BACK_LINK_TEXT);
        out.print(link);
      }
      resp.getWriter().print(HtmlConsts.TAB + HtmlConsts.TAB);
      if (createForward) {
        Date lastDate = submissions.get(submissions.size()-1).getSubmittedTime();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ServletConsts.FORM_ID, formId);
        properties.put(ServletConsts.BACKWARD, Boolean.FALSE.toString());
        properties.put(ServletConsts.INDEX, lastDate.toString());
        String link = HtmlUtil.createHrefWithProperties(req.getRequestURI(), properties,
            ServletConsts.NEXT_LINK_TEXT);
        out.print(link);
      }

      out.print(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(ConfirmServlet.ADDR), HtmlConsts.MULTIPART_FORM_DATA,
          HtmlConsts.POST));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.FORM_ID, formId));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
          ServletConsts.PROCESS_NUM_RECORDS, Integer.toString(query.getNumRecords())));
      
      formatter.processSubmissions(submissions);
      
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, ServletConsts.PROCESS_TYPE,
          ProcessType.DELETE.getButtonText()));
      out.print(HtmlConsts.LINE_BREAK);
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
