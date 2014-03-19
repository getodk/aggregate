/*
 * Copyright (C) 2014 University of Washington
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

package org.opendatakit.aggregate.odktables.impl.api;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.web.CallingContext;

/**
 * AppEngine does automatic GZIP stream processing as part of its supplied
 * framework. However, it does not remove the headers indicating that this has
 * occurred. So the RestEasy code tries to double-unzip or double-zip the
 * content.
 *
 * This filter works around that by removing the Content-Encoding header before
 * the request gets to the RestEasy layers.
 *
 * NOTE: behavior has changed between 1.8.9 and 1.9.0 on development server.
 * Beware!
 *
 * @author mitchellsundt@gmail.com
 *
 */
@PreMatching
@ConstrainedTo(RuntimeType.SERVER)
public class AppEngineContentEncodingSuppressionRequestFilter implements ContainerRequestFilter {

  @Context
  ServletContext sc;
  @Context
  HttpServletRequest req;

  @Override
  public void filter(ContainerRequestContext crc) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String server = sc.getServerInfo();
    boolean isGaeDevelopmentEnvironment = server.contains("Development");
    boolean isGaeEnvironment = cc.getUserService().getCurrentRealm().getIsGaeEnvironment();
    if (isGaeEnvironment && !isGaeDevelopmentEnvironment) {
      MultivaluedMap<String, String> headers = crc.getHeaders();
      headers.remove(ApiConstants.CONTENT_ENCODING_HEADER);
    }
  }

}
