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
package org.opendatakit.common.security.gae;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.security.Realm;

public class RealmImpl implements Realm {
	public static final String GAE_REALM = "Google Account";
	
	final String realmString;
	final String mailToDomain;
	final String rootDomain;
	final List<String> domains = new ArrayList<String>();
	
	RealmImpl(String realmString, String mailToDomain, String rootDomain, List<String> domains) {
		this.realmString = realmString;
		this.mailToDomain = mailToDomain;
		this.rootDomain = rootDomain;
		this.domains.addAll(domains);
	}
	
	@Override
	public List<String> getDomains() {
		return domains;
	}

	@Override
	public String getMailToDomain() {
		return mailToDomain;
	}

	@Override
	public String getRealmString() {
		return realmString;
	}

	@Override
	public String getRootDomain() {
		return rootDomain;
	}

}
