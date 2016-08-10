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

import java.io.Serializable;


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
public enum GrantedAuthorityName implements Serializable {

	AUTH_LOCAL("any users authenticated via the locally-held (<em>Aggregate password</em>) credential"),
   AUTH_OUT_OF_BAND("any users authenticated vio Out-of-band mechanisms"),
	AUTH_OPENID("any users authenticated via OpenID"),
   AUTH_GOOGLE_OAUTH2("any users authenticated via Google Oauth2 proxy"),

	USER_IS_ANONYMOUS("for unauthenticated access"),
	USER_IS_REGISTERED("for registered users of this system (a user identified " +
				"as a registered user will always have been authenticated)"),
	USER_IS_DAEMON("reserved for the execution of background tasks"),

	ROLE_USER("required to view the home (forms) page and the human-readable xform xml listing"),
	ROLE_DATA_COLLECTOR("required to fetch forms, manifests, multimedia, and upload submissions"),
	ROLE_ATTACHMENT_VIEWER("required to view imagery, video, audio and other complex data in the form; split out to work around Google Earth limitation."),
	ROLE_DATA_VIEWER("required to view submissions and to generate csv and kml files and download them"),
	ROLE_DATA_OWNER("required to upload new xforms, upload modifications to existing xforms, configure external services and data publishing, and to delete xforms or thier data"),
   ROLE_SYNCHRONIZE_TABLES("required to synchronize (download, upload and modify ODK Tables data)"),
   ROLE_SUPER_USER_TABLES("required to administer row-level permissions in ODK Tables"),
   ROLE_ADMINISTER_TABLES("required to administer ODK Tables"),
	ROLE_SITE_ACCESS_ADMIN("required for the permissions-management pages, including the registered users, group access rights, and user membership in groups"),

	GROUP_DATA_COLLECTORS("Data Collector"),
	GROUP_DATA_VIEWERS("Data Viewer"),
   GROUP_FORM_MANAGERS("Form Manager"),
   GROUP_SYNCHRONIZE_TABLES("Synchronize Tables"),
   GROUP_SUPER_USER_TABLES("Tables Super-user"),
   GROUP_ADMINISTER_TABLES("Administer Tables"),
   GROUP_SITE_ADMINS("Site Administrator")
	;

	private String displayText;

	private GrantedAuthorityName() {
	  // GWT
	}

	GrantedAuthorityName(String displayText) {
		this.displayText = displayText;
	}

	public String getDisplayText() {
		return displayText;
	}

   public static final String GROUP_PREFIX = "GROUP_";
	public static final String ROLE_PREFIX = "ROLE_";
	public static final String RUN_AS_PREFIX = "RUN_AS_";

	public static final boolean permissionsCanBeAssigned(String authority) {
		return (authority != null) &&
			!(authority.startsWith(ROLE_PREFIX) || authority.startsWith(RUN_AS_PREFIX));
	}
}
