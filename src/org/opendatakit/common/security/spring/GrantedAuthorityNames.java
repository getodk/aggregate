package org.opendatakit.common.security.spring;

public enum GrantedAuthorityNames {

	MODE_WEBSITE,
	MODE_DEVICE,
	
	AUTH_DIGEST,
	AUTH_BASIC,
	AUTH_LDAP,
	AUTH_OPENID,
	
	USER_IS_REGISTERED,    // user is in the registered users table
	USER_IS_DAEMON,        // user is the daemon user
	USER_IS_ANONYMOUS,     // user is not authenticated
	USER_IS_AUTHENTICATED, // user is authenticated, maybe not registered
	
	ROLE_FORM_LIST, // protects fetch of xforms list to device
	ROLE_FORM_DOWNLOAD, // protects fetch of xform definition to device
	ROLE_SUBMISSION_UPLOAD, // protects every submission of form into Aggregate
	ROLE_USER, 		// protects forms page, human-readable xform xml listing
	ROLE_ANALYST,  	// protects generation of csv, kml, and viewing of submissions
	ROLE_SERVICES_ADMIN, // protects external services configuration and change
	ROLE_XFORMS_ADMIN // protects uploading of new xform, changes, and deletions
	
}
