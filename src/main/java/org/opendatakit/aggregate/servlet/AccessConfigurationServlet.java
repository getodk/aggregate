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
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.EmailParser.Email;
import org.opendatakit.common.security.common.GrantedAuthorityNames;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
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

	private static final GrantedAuthority siteAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_SITE_ADMINS);
	private static final GrantedAuthority formAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_FORM_ADMINS);
	private static final GrantedAuthority submitterAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.GROUP_SUBMITTERS);
	private static final GrantedAuthority anonAuth = new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_ANONYMOUS.name());


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
	private String getUsernamesOrEmailsOfGrantedAuthority(GrantedAuthority auth, CallingContext cc) throws ODKDatastoreException {
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		Set<String> str = UserGrantedAuthority.getUriUsers(auth, ds, user);
		
		TreeSet<String> orderedEmails = new TreeSet<String>();
		for ( String uriUser : str ) {
			try {
				if ( uriUser.equals(cc.getUserService().getSuperUserEmail())) {
					String username = uriUser.substring(K_MAILTO.length());
					orderedEmails.add(username);
				} else {
					RegisteredUsersTable t = RegisteredUsersTable.getUserByUri(uriUser, ds, user);
					String username = t.getUsername();
					if ( t.getEmail() != null ) {
						String email = t.getEmail();
						String emailUser = email.substring(K_MAILTO.length(), email.indexOf(K_AT));
						if ( emailUser.equals(username) ) {
							// OK email user matches username -- let's use the e-mail...
							String nickname = t.getNickname();
							if ( nickname == null ) {
								nickname = emailUser;
							}
							username = "\"" + nickname + "\" <" +
								t.getEmail().substring(K_MAILTO.length()) + ">";
						}
					}
					if ( t.getIsRemoved() ) {
						Logger.getLogger(AccessConfigurationServlet.class.getName()).warning(
								"Unexpected presence of a removed user in the groups list " + uriUser);
					} else {
						orderedEmails.add(username);
					}
				}
			} catch ( ODKEntityNotFoundException e ) {
				e.printStackTrace();
				Logger.getLogger(AccessConfigurationServlet.class.getName()).warning(
						"Unable to locate registered user record for " + uriUser);
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
		// ensure that the user exists...
		for ( Email e : emails ) {
			// NOTE: This also sets the Uri of the Email object.
			RegisteredUsersTable.assertActiveUserByUsername(e, cc);
		}

		// build the set of uriUsers for this granted authority...
		TreeSet<String> desiredMembers = new TreeSet<String>();
		for ( Email e : emails ) {
			desiredMembers.add(e.getUri());
		}
		
		// assert that the authority has exactly this set of uriUsers (no more, no less)
		UserGrantedAuthority.assertGrantedAuthorityMembers(auth, desiredMembers, cc);
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
			a.submitter = a.submitter || SecurityServiceUtil.anonSubmitterGrants.contains(s);
			a.viewer = a.viewer || SecurityServiceUtil.anonAttachmentViewerGrants.contains(s);
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
			
			if ( forceSimple == null && !SecurityServiceUtil.isSimpleConfig(cc) ) {
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
						"\n<p>Users are identified by their ODK Aggregate usernames. " +
						"For ease of data entry, if you cut-and-paste e-mail addresses into " +
						"the fields below, the usernames of the email addresses will be taken to be " +
						"the ODK Aggregate username for that individual.  Individual e-mail " +
						"addresses should be separated by " +
						"whitespace, commas or semicolons; in general, you should be able to " +
						"cut-and-paste the To: line from your " +
						"e-mail program into the boxes below, and things should work fine.</p>" +
						"<p>E-mail addresses can be of either form:</p>" +
						"<ul><li>mitchellsundt@gmail.com</li>" +
						"<li>\"Mitch Sundt\" &lt;mitchellsundt@gmail.com&gt;</li></ul>" +
						"<p>Alternatively, if you simply " +
						"enter usernames separated by " +
						"whitespace, commas or semicolons, the system will create those usernames " +
						"without any associated e-mail address.</p>" +
						"<p>If you enter duplicate usernames or two or more e-mails that collapse to " +
						"the same username, only one ODK Aggregate username will be created.</p>" +
						"<hr/>");
				out.print("<form method=\"POST\" action=\"" + cc.getWebApplicationURL(ADDR) + "\">" );
				out.print("<h2>Site Access</h2>");
				out.print("<h4>User Password</h4>");
				out.print("<p>Authenticated submissions from ODK Collect 1.1.6 (or higher) require the use " +
						"of passwords that are held on this server and that are specific to this server " +
						"\n(referred to as <em>Aggregate passwords</em>). " +
						"Site administrators can set or change Aggregate passwords from the " +
						"<em>Manage/Permissions/Manage User Passwords</em> tab. By default, " +
						"users are not assigned an Aggregate password " +
						"and so will not be able to do authenticated submissions until a site administrator " +
						"assigns them a password or until they log in and set their own password.</p>" +
						"<p>If usernames are associated with gmail accounts, users with those accounts " +
						"can log into the website using OpenID.  Otherwise, users can only " +
						"log in with their Aggregate password. For those users, a site administrator must " +
						"visit the above link to set an Aggregate password before they can gain access " +
						"to the system.</p>");
				out.print("<p>Users, once logged in, can reset their Aggregate passwords by visiting the 'Change Password' page.</p>");
				out.print("<h4>Site Administrators</h4><p>Enter the usernames or e-mail addresses of the " +
						"site administrators below</p><textarea name=\"" + GrantedAuthorityNames.GROUP_SITE_ADMINS + "\" rows=\"10\" cols=\"60\">" +
								getUsernamesOrEmailsOfGrantedAuthority(siteAuth, cc) + "</textarea>");
				out.print("<h4>Form Administrators</h4><p>Enter the usernames or e-mail addresses of the " +
						"form administrators below</p><textarea name=\"" + GrantedAuthorityNames.GROUP_FORM_ADMINS + "\" rows=\"10\" cols=\"60\">" +
								getUsernamesOrEmailsOfGrantedAuthority(formAuth, cc) + "</textarea>");
				out.print("<h4>Submitters</h4><p>Enter the usernames or e-mail addresses of the " +
						"submitters below</p><textarea name=\"" + GrantedAuthorityNames.GROUP_SUBMITTERS + "\" rows=\"20\" cols=\"60\">" +
								getUsernamesOrEmailsOfGrantedAuthority(submitterAuth, cc) + "</textarea>");
				out.print("<br/><br/><input name=\"" + ANONYMOUS_SUBMITTERS + "\" type=\"checkbox\" value=\"yes\"" + 
						(a.submitter ? "checked" : "") + ">Accept submissions from " +
						"unidentified sources (e.g., from ODK Collect 1.1.5 and earlier).</input>" +
						"<p><font color=\"red\">Note:</font> checking this box allows malicious " +
						"individuals to submit inaccurate data to your server.</p>");
				out.print("<p>Prior to ODK Collect 1.1.6, submitting completed form data did not communicate " +
						"the identity of the individual submitting the data to the server.  Unchecking this box" +
						" will prevent ODK Collect 1.1.5 and earlier from submitting data to your server.  If left unchecked, " +
						"you will need to either:</p>" +
						"<ol><li>use ODK Collect 1.1.6 (or higher) configured with user logins enabled or</li>" +
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
		
		String siteAdmins = req.getParameter(GrantedAuthorityNames.GROUP_SITE_ADMINS);
		String formAdmins = req.getParameter(GrantedAuthorityNames.GROUP_FORM_ADMINS);
		String submitters = req.getParameter(GrantedAuthorityNames.GROUP_SUBMITTERS);
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

		Collection<Email> siteAdminEmails = EmailParser.parseEmails(siteAdmins);
		{
			// make sure that the super-user is in the site admins list...
			Email eSuperUser = SecurityServiceUtil.getSuperUserEmail(cc);
			boolean found = false;
			for ( Email e : siteAdminEmails ) {
				if ( e.getEmail() != null && 
					 e.getEmail().equals(eSuperUser.getEmail()) ) {
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
		Collection<Email> formAdminEmails = EmailParser.parseEmails(formAdmins);
		Collection<Email> submitterEmails = EmailParser.parseEmails(submitters);

		List<String> anonGrants = new ArrayList<String>();
		
		if ( anonSubmitters ) {
			anonGrants.addAll(SecurityServiceUtil.anonSubmitterGrants);
		}
		
		if ( anonAttachmentViewers ) {
			anonGrants.addAll(SecurityServiceUtil.anonAttachmentViewerGrants);
		}

		try {
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(siteAuth, SecurityServiceUtil.siteGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(formAuth, SecurityServiceUtil.formGrants, cc);
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy(submitterAuth, SecurityServiceUtil.submitterGrants, cc);

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
