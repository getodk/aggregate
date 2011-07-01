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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	private String mailToDomain;
	private String rootDomain;
	private String domains;
	private Set<String> domainSet = new HashSet<String>();
	
	public Realm() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if ( mailToDomain == null ) {
			throw new IllegalStateException("mailToDomain (e.g., mydomain.org) must be specified");
		}
		if ( realmString == null ) {
			realmString = mailToDomain;
		}
		if ( rootDomain == null ) {
			rootDomain = mailToDomain;
		}
		if ( domains == null ) { 
			domains = rootDomain;
		}
		String[] elems = domains.split("[, ]");
		for ( String e : elems ) {
			domainSet.add(e);
		}
		// root domain implicitly granted access
		domainSet.add(rootDomain);
		domainSet.add(mailToDomain);
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

	public String getMailToDomain() {
		return mailToDomain;
	}

	public void setMailToDomain(String mailToDomain) {
		this.mailToDomain = mailToDomain;
	}

	public String getRootDomain() {
		return rootDomain;
	}

	public void setRootDomain(String rootDomain) {
		this.rootDomain = rootDomain;
	}

	public void setDomains(String domains) {
		this.domains = domains;
	}
	
	public Set<String> getDomainSet() {
		return Collections.unmodifiableSet(domainSet);
	}

	public void setDomainSet(Set<String> domains) {
		this.domainSet.clear();
		this.domainSet.addAll(domains);
	}
}
