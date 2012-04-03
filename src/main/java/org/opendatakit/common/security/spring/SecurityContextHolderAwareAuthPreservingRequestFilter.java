package org.opendatakit.common.security.spring;
/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 * Copyright 2012 University of Washington
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;


/**
 * Updated to preserve any authentication context from the newly-wrapped request.
 *  
* A <code>Filter</code> which populates the <code>ServletRequest</code> with a request wrapper
* which implements the servlet API security methods.
* <p>
* The wrapper class used is {@link SecurityContextHolderAwareRequestWrapper}.
*
* @author Orlando Garcia Carmona
* @author Ben Alex
* @author Luke Taylor
*/
public class SecurityContextHolderAwareAuthPreservingRequestFilter extends GenericFilterBean {
   //~ Instance fields ================================================================================================

   private String rolePrefix;

   //~ Methods ========================================================================================================

   public void setRolePrefix(String rolePrefix) {
       Assert.notNull(rolePrefix, "Role prefix must not be null");
       this.rolePrefix = rolePrefix.trim();
   }

   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
           throws IOException, ServletException {
     SecurityContextHolderAwareRequestWrapper wrapper = 
         new SecurityContextHolderAwareRequestWrapper((HttpServletRequest) req, rolePrefix);
     // preserve any authentication from the original req into the wrapped request
     Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
     if ( auth != null ) {
       SecurityContextHolder.getContext().setAuthentication(auth);
     }
     chain.doFilter(wrapper, res);
   }
}
