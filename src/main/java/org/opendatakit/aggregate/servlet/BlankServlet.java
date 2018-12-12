/*
 * Copyright (C) 2010 University of Washington
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class BlankServlet extends ServletUtilBase {

  /**
   * URI from base
   */
  public static final String ADDR = "www/blank";
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -2220857379519391127L;

  /**
   * Handler for HTTP Get request to create blank page that is navigable
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *     javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    beginBasicHtmlResponse(BasicConsts.EMPTY_STRING, resp, cc); // header info
    finishBasicHtmlResponse(resp);
  }

}

