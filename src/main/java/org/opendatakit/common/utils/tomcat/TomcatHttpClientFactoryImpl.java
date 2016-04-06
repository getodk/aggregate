/*
 * Copyright (C) 2011 University of Washington.
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
package org.opendatakit.common.utils.tomcat;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opendatakit.common.utils.HttpClientFactory;

import com.google.api.client.googleapis.apache.GoogleApacheHttpTransport;
import com.google.api.client.http.HttpTransport;

/**
 * Implementation that just uses Apache's default http client.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class TomcatHttpClientFactoryImpl implements HttpClientFactory {

  public TomcatHttpClientFactoryImpl() {
  }

  @Override
  public CloseableHttpClient createHttpClient(SocketConfig socketConfig,
      ConnectionConfig connectionConfig, RequestConfig requestConfig) {
    HttpClientBuilder builder = HttpClientBuilder.create();
    if (socketConfig != null) {
      builder.setDefaultSocketConfig(socketConfig);
    }
    if (connectionConfig != null) {
      builder.setDefaultConnectionConfig(connectionConfig);
    }
    if (requestConfig != null) {
      builder.setDefaultRequestConfig(requestConfig);
    }
    return builder.build();
  }

  @Override
  public HttpTransport getGoogleOAuth2Transport() throws GeneralSecurityException, IOException {
    return GoogleApacheHttpTransport.newTrustedTransport();
  }

}
