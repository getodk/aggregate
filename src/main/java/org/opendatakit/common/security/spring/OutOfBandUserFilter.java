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
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opendatakit.common.utils.OutOfBandUserFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * If the session does not already have an Authentication element,
 * this filter attempts to find an out-of-band (e.g., Google AppEngine
 * OAuthService) fetcher to return the user e-mail to use for
 * authorization decisions.  That e-mail needs to be in the registered
 * users table and assigned permissions on the website.
 * <p>
 * Any user returned by the outOfBandUserFetcher is assumed to have been
 * properly authenticated.  This mechanism could be used to obtain user
 * identities authenticated through a portal.
 *
 * @author mitchellsundt@gmail.com
 */
public class OutOfBandUserFilter extends GenericFilterBean {

  Logger logger = LoggerFactory.getLogger(OutOfBandUserFilter.class);

  AuthenticationProvider authenticationProvider = null;
  OutOfBandUserFetcher outOfBandUserFetcher = null;

  public AuthenticationProvider getAuthenticationProvider() {
    return authenticationProvider;
  }

  public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
  }

  public OutOfBandUserFetcher getOutOfBandUserFetcher() {
    return outOfBandUserFetcher;
  }

  public void setOutOfBandUserFetcher(OutOfBandUserFetcher outOfBandUserFetcher) {
    this.outOfBandUserFetcher = outOfBandUserFetcher;
  }

  @Override
  public void afterPropertiesSet() throws ServletException {
    super.afterPropertiesSet();
    Assert.notNull(outOfBandUserFetcher, "OutOfBandUserFetcher must be supplied.");
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        String userEmail = outOfBandUserFetcher.getEmail();

        if (userEmail != null) {

          // In the common case, if userEmail is non-null, the user should be known to the server.
          Authentication auth =
              authenticationProvider.authenticate(new OutOfBandAuthenticationToken(userEmail));

          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (AuthenticationException ex) {
        // if the authentication fails to recognize the user, silently ignore the failure.
        // Warnings were already logged by the AuthenticationProvider.
      }
    }

    chain.doFilter(request, response);
  }

}
