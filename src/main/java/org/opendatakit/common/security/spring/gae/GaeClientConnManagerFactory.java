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
package org.opendatakit.common.security.spring.gae;

import org.apache.http.conn.ClientConnectionManager;
import org.opendatakit.http.conn.GaeClientConnectionManager;
import org.openid4java.util.ClientConnectionManagerFactory;

/**
 * Support using openid4java on Google AppEngine.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class GaeClientConnManagerFactory implements ClientConnectionManagerFactory {

  public GaeClientConnManagerFactory() {}
  
  @Override
  public ClientConnectionManager getConnectionManager() {
    return new GaeClientConnectionManager( 
        GaeClientConnectionManager.DEFAULT_SCHEME_REGISTRY);
  }

}
