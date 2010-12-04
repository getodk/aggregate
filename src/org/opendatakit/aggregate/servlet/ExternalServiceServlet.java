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
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceType;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

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
  public static final String ADDR = "externalService";

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

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    UserService userService = (UserService) ContextFactory.get().getBean(BeanDefs.USER_BEAN);
    User user = userService.getCurrentUser();

    // get parameter
    String odkId = getParameter(req, ServletConsts.FORM_ID);
    if (odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    String serviceString = getParameter(req, SERVICE);

    // get form
    Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
    Form form;
    try {
      form = Form.retrieveForm(odkId, ds, user);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }

    beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, "Create a Connection for Form: "
        + "<FONT COLOR=0000FF>" + form.getViewableName() + "</FONT>"));

    if (serviceString == null) {
      
      out.write(generateServiceButton(odkId, getServerURL(req), ExternalServiceType.GOOGLE_SPREADSHEET));
      out.write(generateServiceButton(odkId, getServerURL(req), ExternalServiceType.JSON_SERVER));
      out.write(generateServiceButton(odkId, getServerURL(req), ExternalServiceType.GOOGLE_FUSIONTABLES));

    } else {
      ExternalServiceType service = ExternalServiceType.valueOf(serviceString);
      out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, "To: " + "<FONT COLOR=0000FF>"
          + service.getServiceName() + "</FONT>" + " Service"));
      out.write(generateExternalServiceEntry(odkId, service));
    }

    finishBasicHtmlResponse(resp);
  }

  private String generateExternalServiceEntry(String odkId, ExternalServiceType service)
      throws UnsupportedEncodingException {
    StringBuilder form = new StringBuilder();
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createFormBeginTag(service.getAddr(), HtmlConsts.RESP_TYPE_HTML,
        HtmlConsts.GET));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.FORM_ID,
        encodeParameter(odkId)));

    if (!service.equals(ExternalServiceType.GOOGLE_FUSIONTABLES)) {
      form.append(service.getDescriptionOfParam() + HtmlConsts.LINE_BREAK);
      form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT,
          ExternalServiceConsts.EXT_SERV_ADDRESS, null));
    }

    form.append(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    form.append(generateRadioOption(ExternalServiceOption.UPLOAD_ONLY, true));
    form.append(generateRadioOption(ExternalServiceOption.STREAM_ONLY, false));
    form.append(generateRadioOption(ExternalServiceOption.UPLOAD_N_STREAM, false));
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
        ExternalServiceConsts.CREATE_EXTERNAL_SERVICE_BUTTON_LABEL));
    form.append(HtmlConsts.FORM_CLOSE);
    return form.toString();
  }

  private String generateRadioOption(ExternalServiceOption option, boolean checked) {
    return HtmlUtil.createRadio(ServletConsts.EXTERNAL_SERVICE_TYPE, option.toString(), option
        .getDescriptionOfOption(), checked);
  }

  private String generateServiceButton(String odkId, String url, ExternalServiceType service)
      throws UnsupportedEncodingException {

    StringBuilder form = new StringBuilder();
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createFormBeginTag(HtmlUtil.createUrl(url)+ ADDR, null, HtmlConsts.GET));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.FORM_ID,
        encodeParameter(odkId)));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, SERVICE, encodeParameter(service
        .toString())));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, service.getServiceName()));
    form.append(HtmlConsts.FORM_CLOSE);

    return form.toString();
  }

}
