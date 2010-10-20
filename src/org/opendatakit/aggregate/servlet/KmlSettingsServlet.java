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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

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

    UserService userService = (UserService) ContextFactory.get().getBean(
        ServletConsts.USER_BEAN);
    User user = userService.getCurrentUser();
   
    // get parameter
    String odkId = getParameter(req, ServletConsts.ODK_ID);
    if (odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    // get form
    Datastore ds = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);
    FormDefinition fd = FormDefinition.getFormDefinition(odkId, ds, user);
    if ( fd == null ) {
    	odkIdNotFoundError(resp);
    	return;
    }
    
    geopointNodes = new ArrayList<FormDataModel>();
    imageNodes = new ArrayList<FormDataModel>();
    allNodes = new ArrayList<FormDataModel>();
    
    processElementForColumnHead(fd.getTopLevelGroup(), fd.getTopLevelGroup(), "");
    
    beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
    
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.createFormBeginTag(KmlServlet.ADDR, null, HtmlConsts.GET));
    
    out.write("Field to map:" + HtmlConsts.LINE_BREAK);
    List<String> geopointOptions = createSelectOptionsFromFormElements(geopointNodes);
    out.write(HtmlUtil.createSelect("geopointField", geopointOptions));
    out.write("<p/>");
    
    out.write("Title field:" + HtmlConsts.LINE_BREAK);
    List<String> allOptions = createSelectOptionsFromFormElements(allNodes);
    out.write(HtmlUtil.createSelect("titleField", allOptions));
    out.write("<p/>");
    
    out.write("Picture field to display:" + HtmlConsts.LINE_BREAK);
    List<String> imageOptions = createSelectOptionsFromFormElements(imageNodes);
    imageOptions.add("None");
    out.write(HtmlUtil.createSelect("imageField", imageOptions));
    
    out.write("<p/>");
    out.write(HtmlUtil.createInput("hidden", ServletConsts.ODK_ID, fd.getFormId()));
    out.write(HtmlUtil.createInput("submit", null, null));
    out.write("</form>");
    
    finishBasicHtmlResponse(resp);
  }
  
  private List<FormDataModel> geopointNodes;
  private List<FormDataModel> imageNodes;
  private List<FormDataModel> allNodes;
  /**
   * Helper function to recursively go through the element tree and create
   * the column headings
   * 
   */
  private void processElementForColumnHead(FormDataModel node,
		  FormDataModel root, String parentName) {
    if (node == null) return;

    switch ( node.getElementType() ) {
    case GEOPOINT:
    	geopointNodes.add(node);
    	break;
    case BINARY:
    	imageNodes.add(node);
    	break;
    }
    
    switch ( node.getElementType() ) {
    case GROUP:
    case REPEAT:
    case PHANTOM:
    	break;
	default:
    	allNodes.add(node);	
    }

    List<FormDataModel> childDataElements = node.getChildren();
    if (childDataElements == null) {
      return;
    }
    for (FormDataModel child : childDataElements) {
      processElementForColumnHead(child, root, parentName);
    }
  }
  
  private List<String> createSelectOptionsFromFormElements(List<FormDataModel> l) {
    List<String> options = new ArrayList<String>();
    for (FormDataModel fe: l){
      options.add(fe.getElementName());
    }
    return options;
  }
}

