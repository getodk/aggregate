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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.PersistentResults.ResultType;
import org.opendatakit.aggregate.task.KmlGenerator;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * Servlet to generate a KML file for download
 * 
 * @author alerer@gmail.com
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class KmlServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 2387155275645640699L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/kml";

  public static final String IMAGE_FIELD = "imageField";

  public static final String TITLE_FIELD = "titleField";

  public static final String GEOPOINT_FIELD = "geopointField";

  /**
   * Handler for HTTP Get request that responds with CSV
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

    // get parameter
    String formId = getParameter(req, ServletConsts.FORM_ID);

    String geopointFieldName = getParameter(req, GEOPOINT_FIELD);
    String titleFieldName = getParameter(req, TITLE_FIELD);
    String imageFieldName = getParameter(req, IMAGE_FIELD);

    if (formId == null || geopointFieldName == null) {
      errorMissingKeyParam(resp);
      return;
    }

    Form form = null;
    try {
      form = Form.retrieveForm(formId, cc);

      FormElementModel titleField = null;
      if (titleFieldName != null) {
        FormElementKey titleKey = new FormElementKey(titleFieldName);
        titleField = FormElementModel.retrieveFormElementModel(form, titleKey);
      }
      FormElementModel geopointField = null;
      if (geopointFieldName != null) {
        FormElementKey geopointKey = new FormElementKey(geopointFieldName);
        geopointField = FormElementModel.retrieveFormElementModel(form, geopointKey);
      }
      FormElementModel imageField = null;
      if (imageFieldName != null) {
        if (!imageFieldName.equals(KmlSettingsServlet.NONE)) {
          FormElementKey imageKey = new FormElementKey(imageFieldName);
          imageField = FormElementModel.retrieveFormElementModel(form, imageKey);
        }
      }

      Map<String, String> params = new HashMap<String, String>();
      params.put(KmlServlet.TITLE_FIELD, (titleField == null) ? null : titleField
          .constructFormElementKey(form).toString());
      params.put(KmlServlet.IMAGE_FIELD, (imageField == null) ? KmlSettingsServlet.NONE : imageField
          .constructFormElementKey(form).toString());
      params.put(KmlServlet.GEOPOINT_FIELD, (geopointField == null) ? null : geopointField
          .constructFormElementKey(form).toString());

      PersistentResults r = new PersistentResults(ResultType.KML, form, params, cc);
      r.persist(cc);

      KmlGenerator generator = (KmlGenerator) cc.getBean(BeanDefs.KML_BEAN);
      generator.createKmlTask(form, r.getSubmissionKey(), 1L, cc);

    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    }

    resp.sendRedirect(cc.getWebApplicationURL(ResultServlet.ADDR));

  }
}
