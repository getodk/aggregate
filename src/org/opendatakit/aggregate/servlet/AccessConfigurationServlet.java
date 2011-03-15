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

	private static final char K_OPEN_PAREN = '(';
	private static final char K_ESCAPE = '\\';
	private static final char K_CLOSE_PAREN = ')';
	private static final char K_OPEN_ANGLE = '<';
	private static final char K_CLOSE_ANGLE = '>';
	private static final char K_OPEN_SQUARE = '[';
	private static final char K_CLOSE_SQUARE = ']';
	private static final char K_DOT = '.';
	private static final char K_DQ = '\"';
	private static final char K_COLON = ':';
	private static final char K_COMMA = ',';
	private static final char K_SEMI = ';';
	private static final char K_AT = '@';
	private static final String K_SPECIAL_CHARS = "()<>[]:;@\\,.\"";
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
	 * RFC5322 section 3.2.2 production -- except it accepts an epsilon transition.
	 * 
	 * @param emailText
	 * @param idx
	 * @param len
	 * @return
	 */
	private static final int advanceCFWS(String emailText, int idx, int len ) {
		while ( idx < len ) {
			char c = emailText.charAt(idx);
			if ( Character.isWhitespace(c) ) {
				++idx;
				continue;
			} else if ( c == K_OPEN_PAREN ) {
				// comment
				++idx;
				while ( idx < len ) {
					c = emailText.charAt(idx);
					if ( c == K_CLOSE_PAREN ) {
						++idx;
						break;
					}
					if ( c == K_ESCAPE ) {
						++idx;
						++idx;
						continue;
					}
					// normal character...
					++idx;
				}
			} else {
				// not whitespace -- return...
				break;
			}
		}
		return idx;
	}
	
	/**
	 * RFC 5322 production 3.2.3 -- except it accepts epsilon transition and does not consume pre/post FWS
	 * @param emailText
	 * @param idx
	 * @param len
	 * @param dotAtomAllowed
	 * @return
	 */
	private static final int advanceAtomText(String emailText, int idx, int len, boolean dotAtomAllowed) {
		boolean first = true;
		while ( idx < len ) {
			char c = emailText.charAt(idx);
			if ( Character.isWhitespace(c) ) {
				// end of atom.
				break;
			} else if ( K_SPECIAL_CHARS.indexOf(c) != -1) {
				if ( dotAtomAllowed && !first && c == K_DOT ) {
					// dot is allowed if it is embedded in the atom.
					if ( idx+1 >= len ) {
						// this dot is not part of a dotAtom because it is terminal.
						break;
					}
					c = emailText.charAt(idx+1);
					if ( !Character.isWhitespace(c) && K_SPECIAL_CHARS.indexOf(c) == -1 ) {
						// ok -- the character after the dot is not a terminator
						// so advance past the dot and this character
						// and continue consuming characters...
						++idx;
						++idx;
					} else {
						// dot is at end of atom, so consuming it wouldn't produce a 
						// valid dot-atom.  Leave it.
						break;
					}
				} else {
					// simple atom only or special character ending an atom.
					break;
				}
			} else {
				// ordinary character...
				first = false;
				++idx;
			}
		}
		return idx;
	}

	/**
	 * RFC 5322 production 3.2.4 -- except it accepts epsilon transition.
	 * @param emailText
	 * @param idx
	 * @param len
	 * @return
	 */
	private static final int advanceQuotedText(String emailText, int idx, int len) {
		if ( idx >= len ) return idx;
		char c = emailText.charAt(idx);
		if ( c != K_DQ ) {
			return idx;
		}
		if ( idx+1 >= len ) {
			// orphan double quote -- not a valid quoted text.
			return idx;
		}

		++idx;
		while ( idx < len ) {
			c = emailText.charAt(idx);
			if ( c == K_DQ ) {
				++idx;
				return idx;
			} else if ( c == K_ESCAPE ) {
				++idx;
				++idx;
			} else {
				// ordinary character
				++idx;
			}
		}
		throw new IllegalStateException("double-quoted string never ended");
	}
	
	/**
	 * RFC 5322 PHRASE production -- except it accepts epsilon transition.
	 * @param emailText
	 * @param idx
	 * @param len
	 * @return
	 */
	private static final int advancePhrase(String emailText, int idx, int len, boolean allowDotAtom) {
		if ( idx >= len ) return idx;
		char c = emailText.charAt(idx);
		if ( c == K_DQ ) {
			return advanceQuotedText(emailText, idx, len);
		} else {
			return advanceAtomText(emailText, idx, len, allowDotAtom);
		}
	}
	
	/**
	 * Simple return value tuple.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	private static class PositionString {
		int idxEnd;
		String cleanString;
		
		PositionString(int idx) {
			idxEnd = idx;
			cleanString = null;
		}
	}

	/**
	 * Return a normalized domain string and the index of the next parse character.
	 * 
	 * @param emailText
	 * @param idx
	 * @param len
	 * @return
	 */
	private static final PositionString advanceDomain(String emailText, int idx, int len) {
		char c = emailText.charAt(idx);
		if ( c == K_OPEN_SQUARE ) {
			++idx;
			idx = advanceCFWS(emailText, idx, len);
			int idxStart = idx;
			int idxEnd = emailText.indexOf(K_CLOSE_SQUARE, idxStart);
			if ( idxEnd == -1 ) {
				throw new IllegalStateException("Expecting close square bracket");
			}
			int idxEscape = emailText.indexOf(K_ESCAPE, idxStart);
			if ( idxEscape != -1 && idxEscape < idxEnd ) {
				throw new IllegalStateException("escape sequence appears in domain expression");
			}
			int idxDone = idxStart;
			while ( idxDone < len ) {
				c = emailText.charAt(idxDone);
				if ( Character.isWhitespace(c) || c == K_OPEN_PAREN || c == K_CLOSE_SQUARE ) {
					break;
				}
				++idxDone;
			}
			idx = advanceCFWS(emailText, idxDone, len);
			if ( idx != idxEnd ) {
				throw new IllegalStateException("more than just a domain string surround by whitespace");
			}
			PositionString ps = new PositionString(idxEnd+1);
			ps.cleanString = K_OPEN_SQUARE + emailText.substring(idxStart, idxDone) + K_CLOSE_SQUARE;
			return ps;
		} else {
			int idxEnd = advancePhrase(emailText, idx, len, true);
			if ( idxEnd == idx ) {
				throw new IllegalStateException("Expected a domain name");
			}
			PositionString ps = new PositionString(idxEnd);
			ps.cleanString = emailText.substring(idx, idxEnd).toLowerCase();
			return ps;
		}
	}
	
	/**
	 * Advance over an e-mail address specification (RFC 5322)
	 * @param emailText
	 * @param idx position after the email 'localpart' (had better be '@')
	 * @param len
	 * @param idxStart  starting position of email 'localpart' (username)
	 * @param expectCloseAngle
	 * @return return e-mail address string and end position.
	 */
	private static final PositionString advanceAddrSpec(String emailText, int idx, int len, int idxStart, boolean expectCloseAngle) {
		// this had better get a dot-phrase
		int idxEnd = idx;
		if ( idxStart == idxEnd ) {
			throw new IllegalStateException("Missing local part (username) in e-mail address");
		}
		if ( idxEnd >= len ) {
			throw new IllegalStateException("Unexpected end to angle production");
		}
		char c = emailText.charAt(idx);
		if ( c != K_AT ) {
			throw new IllegalStateException("Expected '@' sign in e-mail address");
		}
		++idx;
		int idxStartDomain = idx;
		PositionString domain = advanceDomain(emailText, idx, len);
		if ( idxStartDomain == domain.idxEnd ) {
			throw new IllegalStateException("Missing domain in e-mail address");
		}
		idx = domain.idxEnd;
		idx = advanceCFWS(emailText, idx, len);
		if ( expectCloseAngle ) {
			c = emailText.charAt(idx);
			if ( c != K_CLOSE_ANGLE) {
				throw new IllegalStateException("Expecting close angle bracket");
			}
			++idx;
		}
		PositionString ps = new PositionString(idx);
		ps.cleanString = emailText.substring(idxStart, idxEnd) + K_AT + domain.cleanString; 
		return ps;
	}
	
	/**
	 * Parses the &lt;user@domain.org&gt; portion of an email address per RFC 5322.
	 *   
	 * @param emailText
	 * @param idx
	 * @param len
	 * @return the email address within the angle brackets.
	 */
	private static final PositionString advanceAngleAddr(String emailText, int idx, int len) {
		char c = emailText.charAt(idx);
		if ( c != K_OPEN_ANGLE ) {
			throw new IllegalStateException("Expected to be at angle production");
		}
		// e.g., embedded spaces '< foo@bar >'
		++idx;
		idx = advanceCFWS(emailText, idx, len);
		
		boolean allowRouting = true;
		boolean expectColon = false;
		for (;idx < len;) {
			c = emailText.charAt(idx);
			if ( allowRouting && c == K_AT ) {
				expectColon = true;
				++idx;
				idx = advanceCFWS(emailText, idx, len);
				int idxDomainStart = idx;
				PositionString pd = advanceDomain(emailText, idx, len);
				idx = pd.idxEnd;
				if ( idx == idxDomainStart ) {
					throw new IllegalStateException("Unexpected missing domain in routing list");
				}
				idx = advanceCFWS(emailText, idx, len);
				if ( idx >= len ) {
					throw new IllegalStateException("Unexpected end to angle production");
				}
				c = emailText.charAt(idx);
				if ( c == K_COMMA ) {
					++idx;
					idx = advanceCFWS(emailText, idx, len);
					continue;
				} else if ( c == K_COLON ) {
					++idx;
					idx = advanceCFWS(emailText, idx, len);
					allowRouting = false;
					expectColon = false;
					continue;
				} else {
					throw new IllegalStateException("Unexpected character in routing list");
				}
			} else if ( expectColon && c == K_COLON ) {
				++idx;
				idx = advanceCFWS(emailText, idx, len);
				allowRouting = false;
				expectColon = false;
				continue;
			} else {
				// this had better get a dot-phrase
				int idxStart = idx;
				int idxEnd = advancePhrase(emailText, idx, len, true);
				return advanceAddrSpec(emailText, idxEnd, len, idxStart, true);
			}
		}
		throw new IllegalStateException("Unexpected end to angle production");
	}

	/**
	 * Return value tuple.  
	 * Returns the nickname in an e-mail address
	 * and the email address itself.
	 *  
	 * @author mitchellsundt@gmail.com
	 *
	 */
	private static class Email {
		String nickname;
		String uriUser;
	};
	
	/**
	 * Trims the surrounding double qoutes from the email nickname and 
	 * maintains a map of e-mail addresses, resolving duplicates.
	 * 
	 * @param eMails
	 * @param email
	 */
	private static final void insertEmail( Map<String, Email> eMails, Email email ) {
		if ( email.nickname != null && email.nickname.charAt(0) == K_DQ ) {
			email.nickname = email.nickname.substring(1, email.nickname.length()-1);
		}
		
		Email e = eMails.get(email.uriUser);
		if ( e == null ) {
			eMails.put(email.uriUser, email);
		} else {
			if ( e.nickname == null || 
					(email.nickname != null && e.uriUser.startsWith(K_MAILTO + e.nickname)) ) {
				// replace
				eMails.put(email.uriUser, email);
			}
		}
	}
	
	/**
	 * Parses a string of e-mails or user names that are space, comma or semi-colon separated.
	 * 
	 * @param emailText
	 * @param cc
	 * @return collection of the found e-mails.
	 */
	private static final Collection<Email> parseEmails( String emailText, CallingContext cc ) {
		Map<String,Email> eMails = new HashMap<String,Email>();
		int len = emailText.length();
		int idx = 0;
		while ( idx < len ) {
			idx = advanceCFWS(emailText, idx, len);
			if ( idx >= len ) break;
			char c = emailText.charAt(idx);
			if ( c == K_COMMA ) {
				++idx;
			} else if ( c == K_SEMI ) {
				++idx;
			} else if ( c == K_OPEN_ANGLE ) {
				PositionString ps = advanceAngleAddr( emailText, idx, len);
				idx = ps.idxEnd;
				Email email = new Email();
				email.nickname = ps.cleanString.substring(0,ps.cleanString.indexOf(K_AT));
				email.uriUser = K_MAILTO + ps.cleanString;
				insertEmail(eMails, email);
			} else if ( c == K_COLON ) {
				// must be a group list with no name...
				++idx;
			} else {
				int idxStart = idx;
				int idxEnd = advancePhrase(emailText, idx, len, true);
				if ( idxEnd == idxStart ) {
					throw new IllegalStateException("Expected e-mail address");
				}
				if ( idxEnd >= len || emailText.charAt(idxEnd) != K_AT ) {
					idx = advanceCFWS(emailText, idxEnd, len );
					c = K_COMMA; // fake terminator...
					if ( idx < len ) {
						c = emailText.charAt(idx);
					}
					if ( c == K_COLON ) {
						// this is a group -- ignore it...
						continue;
					} else if ( c == K_OPEN_ANGLE ) {
						PositionString ps = advanceAngleAddr( emailText, idx, len);
						idx = ps.idxEnd;
						Email email = new Email();
						email.nickname = emailText.substring(idxStart, idxEnd);
						email.uriUser = K_MAILTO + ps.cleanString;
						insertEmail(eMails, email);
					} else {
						// must just be a naked name -- 
						Email email = new Email();
						email.nickname = emailText.substring(idxStart, idxEnd);
						email.uriUser = K_MAILTO + emailText.substring(idxStart, idxEnd) + K_AT + 
									cc.getUserService().getCurrentRealm().getMailToDomain();
						insertEmail(eMails, email);
					}
				}
				else {
					PositionString ps = advanceAddrSpec(emailText, idxEnd, len, idxStart, false);
					
					Email email = new Email();
					email.nickname = ps.cleanString.substring(0,ps.cleanString.indexOf(K_AT));
					email.uriUser = K_MAILTO + ps.cleanString;
					insertEmail(eMails, email);
					idx = ps.idxEnd;
				}
			}
		}

		return eMails.values();
	}

	/**
	 * Construct and return the Email object for the superUser.
	 * 
	 * @param cc
	 * @return
	 */
	private Email getSuperUserEmail( CallingContext cc ) {
		Email e = new Email();
		e.uriUser = cc.getUserService().getSuperUserEmail();
		e.nickname = e.uriUser.substring(K_MAILTO.length(), e.uriUser.indexOf(K_AT));
		return e;
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
				RegisteredUsersTable t = ds.getEntity(registeredUserPrototype, e.uriUser, user);
				if ( t.getNickname() == null ) {
					t.setNickname(e.nickname);
					ds.putEntity(t, user);
				} else if ( !t.getNickname().equals(e.nickname) ) {
					// user supplied a real nickname.
					// that nickname is different from what is in the datastore
					t.setNickname(e.nickname);
					ds.putEntity(t, user);
				}
			} catch ( ODKEntityNotFoundException err ) {
				// new user
				RegisteredUsersTable r = ds.createEntityUsingRelation(registeredUserPrototype, user);
				r.setUriUser(e.uriUser);
				r.setNickname(e.nickname);
				r.setIsCredentialNonExpired(true);
				r.setIsEnabled(true);
				ds.putEntity(r, user);
			}
		}

		// build the set of uriUsers for this granted authority...
		TreeSet<String> desiredMembers = new TreeSet<String>();
		for ( Email e : emails ) {
			desiredMembers.add(e.uriUser);
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
				out.print("<p>Authenticated submissions from ODK Collect 1.1.6 require locally-held user passwords. " +
						"Site administrators can set or change locally-held user passwords <a href=\"" + 
						cc.getWebApplicationURL(UserManagePasswordsServlet.ADDR) + "\">here</a>.  By default," +
						  " users are not assigned a locally-held password, and so will not be able to do " +
						  "authenticated submissions from ODK Collect 1.1.6</p>" +
						  "<p>Administrators can define non-gmail account users (e.g., fred@mydomain.org) but those " +
						  "users will need to log in with their locally-held password to use the site (Aggregate can't" +
						  " automatically authenticate against 'mydomain.org'). Thus, administrators must visit the above link " +
						  "to set a locally-held password for non-gmail account users before they can gain access to the system.</p>");
				out.print("<p>Users, once logged in, can reset their passwords by visiting the 'Change Password' page.</p>");
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

		Collection<Email> siteAdminEmails = parseEmails(siteAdmins, cc);
		{
			// make sure that the super-user is in the site admins list...
			Email eSuperUser = getSuperUserEmail(cc);
			boolean found = false;
			for ( Email e : siteAdminEmails ) {
				if ( e.uriUser.equals(eSuperUser.uriUser) ) {
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
		Collection<Email> formAdminEmails = parseEmails(formAdmins, cc);
		Collection<Email> submitterEmails = parseEmails(submitters, cc);

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
