/*
 * Copyright (C) 2011 University of Washington.
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
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.structure.XmlAttachmentFormatter;
import org.opendatakit.aggregate.format.structure.XmlFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet to generate the XML representation of a given submission entry and
 * return the attachments associated with that submission.
 * 
 * Used by Briefcase 2.0 download.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionDownloadServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -5861240658170389989L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/downloadSubmission";

  /**
   * Handler for HTTP Get request that responds with the XML
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // verify parameters are present
    String keyString = getParameter(req, ServletConsts.FORM_ID);
    if (keyString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }
    SubmissionKey key = new SubmissionKey(keyString);

    List<SubmissionKeyPart> parts = key.splitSubmissionKey();
    Submission sub = null;
    try {
      IForm form = FormFactory.retrieveFormByFormId(parts.get(0).getElementName(), cc);
      if (!form.hasValidFormDefinition()) {
        errorRetreivingData(resp);
        return; // ill-formed definition
      }
      sub = Submission.fetchSubmission(parts, cc);

      if (sub != null) {

        resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
        resp.setContentType(HtmlConsts.RESP_TYPE_XML);
        addOpenRosaHeaders(resp);

        PrintWriter out = resp.getWriter();
        out.write("<submission xmlns=\"http://opendatakit.org/submissions\" xmlns:orx=\"http://openrosa.org/xforms\" >");
        out.write("<data>");
        XmlFormatter formatter = new XmlFormatter(out, form, cc);
        formatter.processSubmissions(Collections.singletonList(sub), cc);
        out.write("</data>\n");
        XmlAttachmentFormatter attach = new XmlAttachmentFormatter(out, form, cc);
        attach.processSubmissions(Collections.singletonList(sub), cc);
        out.write("</submission>");
        resp.setStatus(HttpServletResponse.SC_OK);
      } else {
        errorRetreivingData(resp);
      }
    } catch (ODKFormNotFoundException e1) {
      odkIdNotFoundError(resp);
      errorRetreivingData(resp);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      errorRetreivingData(resp);
    }
  }
}
