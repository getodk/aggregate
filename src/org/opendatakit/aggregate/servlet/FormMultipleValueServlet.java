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
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.format.table.HtmlFormatter;
import org.opendatakit.aggregate.query.submission.QueryRepeats;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Servlet generates a webpage with a list of submissions from a repeat node of
 * a form
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormMultipleValueServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
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
   * Handler for HTTP Get request that responds with list of values from a
   * repeat
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    UserService userService = (UserService) ContextFactory.get().getBean(
        ServletConsts.USER_BEAN);
    User user = userService.getCurrentUser();

    // get parameter
    String odkId = getParameter(req, ServletConsts.ODK_ID);
    String kind = getParameter(req, ServletConsts.KIND);
    String elementKey = getParameter(req, ServletConsts.FORM_ELEMENT_KEY);
    String submissionParentKey = getParameter(req, ServletConsts.PARENT_KEY);

    if (odkId == null || kind == null || elementKey == null
        || submissionParentKey == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

 
    Datastore ds = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);
    FormDefinition fd = FormDefinition.getFormDefinition(odkId, ds, user);
    String submissionKey = odkId + "/" + kind;

    try {
      QueryRepeats query = new QueryRepeats(fd, submissionKey, submissionParentKey, ds, user);
      
      FormDefinition form = FormDefinition.getFormDefinition(odkId, ds, user);
      
      // TODO: need to pull the top-level form instance then traverse to the SubmissionSet matching the FORM_ELEMENT_KEY
      // and that may be a specific SubmissionSet or a repeat element.
      FormDataModel element = form.getElementByName(elementKey);     
      HtmlFormatter formatter = new HtmlFormatter(query.getFormDefinition(), getServerURL(req), resp.getWriter(), null, true);
 
      // header info
      beginBasicHtmlResponse(TITLE_INFO + kind, resp, req, true);

      formatter.processSubmissionSetPublic(query.getRepeatSubmissionSet(), element);
     
      // footer info
      finishBasicHtmlResponse(resp);

    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    } catch (ODKIncompleteSubmissionData e) {
      errorRetreivingData(resp);
    } catch (ODKEntityNotFoundException e) {
      errorRetreivingData(resp);
    } catch (ODKDatastoreException e) {
      errorRetreivingData(resp);
    }

  }
}
