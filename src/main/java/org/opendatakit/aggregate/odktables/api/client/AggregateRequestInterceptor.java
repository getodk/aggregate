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
package org.opendatakit.aggregate.odktables.api.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

public class AggregateRequestInterceptor implements ClientHttpRequestInterceptor {

  private String accessToken;

  public AggregateRequestInterceptor(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    HttpRequest wrappedRequest = new AddAccessTokenHttpRequestWrapper(request);
    return execution.execute(wrappedRequest, body);
  }

  private class AddAccessTokenHttpRequestWrapper extends HttpRequestWrapper {

    public AddAccessTokenHttpRequestWrapper(HttpRequest request) {
      super(request);
    }

    @Override
    public URI getURI() {
      String uriString = super.getURI().toString();
      String accessTokenQuery = "access_token=" + accessToken;
      if (super.getURI().getQuery() == null) {
        accessTokenQuery = "?" + accessTokenQuery;
      } else {
        accessTokenQuery = "&" + accessTokenQuery;
      }
      URI uri = URI.create(uriString + accessTokenQuery);
      return uri;
    }

  }

}
