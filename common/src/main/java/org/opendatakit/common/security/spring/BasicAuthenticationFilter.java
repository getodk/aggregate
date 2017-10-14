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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.web.context.ServletContextAware;

/**
 * Wraps the Spring class and ensures that if an Authentication is already 
 * determined for this request, that it isn't overridden.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class BasicAuthenticationFilter implements Filter,
    BeanNameAware, DisposableBean, InitializingBean, EnvironmentAware, ServletContextAware {

  org.springframework.security.web.authentication.www.BasicAuthenticationFilter impl;
  
  public BasicAuthenticationFilter(AuthenticationManager authenticationManager) {
    impl = new org.springframework.security.web.authentication.www.BasicAuthenticationFilter(authenticationManager);
  }
  
  public BasicAuthenticationFilter(AuthenticationManager authenticationManager,
      AuthenticationEntryPoint authenticationEntryPoint) {
    impl = new org.springframework.security.web.authentication.www.BasicAuthenticationFilter(authenticationManager, authenticationEntryPoint);
  }
  
  public void  setAuthenticationDetailsSource(AuthenticationDetailsSource<javax.servlet.http.HttpServletRequest,?> authenticationDetailsSource) {
    impl.setAuthenticationDetailsSource(authenticationDetailsSource);
  }
  
  public void  setCredentialsCharset(String credentialsCharset) {
    impl.setCredentialsCharset(credentialsCharset);
  }
  
  public void  setRememberMeServices(RememberMeServices rememberMeServices) {
    impl.setRememberMeServices(rememberMeServices);
  }
  
  @Override
  public void  afterPropertiesSet() {
    impl.afterPropertiesSet();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if ( SecurityContextHolder.getContext().getAuthentication() == null ) {
      impl.doFilter(request, response, chain);
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    impl.setServletContext(servletContext);
    
  }

  @Override
  public void setEnvironment(Environment environment) {
    impl.setEnvironment(environment);
  }

  @Override
  public void setBeanName(String beanName) {
    impl.setBeanName(beanName);
  }

  @Override
  public void destroy() {
    impl.destroy();
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    impl.init(filterConfig);
  }

}
