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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.submission.SubmissionFieldType;

import com.google.appengine.repackaged.com.google.common.base.Pair;

/**
 * Servlet to generate the XML list of forms to be
 * presented as the API for forms for computers
 *
 * @author alerer@gmail.com
 * @author wbrunette@gmail.com
 *
 */
public class KmlSettingsServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -7920604746551634550L;

  /**
   * URI from base
   */
  public static final String ADDR = "kmlSettings";
  
  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "KML Settings";

  private List<String> geopointNodeNames;
  private List<String> imageNodesNames;
  private List<String> allNodesNames;
  
  
  /**
   * Handler for HTTP Get request that responds with an XML list of forms to download
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
    String odkIdParam = getParameter(req, ServletConsts.ODK_ID);
    if (odkIdParam == null) {
      errorMissingKeyParam(resp);
      return;
    }

    // get form
    EntityManager em = EMFactory.get().createEntityManager();
    Form form;
    try {
      form = Form.retrieveForm(em, odkIdParam);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }
    
    geopointNodeNames = new ArrayList<String>();
    imageNodesNames = new ArrayList<String>();
    allNodesNames = new ArrayList<String>();
    
    processElementForColumnHead(form.getElementTreeRoot(), form.getElementTreeRoot(), BasicConsts.EMPTY_STRING);
    
    beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
    
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.createFormBeginTag(KmlServlet.ADDR, null, ServletConsts.GET));
    
    out.write("Field to map:" + HtmlConsts.LINE_BREAK);
    List<Pair<String,String>> geopointOptions = createSelectOptionsFromFormElements(geopointNodeNames);
    out.write(HtmlUtil.createSelect(KmlServlet.GEOPOINT_FIELD, geopointOptions));
    out.write("<p/>");
    
    out.write("Title field:" + HtmlConsts.LINE_BREAK);
    List<Pair<String,String>> allOptions = createSelectOptionsFromFormElements(allNodesNames);
    out.write(HtmlUtil.createSelect(KmlServlet.TITLE_FIELD, allOptions));
    out.write("<p/>");
    
    out.write("Picture field to display:" + HtmlConsts.LINE_BREAK);
    List<Pair<String,String>> imageOptions = createSelectOptionsFromFormElements(imageNodesNames);
    imageOptions.add(new Pair<String,String>("", "None"));
    out.write(HtmlUtil.createSelect(KmlServlet.IMAGE_FIELD, imageOptions));
    
    out.write("<p/>");
    out.write(HtmlUtil.createInput("hidden", ServletConsts.ODK_ID, form.getOdkId()));
    out.write(HtmlUtil.createInput("submit", null, null));
    out.write("</form>");
    
    finishBasicHtmlResponse(resp);
  }
  

  /**
   * Helper function to recursively go through the element tree and create
   * the column headings
   * 
   */
  private void processElementForColumnHead(FormElement node, FormElement root, String parentName) {
    if (node == null) return;

    if (!node.getSubmissionFieldType().equals(SubmissionFieldType.UNKNOWN)) {
      String nodeName = parentName + node.getElementName();
      if (node.getSubmissionFieldType().equals(SubmissionFieldType.GEOPOINT)) {
        geopointNodeNames.add(nodeName);
      } else if (node.getSubmissionFieldType().equals(
          SubmissionFieldType.PICTURE)) {
        imageNodesNames.add(nodeName);
      }
      allNodesNames.add(nodeName);
    } else if(node.isRepeatable()) {
      parentName = parentName + node.getElementName() + BasicConsts.COLON;
    } else {
      if(node != root) { 
        parentName = parentName + node.getElementName() + BasicConsts.DASH;
      }
    }
    
    List<FormElement> childDataElements = node.getChildren();
    if (childDataElements == null) {
      return;
    }
    for (FormElement child : childDataElements) {
      processElementForColumnHead(child, root, parentName);
    }
  }
  
  private List<Pair<String,String>> createSelectOptionsFromFormElements(List<String> l) {
    List<Pair<String,String>> options = new ArrayList<Pair<String,String>>();
    for (String fe: l){
      options.add(new Pair<String,String>(fe, fe));
    }
    return options;
  }



}

