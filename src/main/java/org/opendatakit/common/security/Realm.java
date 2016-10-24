/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.common.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.springframework.beans.factory.InitializingBean;

/**
 * A bean class used to capture configuration values about this server
 * deployment, its default mailto: domain and the service domains it
 * authorizes.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class Realm implements InitializingBean {

	private boolean sslIsRequired = false;
	private boolean sslIsAvailable = false;
	private Integer port;
	private Integer securePort;
	private String hostname;
	private String realmString;
	private boolean isGaeEnvironment = false;

	public Realm() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if ( realmString == null ) {
			throw new IllegalStateException("realmString (e.g., mydomain.org ODK Aggregate 1.0) must be specified");
		}
		Log log = LogFactory.getLog(Realm.class);
		log.info("Version: " + UIConsts.VERSION_STRING);
		log.info("Hostname: " + hostname);
		log.info("Port: " + Integer.toString(port));
		log.info("SecurePort: " + Integer.toString(securePort));
		log.info("SslIsRequired: " + (sslIsRequired ? "yes" : "no") );
		log.info("SslIsAvailable: " + (sslIsAvailable ? "yes" : "no") );
		log.info("RealmString: " + realmString);
      log.info("isGaeEnvironment: " + (isGaeEnvironment ? "yes" : "no") );
      log.info("java.library.path: " + System.getProperty("java.library.path"));
	}

	public void setSecureChannelType(String type) {
		if ( type != null && type.equals("REQUIRES_SECURE_CHANNEL") ) {
			sslIsAvailable = true;
		}
	}

	public boolean isSslAvailable() {
		return sslIsAvailable;
	}

	public void setChannelType(String type) {
		if ( type != null && type.equals("REQUIRES_SECURE_CHANNEL") ) {
			sslIsRequired = true;
		}
	}

	public boolean isSslRequired() {
		return sslIsRequired;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getSecurePort() {
		return securePort;
	}

	public void setSecurePort(Integer securePort) {
		this.securePort = securePort;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getRealmString() {
		return realmString;
	}

	public void setRealmString(String realmString) {
		this.realmString = realmString;
	}

	public boolean getIsGaeEnvironment() {
	  return isGaeEnvironment;
	}

	public void setIsGaeEnvironment(boolean isGaeEnvironment) {
	  this.isGaeEnvironment = isGaeEnvironment;
	}

}
