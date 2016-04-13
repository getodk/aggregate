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
package org.opendatakit.common.utils.gae;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opendatakit.common.utils.HttpClientFactory;
import org.opendatakit.http.conn.GaeHttpClientConnectionManager;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;

/**
 * Implementation that uses the underlying GAE URLFetchService.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public class GaeHttpClientFactoryImpl implements HttpClientFactory {

  public GaeHttpClientFactoryImpl() {
  }

  @Override
  public CloseableHttpClient createHttpClient(SocketConfig socketConfig, ConnectionConfig connectionConfig, RequestConfig requestConfig) {
	  
	  return HttpClientBuilder.create().setConnectionManager(new GaeHttpClientConnectionManager(socketConfig, connectionConfig, requestConfig)).build();
  }

  @Override
  public HttpTransport getGoogleOAuth2Transport() {
    return new UrlFetchTransport();
  }

}
