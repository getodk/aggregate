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

import org.apache.commons.fileupload.FileUploadException;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.form.FormHtmlTable;
import org.opendatakit.aggregate.format.table.HtmlFormatter;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.process.ProcessParams;
import org.opendatakit.aggregate.process.ProcessType;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.query.submission.QueryByKeys;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

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
  public static final String ADDR = "confirm";
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
    Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
    UserService userService = (UserService) ContextFactory.get().getBean(BeanDefs.USER_BEAN);
    User user = userService.getCurrentUser();
    
    try {
      ProcessParams params = new ProcessParams(new MultiPartFormData(req));
      PrintWriter out = resp.getWriter();
      List<String> paramKeys = params.getKeys();

      if (paramKeys == null || params.getButtonText() == null) {
        sendErrorNotEnoughParams(resp);
        return;
      }

      List<SubmissionKey> keys = new ArrayList<SubmissionKey>();
      for (String paramKey : paramKeys) {
    	  keys.add(new SubmissionKey(paramKey));
      }
      beginBasicHtmlResponse(TITLE_INFO, resp, req, false); // header info
      out.print(HtmlUtil.createFormBeginTag(ProcessServlet.ADDR,
              HtmlConsts.MULTIPART_FORM_DATA, HtmlConsts.POST));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
              ServletConsts.PROCESS_NUM_RECORDS, Integer.toString(keys.size())));
            
      for (int i = 0; i < keys.size(); i++) {
        out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
            ServletConsts.PROCESS_RECORD_PREFIX + i, keys.get(i).toString()));
      }

      if (params.getButtonText().equals(ProcessType.DELETE.getButtonText())) {
    	  if(params.getFormId() == null) {
	        sendErrorNotEnoughParams(resp);
	        return;
	      }
      
    	  Form form = Form.retrieveForm(params.getFormId(), ds, user);

	      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
	          ServletConsts.FORM_ID, form.getFormId()));
      
		  QueryByKeys query = new QueryByKeys(keys, ds, user);
		  SubmissionFormatter formatter = new HtmlFormatter(form, getServerURL(req), resp.getWriter(), null, true);
		  formatter.processSubmissions(query.getResultSubmissions());
      } else if (params.getButtonText().equals(ProcessType.DELETE_FORM.getButtonText())) {
		  QueryFormList formsList = new QueryFormList(keys, true, ds, user);
		  FormHtmlTable formFormatter = new FormHtmlTable(formsList);
		  out.print(formFormatter.generateHtmlFormTable(false, false));
      }
      
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, ServletConsts.PROCESS_TYPE, params.getButtonText()));
      finishBasicHtmlResponse(resp);

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
    } catch (ODKIncompleteSubmissionData e) {
      e.printStackTrace();
    } catch (FileUploadException e) {
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
  }
}
