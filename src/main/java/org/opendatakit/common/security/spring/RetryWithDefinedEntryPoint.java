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

import org.springframework.security.web.access.channel.AbstractRetryEntryPoint;

/**
 * Enforce the port assignment for a given scheme (i.e., http or https).
 * This is used by the channel security filter.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class RetryWithDefinedEntryPoint extends AbstractRetryEntryPoint {

	String scheme;
	int port;
	
    public RetryWithDefinedEntryPoint(String scheme, int port) {
        super(scheme, (scheme.contains("s") ? 443 : 80)); // standard port...
        this.scheme = scheme;
        this.port = port;
    }

    protected Integer getMappedPort(Integer mapFromPort) {
    	// don't care about the origin port -- we only have one port pair.
        return port;
    }
}
