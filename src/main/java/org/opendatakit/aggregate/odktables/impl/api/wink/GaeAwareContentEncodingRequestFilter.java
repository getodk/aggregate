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
package org.opendatakit.aggregate.odktables.impl.api.wink;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.server.internal.servlet.contentencode.ContentEncodingRequestFilter;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.CallingContext;

public class GaeAwareContentEncodingRequestFilter extends ContentEncodingRequestFilter {

  private static final Log logger = LogFactory.getLog(GaeAwareContentEncodingRequestFilter.class);

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain chain) throws IOException, ServletException {

    if (servletRequest instanceof HttpServletRequest) {

      HttpServletRequest req = (HttpServletRequest) servletRequest;
      ServletContext sc = req.getSession().getServletContext();
      CallingContext cc = ContextFactory.getCallingContext(sc, req);
      String server = sc.getServerInfo();

      /*
       * AppEngine leaves the GZIP header even though it unzips the content
       * before delivering it to the app.
       */
      boolean isGaeDevelopmentEnvironment = server.contains("Development");
      boolean isGaeEnvironment = false;
      try {
        UserService us = cc.getUserService();
        if (us != null) {
          Realm realm = us.getCurrentRealm();
          if (realm != null) {
            Boolean outcome = realm.getIsGaeEnvironment();
            if (outcome != null) {
              isGaeEnvironment = outcome;
            }
          }
        }
      } catch (Exception e) {
        // ignore...
      }

      if (isGaeEnvironment && !isGaeDevelopmentEnvironment) {
        // don't try to process anything -- GAE does but does not remove headers
        logger.info("Gae environment -- ignoring Content-Encoding header");
        chain.doFilter(servletRequest, servletResponse);
      } else {
        // perhaps wrap response with GZIP
        logger.info("not Gae environment -- processing Content-Encoding header");
        super.doFilter(servletRequest, servletResponse, chain);
      }
    }
  }
}
