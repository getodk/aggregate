/*
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
import org.opendatakit.common.security.spring.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.utils.EmailParser;
import org.opendatakit.common.utils.EmailParser.Email;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

/**
 * Simple access configuration screen for initial and secondary
 * management of user permissions.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class AccessConfigurationServlet extends ServletUtilBase {

	/*
	 * Standard servlet fields... 
	 */
	
	private static final long serialVersionUID = -7856387925559237871L;
	
	public static final String ADDR = "access/access-configuration";
	
	private static final String ADDR_APPLIED = "change-applied.html";
	
	public static final String TITLE_INFO = "Configure Site Access";
	
	/*
	 * Form fields
	 */
	
	private static final String FORCE_SIMPLE = "forceSimple";
	
	private static final String SITE_ADMINS = "siteAdmins";
	
	private static final String FORM_ADMINS = "formAdmins";
	
	private static final String SUBMITTERS = "submitters";
	
	private static final String ANONYMOUS_SUBMITTERS = "anonymousSubmitters";
	
	private static final String ANONYMOUS_ATTACHMENT_VIEWERS = "anonymousAttachmentViewers";

	/*
	 * E-mail parsing characters and constants
	 */

	private static final char K_AT = '@';
	private static final String K_MAILTO = "mailto:";
	private static final String K_NEWLINE = "\n";

	/*
	 * Static immutable values for access management configuration...
	 */

	private static final GrantedAuthority siteAuth = new GrantedAuthorityImpl(SITE_ADMINS);
	private static final GrantedAuthority formAuth = new GrantedAuthorityImpl(FORM_ADMINS);
	private static final GrantedAuthority submitterAuth = new GrantedAuthorityImpl(SUBMITTERS);
	private static final GrantedAuthority anonAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_ANONYMOUS.name());

	private static final List<String> siteGrants;
	private static final List<String> formGrants;
	private static final List<String> submitterGrants;

	private static final List<String> anonSubmitterGrants;
	private static final List<String> anonAttachmentViewerGrants;
	
	static {
		List<String> isiteGrants = new ArrayList<String>();
		isiteGrants.add(GrantedAuthorityNames.ROLE_ACCESS_ADMIN.name());
		isiteGrants.add(FORM_ADMINS);
		siteGrants = Collections.unmodifiableList(isiteGrants);
	
		List<String> iformGrants = new ArrayList<String>();
		iformGrants.add(GrantedAuthorityNames.ROLE_FORM_ADMIN.name());
		iformGrants.add(SUBMITTERS);
		formGrants = Collections.unmodifiableList(iformGrants);
	
		List<String> isubmitterGrants = new ArrayList<String>();
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_ANALYST.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_ATTACHMENT_VIEWER.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_DOWNLOAD.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_LIST.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_SERVICES_ADMIN.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_SUBMISSION_UPLOAD.name());
		isubmitterGrants.add(GrantedAuthorityNames.ROLE_USER.name());
		submitterGrants = Collections.unmodifiableList(isubmitterGrants);

		List<String> ianonSubmitterGrants = new ArrayList<String>();
		ianonSubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_DOWNLOAD.name());
		ianonSubmitterGrants.add(GrantedAuthorityNames.ROLE_FORM_LIST.name());
		ianonSubmitterGrants.add(GrantedAuthorityNames.ROLE_SUBMISSION_UPLOAD.name());
		anonSubmitterGrants = Collections.unmodifiableList(ianonSubmitterGrants);

		List<String> ianonAttachmentViewerGrants = new ArrayList<String>();
		ianonAttachmentViewerGrants.add(GrantedAuthorityNames.ROLE_ATTACHMENT_VIEWER.name());
		anonAttachmentViewerGrants = Collections.unmodifiableList(ianonAttachmentViewerGrants);
	}

	/**
	 * Retrieves and constructs the new-line-separated list of e-mails that are 
	 * directly granted the given authority.  Used to construct the populated
	 * value-list of the &lt;textarea&gt; widget.
	 *  
	 * @param auth
	 * @param cc
	 * @return
	 * @throws ODKDatastoreException
	 */
	private String getEmailsOfGrantedAuthority(GrantedAuthority auth, CallingContext cc) throws ODKDatastoreException {
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		TreeSet<String> str = UserGrantedAuthority.getUriUsers(auth, ds, user);
		
		RegisteredUsersTable prototype = RegisteredUsersTable.assertRelation(ds, user);
		TreeSet<String> orderedEmails = new TreeSet<String>();
		for ( String uriUser : str ) {
			try {
				RegisteredUsersTable t = ds.getEntity(prototype, uriUser, user);
				String nickname = t.getNickname();
				if ( nickname == null ) {
					nickname = uriUser.substring(K_MAILTO.length(), uriUser.indexOf(K_AT));
				}
				orderedEmails.add( "\"" + nickname + "\" <" +
								t.getUriUser().substring(K_MAILTO.length()) + ">");
			} catch ( ODKEntityNotFoundException e ) {
				e.printStackTrace();
			}
		}
		
		StringBuilder b = new StringBuilder();
		for ( String email : orderedEmails ) {
			b.append(email);
			b.append(K_NEWLINE);
		}
		return b.toString();
	}
	
	/**
	 * Given a collection of e-mails, ensure that each e-mail is a registered user 
	 * (creating a registered user if one doesn't exist for the e-mail) and assign
	 * those users to the granted authority.  
	 * <p>The collection is assumed to be exhaustive.  If there are other e-mails
	 * already assigned to the granted authority, they will be removed so that 
	 * exactly the passed-in set of users are assigned to the authority, no more, 
	 * no less.</p>
	 * 
	 * @param emails
	 * @param auth
	 * @param cc
	 * @throws ODKDatastoreException
	 */
	private void setEmailsOfGrantedAuthority(Collection<Email> emails, GrantedAuthority auth, CallingContext cc) throws ODKDatastoreException {
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		
		// ensure that the user exists...
		RegisteredUsersTable registeredUserPrototype = RegisteredUsersTable.assertRelation(ds, user);
		for ( Email e : emails ) {
			try {
				RegisteredUsersTable t = ds.getEntity(registeredUserPrototype, e.getUriUser(), user);
				if ( t.getNickname() == null ) {
					t.setNickname(e.getNickname());
					ds.putEntity(t, user);
				} else if ( !t.getNickname().equals(e.getNickname()) ) {
					// user supplied a real nickname.
					// that nickname is different from what is in the datastore
					t.setNickname(e.getNickname());
					ds.putEntity(t, user);
				}
			} catch ( ODKEntityNotFoundException err ) {
				// new user
				RegisteredUsersTable r = ds.createEntityUsingRelation(registeredUserPrototype, user);
				r.setUriUser(e.getUriUser());
				r.setNickname(e.getNickname());
				r.setIsCredentialNonExpired(true);
				r.setIsEnabled(true);
				ds.putEntity(r, user);
			}
		}

		// build the set of uriUsers for this granted authority...
		TreeSet<String> desiredMembers = new TreeSet<String>();
		for ( Email e : emails ) {
			desiredMembers.add(e.getUriUser());
		}
		
		// assert that the authority has exactly this set of uriUsers (no more, no less)
		UserGrantedAuthority.assertGrantedAuthoryMembers(auth, desiredMembers, cc);
	}

	/**
	 * Determine whether or not the configuration is a full or partially constructed
	 * simple configuration.  If it has additional elements, we show the click-through
	 * to custom management screen.  Otherwise, show the wizard screen.
	 * 
	 * @param cc
	 * @return
	 * @throws ODKDatastoreException
	 */
	private boolean isSimpleConfig( CallingContext cc ) throws ODKDatastoreException {
		TreeMap<String, TreeSet<String>> hierarchy = 
			GrantedAuthorityHierarchyTable.getEntireGrantedAuthorityHierarchy(cc.getDatastore(), cc.getCurrentUser());
		
		// check that a subset of the expected set of fields are there...
		for ( Map.Entry<String, TreeSet<String>> e : hierarchy.entrySet() ) {
			if ( e.getKey().equals(SITE_ADMINS) ) {
				for ( String s : e.getValue() ) {
					if ( siteGrants.contains(s) ) continue;
					return false; 
				}
			} else if ( e.getKey().equals(FORM_ADMINS) ) {
				for ( String s : e.getValue() ) {
					if ( formGrants.contains(s) ) continue;
					return false; 
				}
			} else if ( e.getKey().equals(SUBMITTERS) ) {
				for ( String s : e.getValue() ) {
					if ( submitterGrants.contains(s) ) continue;
					return false; 
				}
			} else if ( e.getKey().equals(GrantedAuthorityNames.USER_IS_ANONYMOUS.name())) {
				for ( String s : e.getValue() ) {
					if ( anonSubmitterGrants.contains(s) ||
							anonAttachmentViewerGrants.contains(s) ) continue;
					return false; 
				}
			} else {
				// some other name -- must be a custom set-up...
				return false;
			}
		}
		return true;
	}
	
	private static final class AnonSettings {
		boolean submitter;
		boolean viewer;
	};
	
	private AnonSettings getAnonymousCharacterization(CallingContext cc) throws ODKDatastoreException {
		TreeSet<String> grants = GrantedAuthorityHierarchyTable.getSubordinateGrantedAuthorities(anonAuth, cc);
		AnonSettings a = new AnonSettings();
		a.submitter = false;
		a.viewer = false;
		
		for ( String s : grants ) {
			a.submitter = a.submitter || anonSubmitterGrants.contains(s);
			a.viewer = a.viewer || anonAttachmentViewerGrants.contains(s);
		}
		return a;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		String forceSimple = req.getParameter(FORCE_SIMPLE);

		// don't show the title bar if there is no access management configured.
		// otherwise, show it like on any other page.
		beginBasicHtmlResponse(TITLE_INFO, resp, 
					cc.getUserService().isAccessManagementConfigured(), cc); // header info

		PrintWriter out = resp.getWriter();
		try {
			
			if ( forceSimple == null && !isSimpleConfig(cc) ) {
				Map<String,String> properties = new HashMap<String,String>();
				properties.put(FORCE_SIMPLE, "yes");
				String link = HtmlUtil.createHrefWithProperties( cc.getWebApplicationURL(ADDR),
																	properties, "here");
				
				out.print("<p>It appears that a customized access management configuration" +
						"has been specified.  Click <a href=\"" + 
						cc.getWebApplicationURL(AccessManagementServlet.ADDR) + 
						"\">here</a> to go to the non-wizard-based configuration screens.</p>" +
						"<p>Or, if you would like to reset the access management to the " +
						"simplified model managed by the wizard-based interface, click " + link +
						" to access the wizard-based interface and overwrite your custom " +
						"access management configuration.");
			} else {
				AnonSettings a = getAnonymousCharacterization(cc);
				
				out.print("<p>Use this page to configure who has access to this server." +
						" This page allows you to designate individuals as any of:</p>" +
						"<ol><li>site administrators - people who can configure site access.</li>" +
						"<li>form administrators - people who can add or delete forms and delete " +
						"uploaded submissions, or</li>" +
						"<li>submitters - people who can download and upload forms and upload, " +
						"view, download and publish submissions.</li></ol>" +
						"\n<p>Users are identified by their e-mail addresses.  Individual e-mail " +
						"addresses should be separated by " +
						"whitespace, commas or semicolons; in general, you should be able to " +
						"cut-and-paste the To: line from your " +
						"e-mail program into the boxes below, and things should work fine.</p>" +
						"<p>E-mail addresses can be of either form:</p>" +
						"<ul><li>mitchellsundt@gmail.com</li>" +
						"<li>\"Mitch Sundt\" &lt;mitchellsundt@gmail.com&gt;</li></ul>" +
						"<p>Alternatively, if you simply " +
						"enter usernames, the system will convert them to e-mail addresses by appending <code>@" +
						cc.getUserService().getCurrentRealm().getMailToDomain() +
						"</code> to them.<p>" +
						"\n<hr/>");
				out.print("<form method=\"POST\" action=\"" + cc.getWebApplicationURL(ADDR) + "\">" );
				out.print("<h2>Site Access</h2>");
				out.print("<h4>User Password</h4>");
				out.print("<p>Authenticated submissions from ODK Collect 1.1.6 require the use of passwords that are " +
						"held on this server and that are specific to this server (referred to as <em>Aggregate passwords</em>). " +
						"Site administrators can set or change Aggregate passwords <a href=\"" + 
						cc.getWebApplicationURL(UserManagePasswordsServlet.ADDR) + "\">here</a>.  By default," +
						  " users are not assigned an Aggregate password and must log in using their <code>@" +
						cc.getUserService().getCurrentRealm().getMailToDomain() +
						"</code> account," +
						  " and so will not be able to do authenticated submissions from ODK Collect 1.1.6</p>" +
						  "<p>Administrators can define non-gmail account users (e.g., <code>fred@mydomain.org</code>) but those " +
						  "users can only log in with their Aggregate password when using this site (Aggregate can't" +
						  " automatically authenticate against '<code>mydomain.org</code>'). In this case, administrators must " +
						  "visit the above link to set an Aggregate password for non-gmail account users before they " +
						  "can gain access to the system.</p>");
				out.print("<p>Users, once logged in, can reset their Aggregate passwords by visiting the 'Change Password' page.</p>");
				out.print("<h4>Site Administrators</h4><p>Enter the e-mail addresses of the " +
						"site administrators below</p><textarea name=\"" + SITE_ADMINS + "\" rows=\"10\" cols=\"60\">" +
								getEmailsOfGrantedAuthority(siteAuth, cc) + "</textarea>");
				out.print("<h4>Form Administrators</h4><p>Enter the e-mail addresses of the " +
						"form administrators below</p><textarea name=\"" + FORM_ADMINS + "\" rows=\"10\" cols=\"60\">" +
								getEmailsOfGrantedAuthority(formAuth, cc) + "</textarea>");
				out.print("<h4>Submitters</h4><p>Enter the e-mail addresses of the " +
						"submitters below</p><textarea name=\"" + SUBMITTERS + "\" rows=\"20\" cols=\"60\">" +
								getEmailsOfGrantedAuthority(submitterAuth, cc) + "</textarea>");
				out.print("<br/><br/><input name=\"" + ANONYMOUS_SUBMITTERS + "\" type=\"checkbox\" value=\"yes\"" + 
						(a.submitter ? "checked" : "") + ">Accept submissions from " +
						"unidentified sources (e.g., from ODK Collect 1.1.5 and earlier).</input>" +
						"<p><font color=\"red\">Note:</font> checking this box allows malicious " +
						"individuals to submit inaccurate data to your server.</p>");
				out.print("<p>Prior to ODK Collect 1.1.6, submitting completed form data did not communicate " +
						"the identity of the individual submitting the data to the server.  Unchecking this box" +
						" will prevent ODK Collect 1.1.5 and earlier from submitting data to your server.  If left unchecked, " +
						"you will need to either:</p>" +
						"<ol><li>use ODK Collect 1.1.6 (or greater) configured with user logins enabled or</li>" +
						"<li>use the 'Upload Submissions' web page to manually upload the completed submissions</li></ol>");
				out.print("<h2>Google Earth Balloon Display Compatibility</h2>" +
						"<input name=\"" + ANONYMOUS_ATTACHMENT_VIEWERS + "\" type=\"checkbox\" value=\"yes\"" +
						(a.viewer ? "checked" : "") + ">Allow anonymous " +
						"retrieval of images, audio and video data (needed for GoogleEarth ballon displays)</input>" +
						"<p>Checking this checkbox enables anyone to access the images, audio clips and video clips " +
						"associated with the uploaded form data.  Disclosure risks are somewhat mitigated by needing " +
						"to know the exact URL that identifies this data.  It is unlikely that an outsider could guess " +
						"that URL, but URLs such as these can be accidentally disclosed by your organization and," +
						" once disclosed, by checking this checkbox, your organization will not have any control" +
						" over who can access that data.</p>");
				out.print("<input type=\"submit\" value=\"Submit\">");
				out.print("</form>");
				out.print("<p>Advanced users may wish to use the non-wizard " +
						"configuration pages <a href=\"" + 
						cc.getWebApplicationURL(AccessManagementServlet.ADDR) + 
						"\">here</a> for more precise access control.</p>");
			}
			finishBasicHtmlResponse(resp);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
	        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	                ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		CallingContext cc = ContextFactory.getCallingContext(this, req);
		
		String siteAdmins = req.getParameter(SITE_ADMINS);
		String formAdmins = req.getParameter(FORM_ADMINS);
		String submitters = req.getParameter(SUBMITTERS);
		boolean anonSubmitters = false;
		{
			String str = req.getParameter(ANONYMOUS_SUBMITTERS);
			anonSubmitters = ( str != null) && 
							(str.compareToIgnoreCase("yes") == 0);
		}
		boolean anonAttachmentViewers = false;
		{
			String str = req.getParameter(ANONYMOUS_ATTACHMENT_VIEWERS);
			anonAttachmentViewers = ( str != null) && 
							(str.compareToIgnoreCase("yes") == 0);
		}

		Collection<Email> siteAdminEmails = EmailParser.parseEmails(siteAdmins, cc);
		{
			// make sure that the super-user is in the site admins list...
			Email eSuperUser = EmailParser.getSuperUserEmail(cc);
			boolean found = false;
			for ( Email e : siteAdminEmails ) {
				if ( e.getUriUser().equals(eSuperUser.getUriUser()) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				List<Email> newList = new ArrayList<Email>();
				newList.addAll(siteAdminEmails);
				newList.add(eSuperUser);
				siteAdminEmails = newList;
			}
		}
		Collection<Email> formAdminEmails = EmailParser.parseEmails(formAdmins, cc);
		Collection<Email> submitterEmails = EmailParser.parseEmails(submitters, cc);

		List<String> anonGrants = new ArrayList<String>();
		
		if ( anonSubmitters ) {
			anonGrants.addAll(anonSubmitterGrants);
		}
		
		if ( anonAttachmentViewers ) {
			anonGrants.addAll(anonAttachmentViewerGrants);
		}

		try {
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(siteAuth, siteGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(formAuth, formGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(submitterAuth, submitterGrants, cc);

			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(anonAuth, anonGrants, cc);
			
			TreeSet<String> authorities = GrantedAuthorityHierarchyTable.getAllPermissionsAssignableGrantedAuthorities(cc.getDatastore(), cc.getCurrentUser());
			authorities.remove(siteAuth.getAuthority());
			authorities.remove(formAuth.getAuthority());
			authorities.remove(submitterAuth.getAuthority());
			authorities.remove(anonAuth.getAuthority());
			
			// remove anything else from database...
			List<String> empty = Collections.emptyList();
			for ( String s : authorities ) {
				GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(new GrantedAuthorityImpl(s), empty, cc );
			}
			
			setEmailsOfGrantedAuthority(siteAdminEmails, siteAuth, cc);
			setEmailsOfGrantedAuthority(formAdminEmails, formAuth, cc);
			setEmailsOfGrantedAuthority(submitterEmails, submitterAuth, cc);

		} catch (ODKDatastoreException e) {
			e.printStackTrace();
	        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	                ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
	        return;
		}

		resp.sendRedirect(cc.getWebApplicationURL(ADDR_APPLIED));
	}
}
