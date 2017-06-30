/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */
package org.opendatakit.aggregate.odktablesperf.api;

import org.apache.wink.client.ClientAuthenticationException;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.common.http.HttpStatus;
import org.opendatakit.aggregate.odktables.api.exceptions.InvalidAuthTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SecurityHandler for a client to adjust the bearer access token
 */
public class ReAuthSecurityHandler implements ClientHandler {

  private static Logger logger = LoggerFactory.getLogger(ReAuthSecurityHandler.class);

  private static final int UNAUTHORIZED = HttpStatus.UNAUTHORIZED.getCode();

  private AggregateSynchronizer sync;

  public ReAuthSecurityHandler(AggregateSynchronizer sync) {
    this.sync = sync;
  }

  /**
   * Performs basic HTTP authentication and proxy authentication, if necessary.
   * 
   * @param client
   *          request object
   * @param handler
   *          context object
   * @return a client response object that may contain an HTTP Authorization
   *         header
   */
  public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
    logger.trace("Entering BasicAuthSecurityHandler.doChain()"); //$NON-NLS-1$
    ClientResponse response = context.doChain(request);
    if (response.getStatusCode() == UNAUTHORIZED) {
      response.consumeContent();
      String accessToken;
      try {
        accessToken = sync.updateAccessToken();
      } catch ( InvalidAuthTokenException e ) {
        throw new ClientAuthenticationException(
            "serviceFailedToAuthenticateWithBearerToken"); //$NON-NLS-1$
      }
      request.getHeaders().putSingle("Authorization", "Bearer " + accessToken);
      logger.trace("Issuing request again with Authorization header"); //$NON-NLS-1$
      response = context.doChain(request);
      if (response.getStatusCode() == UNAUTHORIZED) {
        logger
            .trace("After sending request with Authorization header, still got " + UNAUTHORIZED + " response"); //$NON-NLS-1$
        throw new ClientAuthenticationException(
            "serviceFailedToAuthenticateWithBearerToken"); //$NON-NLS-1$
      } else {
        logger.trace("Got a non-" + UNAUTHORIZED + " response, so returning response"); //$NON-NLS-1$
        return response;
      }
    } else {
      logger.trace("Status code was not " + UNAUTHORIZED + " so no need to re-issue request."); //$NON-NLS-1$
      return response;
    }
  }

}
