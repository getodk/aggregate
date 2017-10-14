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

import javax.servlet.ServletRequest;

import org.springframework.security.web.PortResolver;

/**
 * PortResolver that makes its decisions solely based upon the scheme.
 * This is used by the channel security filter.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class PortResolverBySchemeImpl implements PortResolver {

	int port = 80;
	int securePort = 443;
	
	public PortResolverBySchemeImpl() {
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getSecurePort() {
		return securePort;
	}

	public void setSecurePort(int securePort) {
		this.securePort = securePort;
	}

	@Override
	public int getServerPort(ServletRequest request) {
		String scheme = request.getScheme();
		if ( scheme.equals("https") ) {
			return securePort;
		} else {
			return port;
		}
	}

}
