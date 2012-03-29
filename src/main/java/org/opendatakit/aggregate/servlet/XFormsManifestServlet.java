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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.form.XFormsManifestXmlTable;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet to generate the OpenRosa-compliant XML list of forms to be presented
 * as the API for forms for computers
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class XFormsManifestServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 13236849409070038L;

  /**
   * URI from base
   */
  public static final String ADDR = "xformsManifest";

  /**
   * Handler for HTTP Get request that responds with an XML list of forms to
   * download
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    addOpenRosaHeaders(resp);

    // get parameters
    String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    IForm form;
    try {
      form = FormFactory.retrieveFormByFormId(formId, cc);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      odkIdNotFoundError(resp);
      return;
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      datastoreError(resp);
      return;
    }

    XFormsManifestXmlTable formFormatter = new XFormsManifestXmlTable(form, cc.getServerURL());
    resp.setContentType(HtmlConsts.RESP_TYPE_XML);
    PrintWriter out = resp.getWriter();
    try {
      formFormatter.generateXmlManifestList(out, cc);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      datastoreError(resp);
      return;
    }
  }

}
