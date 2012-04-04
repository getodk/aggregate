/*
 * Copyright (C) 2012 University of Washington.
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
package org.opendatakit.common.security.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

/**
 * Copied from Spring Security SavedRequestAwareAuthenticationSuccessHandler
 * Prefers using the redirect target URL (coming in as a query string parameter)
 * over the saved request URL.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class TargetUrlRequestAwareAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  protected final Log logger = LogFactory.getLog(this.getClass());

  private RequestCache requestCache = new HttpSessionRequestCache();

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
          Authentication authentication) throws ServletException, IOException {
      SavedRequest savedRequest = requestCache.getRequest(request, response);

      String targetUrlParameter = getTargetUrlParameter();
      if (isAlwaysUseDefaultTargetUrl() || (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
          requestCache.removeRequest(request, response);
          super.onAuthenticationSuccess(request, response, authentication);

          return;
      }

      // fall back to SimpleUrl actions only if no targetUrlParameter
      if (savedRequest == null) {
          super.onAuthenticationSuccess(request, response, authentication);

          return;
      }

      clearAuthenticationAttributes(request);

      // Use the DefaultSavedRequest URL
      String targetUrl = savedRequest.getRedirectUrl();
      logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
      getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  public void setRequestCache(RequestCache requestCache) {
      this.requestCache = requestCache;
  }
}