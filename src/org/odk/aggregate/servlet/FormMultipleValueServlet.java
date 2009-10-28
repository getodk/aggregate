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
import org.odk.aggregate.table.SubmissionHtmlTable;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Servlet generates a webpage with a list of submissions 
 * from a repeat node of a form
 *
 * @author wbrunette@gmail.com
 *
 */
public class FormMultipleValueServlet extends ServletUtilBase {
  /**
   *  Serial number for serialization
   */
  private static final long serialVersionUID = -5870882843863177371L;

  /**
   * URI from base
   */
  public static final String ADDR = "formMultipleValue";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Submissions Results: ";

  /**
   * Handler for HTTP Get request that responds with list of values from a repeat
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
    String kind = getParameter(req, ServletConsts.KIND);
    String elementKeyString = getParameter(req, ServletConsts.FORM_ELEMENT_KEY);
    String submissionParentKeyString = getParameter(req, ServletConsts.PARENT_KEY);
    
    if(odkId == null || kind == null || elementKeyString == null || submissionParentKeyString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }
    
    Key elementKey = KeyFactory.stringToKey(elementKeyString);
    Key submissionParentKey = KeyFactory.stringToKey(submissionParentKeyString);

    // header info
    beginBasicHtmlResponse(TITLE_INFO + kind, resp, req, true);
    
    EntityManager em = EMFactory.get().createEntityManager();
    
    try {
      String html = new SubmissionHtmlTable(getServerURL(req), odkId, em).generateHtmlSubmissionRepeatResultsTable(kind, elementKey, submissionParentKey);
      resp.getWriter().print(html);
      
      // footer info
      finishBasicHtmlResponse(resp);
      
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    } catch (ODKIncompleteSubmissionData e) {
      errorRetreivingData(resp);
    } finally {
      em.close();
    }

  }
}
