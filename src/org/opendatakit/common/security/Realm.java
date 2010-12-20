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
import java.util.List;
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

	private String realmString;
	private String mailToDomain;
	private String rootDomain;
	private Set<String> domains = new HashSet<String>();
	
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
		// root domain implicitly granted access
		domains.add(rootDomain);
		domains.add(mailToDomain);
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

	public Set<String> getDomains() {
		return Collections.unmodifiableSet(domains);
	}

	public void setDomains(Set<String> domains) {
		this.domains.clear();
		this.domains.addAll(domains);
	}
}
