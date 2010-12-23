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

import org.apache.commons.lang.StringEscapeUtils;
import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.constants.HtmlConsts;

/**
 * Servlet to generate the XML list of forms to be presented as the API for
 * forms for computers
 * 
 * @author alerer@gmail.com
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
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
  public static final String ADDR = "view/kmlSettings";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "KML Settings";

  private static final String PIC_TXT = "Picture field to display:";

  private static final String TITLE_TXT = "Title field:";

  private static final String GEOPOINT_TXT = "Field to map:";

  public static final String NONE = "None";

  private List<FormElementKey> geopointNodesNames;
  private List<FormElementKey> binaryNodeNames;
  private List<FormElementKey> allNodesNames;

  /**
   * Handler for HTTP Get request that responds with an XML list of forms to
   * download
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

    // get parameter
    String odkId = getParameter(req, ServletConsts.FORM_ID);
    if (odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    // get form
    Form form = null;
    try {
      form = Form.retrieveForm(odkId, cc);
      geopointNodesNames = new ArrayList<FormElementKey>();
      binaryNodeNames = new ArrayList<FormElementKey>();
      allNodesNames = new ArrayList<FormElementKey>();

      FormElementModel root = form.getTopLevelGroupElement();
      processElementForColumnHead(form, root, root);

    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }

    beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info
    String spaceBetweenInputs = HtmlUtil.createSelfClosingTag(HtmlConsts.P);

    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(KmlServlet.ADDR), null, HtmlConsts.GET));

    out.write(GEOPOINT_TXT + HtmlConsts.LINE_BREAK);
    out.write(createSelect(KmlServlet.GEOPOINT_FIELD, geopointNodesNames, false, form));
    out.write(spaceBetweenInputs);

    out.write(TITLE_TXT + HtmlConsts.LINE_BREAK);
    out.write(createSelect(KmlServlet.TITLE_FIELD, allNodesNames, false, form));
    out.write(spaceBetweenInputs);

    out.write(PIC_TXT + HtmlConsts.LINE_BREAK);
    out.write(createSelect(KmlServlet.IMAGE_FIELD, binaryNodeNames, true, form));
    out.write(spaceBetweenInputs);

    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.FORM_ID, form
        .getFormId()));
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, null));
    out.write(HtmlConsts.FORM_CLOSE);

    finishBasicHtmlResponse(resp);
  }

  /**
   * Helper function to recursively go through the element tree and create the
   * FormElementKeys
   * 
   */
  private void processElementForColumnHead(Form form, FormElementModel node, FormElementModel root) {
    if (node == null)
      return;

    FormElementKey key = node.constructFormElementKey(form);
    switch (node.getElementType()) {
    case GEOPOINT:
      geopointNodesNames.add(key);
      break;
    case BINARY:
      binaryNodeNames.add(key);
      break;
    case REPEAT:
    case GROUP:
      break; // should not be in any list
    default:
      allNodesNames.add(key);
    }

    List<FormElementModel> childDataElements = node.getChildren();
    if (childDataElements == null) {
      return;
    }
    for (FormElementModel child : childDataElements) {
      processElementForColumnHead(form, child, root);
    }
  }

  public final String createSelect(String name, List<FormElementKey> values, boolean addNone, Form form) {
    if (name == null) {
      return null;
    }
    StringBuilder html = new StringBuilder();
    html.append("<select name='" + StringEscapeUtils.escapeHtml(name) + "'>");

    if(addNone) {
      html.append("<option value='" + NONE + "'>" + NONE + "</option>");
    }
    
    if (values != null) {
      for (FormElementKey key : values) {
        html.append("<option value='" + StringEscapeUtils.escapeHtml(key.toString()) + "'>");
        html.append(StringEscapeUtils.escapeHtml(key.userFriendlyString(form)));
        html.append("</option>");
      }
    }
    html.append("</select>");
    return html.toString();
  }

}
