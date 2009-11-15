/*
 * Copyright (C) 2009 University of Washington
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
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.parser.MultiPartFormData;
import org.odk.aggregate.process.ProcessParams;
import org.odk.aggregate.table.SubmissionHtmlTable;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

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
    EntityManager em = EMFactory.get().createEntityManager();
 
    try {
      ProcessParams params = new ProcessParams(new MultiPartFormData(req));
      PrintWriter out = resp.getWriter();
      List<Key> keys = params.getKeys();
    
      if (params.getOdkId() == null || keys == null || params.getButtonText() == null) {
        sendErrorNotEnoughParams(resp);
        return;
      }
      
      beginBasicHtmlResponse(TITLE_INFO, resp, req, false); // header info
      SubmissionHtmlTable submissions = new SubmissionHtmlTable(getServerURL(req), params.getOdkId(), em);
      submissions.generateHtmlSubmissionResultsTableFromKeys(keys);
     
      out.print(HtmlUtil.createFormBeginTag(ProcessServlet.ADDR, ServletConsts.MULTIPART_FORM_DATA, ServletConsts.POST));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.ODK_ID, params.getOdkId()));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.PROCESS_NUM_RECORDS, Integer.toString(keys.size())));
      for(int i=0; i < keys.size(); i++) {
        out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,ServletConsts.PROCESS_RECORD_PREFIX + i, KeyFactory.keyToString(keys.get(i))));
      }
      out.print(submissions.getResultsHtml(false));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, ServletConsts.PROCESS_TYPE, params.getButtonText()));
      finishBasicHtmlResponse(resp);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
    } catch (ODKIncompleteSubmissionData e) {
      e.printStackTrace();
    } catch (FileUploadException e) {
      e.printStackTrace();
    }
    
    em.close();
  }
}
