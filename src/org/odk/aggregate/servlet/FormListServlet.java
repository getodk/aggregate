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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.table.FormXmlTable;

/**
 * Servlet to generate the XML list of forms to be
 * presented as the API for forms for computers
 *
 * @author wbrunette@gmail.com
 *
 */
public class FormListServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 13236849409070038L;

  /**
   * URI from base
   */
  public static final String ADDR = "formList";
  
  /**
   * Handler for HTTP Get request that responds with an XML list of forms to download
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    resp.setContentType(ServletConsts.RESP_TYPE_XML);
    FormXmlTable xmlFormTable = new FormXmlTable(getServerURL(req));
    resp.getWriter().print(xmlFormTable.generateXmlListOfForms());
  }


}
