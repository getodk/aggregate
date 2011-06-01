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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.form.FormHtmlTable;
import org.opendatakit.aggregate.format.table.HtmlFormatter;
import org.opendatakit.aggregate.process.ProcessType;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.query.submission.QueryByKeys;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ConfirmServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 7812984489139368477L;
  /**
   * URI from base
   */
  public static final String ADDR = "admin/confirm";
  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Confirm Action";

  /**
   * Handler for HTTP Post request
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);

	// get parameter
	String[] recordKeyArray = req.getParameterValues(ServletConsts.RECORD_KEY);
	List<SubmissionKey> recordKeys = new ArrayList<SubmissionKey>();
	if ( recordKeyArray != null ) {
		for ( String formId : recordKeyArray ) {
			recordKeys.add(new SubmissionKey(formId));
		}
	}
	if (recordKeys.isEmpty()) {
		errorMissingParam(resp);
		return;
	}
	
	String processType = req.getParameter(ServletConsts.PROCESS_TYPE);
	if (processType == null || processType.length() == 0) {
		errorMissingParam(resp);
		return;
	}

    try {
      beginBasicHtmlResponse(TITLE_INFO, resp, false, cc); // header info
      PrintWriter out = resp.getWriter();
      out.print(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(ProcessServlet.ADDR),
              null, HtmlConsts.POST));
      
      // copy post parameters to form as hidden values
      for ( SubmissionKey desiredFormId : recordKeys ) {
        out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
            ServletConsts.RECORD_KEY, desiredFormId.toString() ));
      }

      if (processType.equals(ProcessType.DELETE.getButtonText())) {

    	  String formId = req.getParameter(ServletConsts.FORM_ID);
		  if (formId == null || formId.length() == 0) {
			errorMissingParam(resp);
			return;
		  }
      
    	  // display the submissions being deleted...
    	  // This is a read-only display for confirmation only.  The
    	  // hidden fields above contain the keys for each of these 
    	  // records.
    	  Form form = Form.retrieveForm(formId, cc);

	      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
	          ServletConsts.FORM_ID, form.getFormId()));
      
		  QueryByKeys query = new QueryByKeys(recordKeys);
		  SubmissionFormatter formatter = new HtmlFormatter(form, cc.getServerURL(), out, null, false);
		  formatter.processSubmissions(query.getResultSubmissions(cc), cc);
		  
      } else if (processType.equals(ProcessType.DELETE_FORM.getButtonText())) {
    	  
    	  // Display the forms being deleted.
    	  // This is a read-only display for confirmation only.  The
    	  // hidden fields above contain the form ids being removed.
		  QueryFormList formsList = new QueryFormList(recordKeys, true, cc);
		  FormHtmlTable formFormatter = new FormHtmlTable(formsList);
		  out.print(formFormatter.generateHtmlFormTable(false, false, cc));
      }
      
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, ServletConsts.PROCESS_TYPE, processType));
      finishBasicHtmlResponse(resp);

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
    } catch (ODKIncompleteSubmissionData e) {
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    } 
  }
}
