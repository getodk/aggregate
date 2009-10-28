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
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormAlreadyExistsException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.parser.FormParserForJavaRosa;
import org.odk.aggregate.parser.MultiPartFormData;
import org.odk.aggregate.parser.MultiPartFormItem;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Servlet to upload, parse, and save an XForm
 *
 * @author wbrunette@gmail.com
 *
 */
public class FormUploadServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -3784460108221008112L;

  /**
   * URI from base
   */
  public static final String ADDR = "upload";
  
  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Xform Upload";
  
  /**
   * Handler for HTTP Get request to create xform upload page
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
   
    beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
    
    
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.createFormBeginTag(ADDR, ServletConsts.MULTIPART_FORM_DATA, ServletConsts.POST));
    out.write("Name of Xform:" + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT, ServletConsts.FORM_NAME_PRAM, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write("Location of Xform definition to be uploaded:" + HtmlConsts.LINE_BREAK); 
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, ServletConsts.FORM_DEF_PRAM, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, "Upload"));
    out.write(HtmlConsts.FORM_CLOSE);
    finishBasicHtmlResponse(resp);
  }
  
  /**
   * Handler for HTTP Post request that takes an xform, parses, and saves a 
   * parsed version in the datastore 
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    
    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }
    
    // verify request is multipart
    if(!ServletFileUpload.isMultipartContent(req)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_MULTI_PART_CONTENT);
      return;
    }
    
    try {
      // process form
      MultiPartFormData uploadedFormItems = new MultiPartFormData(req);

      MultiPartFormItem formNameData = uploadedFormItems.getFormDataByFieldName(ServletConsts.FORM_NAME_PRAM);
      MultiPartFormItem formXmlData = uploadedFormItems.getFormDataByFieldName(ServletConsts.FORM_DEF_PRAM);
       
      FormParserForJavaRosa parser = null;
      String formName = null;
      String formXml = null;
      String xmlFileName = "default.xml";

      if(formNameData != null) {
        formName = formNameData.getStream().toString("UTF-8");
      }
      if(formXmlData != null) {
        // TODO: changed added output stream writer. probably something better exists
        formXml =  formXmlData.getStream().toString("UTF-8");
        xmlFileName = formXmlData.getFilename();
      }
      
      // persist form
      EntityManager em = EMFactory.get().createEntityManager();
      
      if(formName != null && formXml != null) {
        try {
          parser = new FormParserForJavaRosa(formName, user.getNickname(), formXml, xmlFileName, em);
        } catch (ODKFormAlreadyExistsException e) {
          resp.sendError(HttpServletResponse.SC_CONFLICT, ErrorConsts.FORM_WITH_ODKID_EXISTS);
          return;
        }
      } else {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
        return;
      } 
  
     
      // TODO: do better error handling
      try {
        Form form = parser.getForm();
        em.persist(form);
        form.printDataTree(System.out);
      } catch (Exception e) {
        e.printStackTrace();
      }
      em.close();
      resp.sendRedirect(FormsServlet.ADDR);

    } catch (FileUploadException e) {
      e.printStackTrace(resp.getWriter());
    }

  }
}
