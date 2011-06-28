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
package org.opendatakit.common.security.common;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * Shared code between GWT Javascript and the server side.  This class 
 * defines the system-defined granted authority names.  The convention is that:
 * <ul><li>any name beginning with ROLE_ is a primitive authority.</li>  
 * <li>any name beginning with RUN_AS_ is a primitive run-as directive.</li>
 * </ul>
 * Only non-primitive names can be granted primitive authorities.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public enum GrantedAuthorityNames implements IsSerializable {

	AUTH_LOCAL("any users authenticated via the locally-held (<em>Aggregate password</em>) credential"),
	AUTH_OPENID("any users authenticated via OpenID"),
	
	USER_IS_ANONYMOUS("for unauthenticated access"),
	USER_IS_AUTHENTICATED("for authenticated users"),
	USER_IS_REGISTERED("for registered users of this system (a user identified " +
				"as a registered user will always have been authenticated)"),
	USER_IS_DAEMON("reserved for the execution of background tasks"),
	
	MAILTO_GMAIL_COM("all users logged in via OpenID with the 'gmail.com' e-mail domain"),
	
	ROLE_FORM_LIST("required to fetching the xforms list (e.g., by the device)"),
	ROLE_FORM_DOWNLOAD("required to fetch an xform definition (e.g., by the device)"),
	ROLE_SUBMISSION_UPLOAD("required to submit a filled-out xform"),
	ROLE_USER("required to view the home (forms) page and the human-readable xform xml listing"),
	ROLE_ATTACHMENT_VIEWER("required to view imagery, video, audio and other complex data in the form"),
	ROLE_ANALYST("required to view submissions and to generate csv and kml files and download them"),
	ROLE_SERVICES_ADMIN("required to configure external services and data publishing"),
	ROLE_FORM_ADMIN("required to upload new xforms, upload modifications to existing xforms, and to delete xforms or thier data"),
	ROLE_ACCESS_ADMIN("required for the permissions-management pages, including the registered users, group access rights, and user membership in groups"),
	;
	
	private final String description;
	
	GrantedAuthorityNames(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static final String GROUP_DATA_COLLECTORS = "Data Collector";
	public static final String GROUP_DATA_VIEWERS = "Data Viewer";
	public static final String GROUP_DATA_ADMINS = "Data Manager";
	public static final String GROUP_SITE_ADMINS = "Site Administrator";
	
	public static final String MAILTO_PREFIX = "MAILTO_";
	public static final String ROLE_PREFIX = "ROLE_";
	public static final String RUN_AS_PREFIX = "RUN_AS_";
	
	public static final boolean permissionsCanBeAssigned(String authority) {
		return (authority != null) && 
			!(authority.startsWith(ROLE_PREFIX) || authority.startsWith(RUN_AS_PREFIX));
	}
	
	public static final String getMailtoGrantedAuthorityName(String mailtoDomain) {
		if ( mailtoDomain == null ) return null;
		return GrantedAuthorityNames.MAILTO_PREFIX + 
			mailtoDomain.replaceAll("[^\\p{Digit}\\p{Lu}\\p{Lo}\\p{Ll}]", "_").toUpperCase();
	}
}
