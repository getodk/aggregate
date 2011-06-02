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
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceType;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.web.CallingContext;

/**
 * Servlet to setup connection to an external repository
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ExternalServiceServlet extends ServletUtilBase {
  private static final String SERVICE = "externalService";

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -8372575911483932336L;

  /**
   * URI from base
   */
  public static final String ADDR = "extern/externalService";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Create Connection to External Service";

  /**
   * Handler for HTTP Get request to create External Service
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);

    // get parameter
    String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    String serviceString = getParameter(req, SERVICE);

    // get form
    Form form;
    try {
      form = Form.retrieveForm(formId, cc);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }

    beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, "Create a Connection for Form: "
        + "<FONT COLOR=0000FF>" + form.getViewableName() + "</FONT>"));

    if (serviceString == null) {
      
      out.write(generateServiceButton(formId, ExternalServiceType.GOOGLE_SPREADSHEET, cc));
      out.write(generateServiceButton(formId, ExternalServiceType.JSON_SERVER, cc));
      out.write(generateServiceButton(formId, ExternalServiceType.GOOGLE_FUSIONTABLES, cc));

    } else {
      ExternalServiceType service = ExternalServiceType.valueOf(serviceString);
      out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, "To: " + "<FONT COLOR=0000FF>"
          + service.getServiceName() + "</FONT>" + " Service"));
      out.write(generateExternalServiceEntry(formId, service, cc));
    }

    finishBasicHtmlResponse(resp);
  }

  private String generateExternalServiceEntry(String formId, ExternalServiceType service, CallingContext cc)
      throws UnsupportedEncodingException {
    StringBuilder form = new StringBuilder();
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(service.getAddr()), HtmlConsts.RESP_TYPE_HTML,
        HtmlConsts.GET));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.FORM_ID,
        encodeParameter(formId)));

    if (!service.equals(ExternalServiceType.GOOGLE_FUSIONTABLES)) {
      form.append(service.getDescriptionOfParam() + HtmlConsts.LINE_BREAK);
      form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT,
          ExternalServiceConsts.EXT_SERV_ADDRESS, null));
    }

    form.append(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    form.append(generateRadioOption(ExternalServicePublicationOption.UPLOAD_ONLY, true));
    form.append(generateRadioOption(ExternalServicePublicationOption.STREAM_ONLY, false));
    form.append(generateRadioOption(ExternalServicePublicationOption.UPLOAD_N_STREAM, false));
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
        ExternalServiceConsts.CREATE_EXTERNAL_SERVICE_BUTTON_LABEL));
    form.append(HtmlConsts.FORM_CLOSE);
    return form.toString();
  }

  private String generateRadioOption(ExternalServicePublicationOption option, boolean checked) {
    return HtmlUtil.createRadio(ServletConsts.EXTERNAL_SERVICE_TYPE, option.toString(), option
        .getDescriptionOfOption(), checked);
  }

  private String generateServiceButton(String formId, ExternalServiceType service, CallingContext cc)
      throws UnsupportedEncodingException {

    StringBuilder form = new StringBuilder();
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(ADDR), null, HtmlConsts.GET));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.FORM_ID,
        encodeParameter(formId)));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, SERVICE, encodeParameter(service
        .toString())));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, service.getServiceName()));
    form.append(HtmlConsts.FORM_CLOSE);

    return form.toString();
  }

}
