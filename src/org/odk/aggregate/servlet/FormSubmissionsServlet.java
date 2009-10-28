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
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.table.SubmissionHtmlTable;

/**
 * Servlet generates a webpage with a list of submissions from a specified form
 *
 * @author wbrunette@gmail.com
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
  public static final String ADDR = "formSubmissions";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Form Submissions Results";

  /**
   * Handler for HTTP Get request that responds list of submissions from a
   * specified form
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // get parameter
    String odkId = getParameter(req, ServletConsts.ODK_ID);
    String backwardString = getParameter(req, ServletConsts.BACKWARD);
    String indexString = getParameter(req, ServletConsts.INDEX);
    
    if(odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    // header info
    beginBasicHtmlResponse(TITLE_INFO, resp, req, true);
    
    EntityManager em = EMFactory.get().createEntityManager();
    
    try {
      Boolean backward = false;
      if(backwardString != null) {
        backward = Boolean.parseBoolean(backwardString);
      }
      
      Date indexDate = TableConsts.EPOCH; 
      if(indexString != null) {
        try {
          indexDate = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).parse(indexString);
          if(!backward) {
            indexDate = new Date(indexDate.getTime()+1000); // TODO: worse hack EVER!
          }
        } catch (ParseException e) {
          // ignore exception as if we can't parse the string then keep the default
        }
      }
            
      SubmissionHtmlTable submissions = new SubmissionHtmlTable(getServerURL(req), odkId, em);
      submissions.generateHtmlSubmissionResultsTable(indexDate, backward);

      boolean createBack = false;
      boolean createForward = false;
      
      if(indexString != null) {
        if(submissions.isMoreRecords()) {
          // create both directions
          createBack = true;
          createForward = true;
        } else {
          // create only the direction opposite of the previous move
          if(backward) {
            createForward = true;
          } else {
            createBack = true;
          }
        }
      } else {
        if(submissions.isMoreRecords()) {
          // create forward
          createForward = true;
        }
      }
      
      if(createBack) {
        Map<String, String> properties = new HashMap<String,String>();
        properties.put(ServletConsts.ODK_ID, odkId);
        properties.put(ServletConsts.BACKWARD,  Boolean.TRUE.toString());
        properties.put(ServletConsts.INDEX, submissions.getFirstDate());
        String link = HtmlUtil.createHrefWithProperties(req.getRequestURI(), properties, ServletConsts.BACK_LINK_TEXT);
        resp.getWriter().print(link);
      }
      resp.getWriter().print(HtmlConsts.TAB + HtmlConsts.TAB);
      if(createForward) {
        Map<String, String> properties = new HashMap<String,String>();
        properties.put(ServletConsts.ODK_ID, odkId);
        properties.put(ServletConsts.BACKWARD, Boolean.FALSE.toString());
        properties.put(ServletConsts.INDEX, submissions.getLastDate());
        String link = HtmlUtil.createHrefWithProperties(req.getRequestURI(), properties, ServletConsts.NEXT_LINK_TEXT);
        resp.getWriter().print(link);
      }

      resp.getWriter().print(submissions.getResultsHtml());

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
