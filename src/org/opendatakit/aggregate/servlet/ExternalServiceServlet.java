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
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Servlet to setup connection to an external repository
 * 
 * @author wbrunette@gmail.com
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
    if (odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    String serviceString = getParameter(req, SERVICE);

    // get form
    Datastore ds = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);
    Form form;
    try {
      form = Form.retrieveForm(odkId, ds, user, userService.getCurrentRealm());
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }

    beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3,
        "Create a Connection for Form: " + "<FONT COLOR=0000FF>"
            + form.getViewableName() + "</FONT>"));

    if (serviceString == null) {
      // TODO: change so is based off enum
      out
          .write(generateServiceButton(odkId,
              ExternalService.GOOGLE_SPREADSHEET));
      out.write(generateServiceButton(odkId, ExternalService.RHIZA_INSIGHT));
      out.write(generateServiceButton(odkId,
          ExternalService.GOOGLE_FUSIONTABLES));

    } else {
      ExternalService service = ExternalService.valueOf(serviceString);
      out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, "To: "
          + "<FONT COLOR=0000FF>" + service.getServiceName() + "</FONT>"
          + " Service"));
      out.write(generateExternalServiceEntry(odkId, service));
    }

    finishBasicHtmlResponse(resp);
  }

  private String generateExternalServiceEntry(String odkId,
      ExternalService service) throws UnsupportedEncodingException {
    StringBuilder form = new StringBuilder();
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createFormBeginTag(service.getAddr(), HtmlConsts.RESP_TYPE_HTML, HtmlConsts.GET));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.ODK_ID, encodeParameter(odkId)));
    
    if(!service.equals(ExternalService.GOOGLE_FUSIONTABLES)){
      form.append(service.getDescriptionOfParam() + HtmlConsts.LINE_BREAK);
      form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT, ServletConsts.SPREADSHEET_NAME_PARAM, null));
    }

    form.append(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    form.append(generateRadioOption(ExternalServiceOption.UPLOAD_ONLY, true));
    form.append(generateRadioOption(ExternalServiceOption.STREAM_ONLY, false));
    form.append(generateRadioOption(ExternalServiceOption.UPLOAD_N_STREAM,
        false));
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
        ServletConsts.CREATE_EXTERNAL_SERVICE_BUTTON_LABEL));
    form.append(HtmlConsts.FORM_CLOSE);
    return form.toString();
  }

  private String generateRadioOption(ExternalServiceOption option,
      boolean checked) {
    return HtmlUtil.createRadio(ServletConsts.EXTERNAL_SERVICE_TYPE, option
        .toString(), option.getDescriptionOfOption(), checked);
  }

  private String generateServiceButton(String odkId,
      ExternalService service) throws UnsupportedEncodingException {

    StringBuilder form = new StringBuilder();
    form.append(HtmlConsts.LINE_BREAK);
    form.append(HtmlUtil
        .createFormBeginTag("/" + ADDR, null, HtmlConsts.GET));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
        ServletConsts.ODK_ID, encodeParameter(odkId)));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, SERVICE,
        encodeParameter(service.toString())));
    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
        service.getServiceName()));
    form.append(HtmlConsts.FORM_CLOSE);

    return form.toString();
  }

  private enum ExternalService {
    GOOGLE_SPREADSHEET("Google Spreadsheet", SpreadsheetServlet.ADDR,
        ServletConsts.SPEADSHEET_NAME_LABEL), RHIZA_INSIGHT("Rhiza Insight",
        JsonServlet.ADDR, "Rhiza Insight Server Address"), GOOGLE_FUSIONTABLES(
        "Google FusionTables", FusionTableServlet.ADDR, "Fusion Table ID");

    private String serviceName;

    private String addr;

    private String descriptionOfParam;

    private ExternalService(String name, String servletAddr, String desc) {
      serviceName = name;
      addr = servletAddr;
      descriptionOfParam = desc;
    }

    public String getAddr() {
      return addr;
    }

    public String getDescriptionOfParam() {
      return descriptionOfParam;
    }

    public String getServiceName() {
      return serviceName;
    }

  }

}
