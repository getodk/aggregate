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
package org.opendatakit.aggregate.odktables.rest.interceptor;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class AggregateRequestInterceptor implements ClientHttpRequestInterceptor {

  private URI uriBase;
  private String accessToken;
  private List<MediaType> mediaTypes;

  public AggregateRequestInterceptor(URI uriBase, String accessToken) {
    this(uriBase, accessToken, null);
  }

  public AggregateRequestInterceptor(URI uriBase, String accessToken, List<MediaType> mediaTypes) {
    this.uriBase = uriBase;
    this.accessToken = accessToken;
    this.mediaTypes = mediaTypes;
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    if (accessToken != null && uriBase != null) {
      if (request.getURI().getHost().equals(uriBase.getHost())
          && request.getURI().getPort() == uriBase.getPort()) {
        request.getHeaders().set("Authorization", "Bearer " + accessToken);
      }
    }
    if (mediaTypes != null && mediaTypes.size() != 0) {
      request.getHeaders().setAccept(mediaTypes);
    }
    return execution.execute(request, body);
  }

}
