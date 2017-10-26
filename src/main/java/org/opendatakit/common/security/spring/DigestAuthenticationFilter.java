/*
 * Copyright (C) 2012 University of Washington
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

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Wraps the Spring class and ensures that if an Authentication is already 
 * determined for this request, that it isn't overridden.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class DigestAuthenticationFilter extends org.springframework.security.web.authentication.www.DigestAuthenticationFilter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if ( SecurityContextHolder.getContext().getAuthentication() == null ) {
      super.doFilter(request, response, chain);
    } else {
      chain.doFilter(request, response);
    }
  }

}
