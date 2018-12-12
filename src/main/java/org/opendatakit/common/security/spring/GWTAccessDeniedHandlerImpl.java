/*
 * Copyright (C) 2011 University of Washington
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

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;

/**
 * An AccessDenied handler that uses the presence of a special header to identify
 * GWT requests and causes those requests to have an
 * {@link org.opendatakit.common.security.client.exception.AccessDeniedException} thrown
 * within the GWT RPC mechanism.  This necessitates that all <code>GWT.create()</code>
 * requests be handled through the <code>SecureGWT</code> class.
 * <p>
 * If the header is not present, the configured access denied handler is used.
 *
 * @author mitchellsundt@gmail.com
 */
public class GWTAccessDeniedHandlerImpl implements AccessDeniedHandler, ServletContextAware,
    InitializingBean {

  AccessDeniedHandler pImpl = null;
  String headerString = null;
  ServletContext context = null;

  GWTAccessDeniedHandlerImpl() {
  }

  public void setOrdinaryAccessDeniedHandler(AccessDeniedHandler pImpl) {
    this.pImpl = pImpl;
  }

  public void setGwtHeader(String headerString) {
    this.headerString = headerString;
  }

  @Override
  public void setServletContext(ServletContext arg0) {
    context = arg0;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(pImpl, "ordinaryAccessDeniedHandler must be specified");
    Assert.notNull(headerString, "gwtHeader must be specified");
  }

  @Override
  public void handle(HttpServletRequest arg0, HttpServletResponse arg1,
                     AccessDeniedException arg2) throws IOException, ServletException {
    String headerValue = arg0.getHeader(headerString);
    if (headerValue != null) {
      // it is a GWT request...
      AccessDeniedRemoteServiceServlet s = new AccessDeniedRemoteServiceServlet(
          arg2, context);
      s.doPost(arg0, arg1);
    } else {
      // delegate
      pImpl.handle(arg0, arg1, arg2);
    }
  }

  /**
   * Inner class that processes a GWT request and always "throws" an
   * org.opendatakit.common.security.exception.AccessDeniedException
   * back to the caller (via the proprietary GWT RPC mechanism).
   *
   * @author mitchellsundt@gmail.com
   */
  private static class AccessDeniedRemoteServiceServlet extends
      RemoteServiceServlet {
    /**
     *
     */
    private static final long serialVersionUID = -4524746517720874953L;

    AccessDeniedException e;
    ServletContext context;

    AccessDeniedRemoteServiceServlet(AccessDeniedException e, ServletContext context) {
      super();
      this.e = e;
      this.context = context;
    }

    /**
     * This is taken from GWT 2.4 sources.  The changed code was to, rather than
     * call the actual XxxxServer method that would have handled the request,
     * simply encode an AccessDeniedException as the failure response for the request.
     * <p>
     * Revisions to newer GWT versions should verify that this code is still
     * suitable for those newer GWT versions.
     */
    @Override
    public String processCall(String payload) throws SerializationException {
      try {
        RPCRequest rpcRequest = RPC.decodeRequest(payload);
        onAfterRequestDeserialized(rpcRequest);
        // ******* CHANGED GWT 2.4 CODE STARTS
        LoggerFactory.getLogger(GWTAccessDeniedHandlerImpl.class).warn("GWT Method: "
            + rpcRequest.getMethod().getName() + " Exception: " + e.getMessage());
        return RPC
            .encodeResponseForFailure(
                rpcRequest.getMethod(),
                new org.opendatakit.common.security.client.exception.AccessDeniedException(
                    e));
        // ******** CHANGED GWT 2.4 CODE ENDS
      } catch (IncompatibleRemoteServiceException ex) {
        LoggerFactory.getLogger(GWTAccessDeniedHandlerImpl.class).warn("An IncompatibleRemoteServiceException was thrown while processing this call.",
            ex);
        return RPC.encodeResponseForFailure(null, ex);
      }
    }

    @Override
    public ServletContext getServletContext() {
      return context;
    }
  }

}
