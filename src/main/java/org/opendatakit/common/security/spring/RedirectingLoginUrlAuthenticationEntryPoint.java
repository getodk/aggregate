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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.security.web.util.UrlUtils;

/**
 * Respects the ?redirect=URL string during login and redirects to it
 * upon successful login.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class RedirectingLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
  
  private Log logger = LogFactory.getLog(RedirectingLoginUrlAuthenticationEntryPoint.class);
  
  public RedirectingLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
    super(loginFormUrl);
  }
  

  protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response,
          AuthenticationException authException) {

      String loginForm = determineUrlToUseForThisRequest(request, response, authException);

      if (UrlUtils.isAbsoluteUrl(loginForm)) {
          return loginForm;
      }

      int serverPort = getPortResolver().getServerPort(request);
      String scheme = request.getScheme();

      RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();

      urlBuilder.setScheme(scheme);
      urlBuilder.setServerName(request.getServerName());
      urlBuilder.setPort(serverPort);
      urlBuilder.setContextPath(request.getContextPath());
      urlBuilder.setPathInfo(loginForm);
      try {
        String fullRequest = request.getRequestURL().toString();
        if ( request.getQueryString() != null ) {
          fullRequest += "?" + request.getQueryString();
        }
        String redirectParam = "redirect=" + URLEncoder.encode(fullRequest,"UTF-8");
        urlBuilder.setQuery(redirectParam);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      if (isForceHttps() && "http".equals(scheme)) {
          Integer httpsPort = getPortMapper().lookupHttpsPort(Integer.valueOf(serverPort));

          if (httpsPort != null) {
              // Overwrite scheme and port in the redirect URL
              urlBuilder.setScheme("https");
              urlBuilder.setPort(httpsPort.intValue());
          } else {
              logger.warn("Unable to redirect to HTTPS as no port mapping found for HTTP port " + serverPort);
          }
      }

      return urlBuilder.getUrl();
  }

}
