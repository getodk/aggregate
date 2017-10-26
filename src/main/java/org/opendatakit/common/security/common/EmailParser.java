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

package org.opendatakit.common.security.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Routines shared between the JavaScript GWT code and the server to parse
 * username and Email lists for easy set-up of the registered users of the
 * system.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public final class EmailParser {

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
  public static final char K_AT = '@';
  private static final String K_WHITESPACE_CHARS = " \t\n\r";
  private static final String K_SPECIAL_CHARS = "()<>[]:;@\\,.\"";
  public static final String K_MAILTO = "mailto:";

  private static final String K_INVALID_EMAIL_CHARACTERS = " \t\n\r\",;()<>?/{}'[]";

  public static boolean hasInvalidEmailCharacters(String email) {
    for (int i = 0; i < K_INVALID_EMAIL_CHARACTERS.length(); ++i) {
      char ch = K_INVALID_EMAIL_CHARACTERS.charAt(i);
      if (email.indexOf(ch) != -1) {
        return true;
      }
    }
    return false;
  }

  /**
   * Character.isWhitespace is not supported in GWT because GWT is not fully
   * UNICODE-compliant.
   *
   * @param c
   * @return true if c is a standard whitespace character
   */
  private static final boolean isWhitespace(char c) {
    return (K_WHITESPACE_CHARS.indexOf(c) != -1);
  }

  /**
   * RFC5322 section 3.2.2 production -- except it accepts an epsilon
   * transition.
   *
   * @param emailText
   * @param idx
   * @param len
   * @return
   */
  private static final int advanceCFWS(String emailText, int idx, int len) {
    while (idx < len) {
      char c = emailText.charAt(idx);
      if (isWhitespace(c)) {
        ++idx;
        continue;
      } else if (c == K_OPEN_PAREN) {
        // comment
        ++idx;
        while (idx < len) {
          c = emailText.charAt(idx);
          if (c == K_CLOSE_PAREN) {
            ++idx;
            break;
          }
          if (c == K_ESCAPE) {
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
   * RFC 5322 production 3.2.3 -- except it accepts epsilon transition and does
   * not consume pre/post FWS
   *
   * @param emailText
   * @param idx
   * @param len
   * @param dotAtomAllowed
   * @return
   */
  private static final int advanceAtomText(String emailText, int idx, int len,
      boolean dotAtomAllowed) {
    boolean first = true;
    while (idx < len) {
      char c = emailText.charAt(idx);
      if (isWhitespace(c)) {
        // end of atom.
        break;
      } else if (K_SPECIAL_CHARS.indexOf(c) != -1) {
        if (dotAtomAllowed && !first && c == K_DOT) {
          // dot is allowed if it is embedded in the atom.
          if (idx + 1 >= len) {
            // this dot is not part of a dotAtom because it is terminal.
            break;
          }
          c = emailText.charAt(idx + 1);
          if (!isWhitespace(c) && K_SPECIAL_CHARS.indexOf(c) == -1) {
            // ok -- the character after the dot is not a terminator
            // so advance past the dot and this character
            // and continue consuming characters...
            ++idx;
            ++idx;
          } else {
            // dot is at end of atom, so consuming it wouldn't produce a
            // valid dot-atom. Leave it.
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
   *
   * @param emailText
   * @param idx
   * @param len
   * @return
   */
  private static final int advanceQuotedText(String emailText, int idx, int len) {
    if (idx >= len)
      return idx;
    char c = emailText.charAt(idx);
    if (c != K_DQ) {
      return idx;
    }
    if (idx + 1 >= len) {
      // orphan double quote -- not a valid quoted text.
      return idx;
    }

    ++idx;
    while (idx < len) {
      c = emailText.charAt(idx);
      if (c == K_DQ) {
        ++idx;
        return idx;
      } else if (c == K_ESCAPE) {
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
   *
   * @param emailText
   * @param idx
   * @param len
   * @return
   */
  private static final int advancePhrase(String emailText, int idx, int len, boolean allowDotAtom) {
    if (idx >= len)
      return idx;
    char c = emailText.charAt(idx);
    if (c == K_DQ) {
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
   * Return a normalized domain string and the index of the next parse
   * character.
   *
   * @param emailText
   * @param idx
   * @param len
   * @return
   */
  private static final PositionString advanceDomain(String emailText, int idx, int len) {
    char c = emailText.charAt(idx);
    if (c == K_OPEN_SQUARE) {
      ++idx;
      idx = advanceCFWS(emailText, idx, len);
      int idxStart = idx;
      int idxEnd = emailText.indexOf(K_CLOSE_SQUARE, idxStart);
      if (idxEnd == -1) {
        throw new IllegalStateException("Expecting close square bracket");
      }
      int idxEscape = emailText.indexOf(K_ESCAPE, idxStart);
      if (idxEscape != -1 && idxEscape < idxEnd) {
        throw new IllegalStateException("escape sequence appears in domain expression");
      }
      int idxDone = idxStart;
      while (idxDone < len) {
        c = emailText.charAt(idxDone);
        if (isWhitespace(c) || c == K_OPEN_PAREN || c == K_CLOSE_SQUARE) {
          break;
        }
        ++idxDone;
      }
      idx = advanceCFWS(emailText, idxDone, len);
      if (idx != idxEnd) {
        throw new IllegalStateException("more than just a domain string surround by whitespace");
      }
      PositionString ps = new PositionString(idxEnd + 1);
      ps.cleanString = K_OPEN_SQUARE + emailText.substring(idxStart, idxDone) + K_CLOSE_SQUARE;
      return ps;
    } else {
      int idxEnd = advancePhrase(emailText, idx, len, true);
      if (idxEnd == idx) {
        throw new IllegalStateException("Expected a domain name");
      }
      PositionString ps = new PositionString(idxEnd);
      ps.cleanString = emailText.substring(idx, idxEnd).toLowerCase();
      return ps;
    }
  }

  /**
   * Advance over an e-mail address specification (RFC 5322)
   *
   * @param emailText
   * @param idx
   *          position after the email 'localpart' (had better be '@')
   * @param len
   * @param idxStart
   *          starting position of email 'localpart' (username)
   * @param expectCloseAngle
   * @return return e-mail address string and end position.
   */
  private static final PositionString advanceAddrSpec(String emailText, int idx, int len,
      int idxStart, boolean expectCloseAngle) {
    // this had better get a dot-phrase
    int idxEnd = idx;
    if (idxStart == idxEnd) {
      throw new IllegalStateException("Missing local part (username) in e-mail address");
    }
    if (idxEnd >= len) {
      throw new IllegalStateException("Unexpected end to angle production");
    }
    char c = emailText.charAt(idx);
    if (c != K_AT) {
      throw new IllegalStateException("Expected '@' sign in e-mail address");
    }
    ++idx;
    int idxStartDomain = idx;
    PositionString domain = advanceDomain(emailText, idx, len);
    if (idxStartDomain == domain.idxEnd) {
      throw new IllegalStateException("Missing domain in e-mail address");
    }
    idx = domain.idxEnd;
    idx = advanceCFWS(emailText, idx, len);
    if (expectCloseAngle) {
      c = emailText.charAt(idx);
      if (c != K_CLOSE_ANGLE) {
        throw new IllegalStateException("Expecting close angle bracket");
      }
      ++idx;
    }
    PositionString ps = new PositionString(idx);
    ps.cleanString = emailText.substring(idxStart, idxEnd) + K_AT + domain.cleanString;
    return ps;
  }

  /**
   * Parses the &lt;user@domain.org&gt; portion of an email address per RFC
   * 5322.
   *
   * @param emailText
   * @param idx
   * @param len
   * @return the email address within the angle brackets.
   */
  private static final PositionString advanceAngleAddr(String emailText, int idx, int len) {
    char c = emailText.charAt(idx);
    if (c != K_OPEN_ANGLE) {
      throw new IllegalStateException("Expected to be at angle production");
    }
    // e.g., embedded spaces '< foo@bar >'
    ++idx;
    idx = advanceCFWS(emailText, idx, len);

    boolean allowRouting = true;
    boolean expectColon = false;
    for (; idx < len;) {
      c = emailText.charAt(idx);
      if (allowRouting && c == K_AT) {
        expectColon = true;
        ++idx;
        idx = advanceCFWS(emailText, idx, len);
        int idxDomainStart = idx;
        PositionString pd = advanceDomain(emailText, idx, len);
        idx = pd.idxEnd;
        if (idx == idxDomainStart) {
          throw new IllegalStateException("Unexpected missing domain in routing list");
        }
        idx = advanceCFWS(emailText, idx, len);
        if (idx >= len) {
          throw new IllegalStateException("Unexpected end to angle production");
        }
        c = emailText.charAt(idx);
        if (c == K_COMMA) {
          ++idx;
          idx = advanceCFWS(emailText, idx, len);
          continue;
        } else if (c == K_COLON) {
          ++idx;
          idx = advanceCFWS(emailText, idx, len);
          allowRouting = false;
          expectColon = false;
          continue;
        } else {
          throw new IllegalStateException("Unexpected character in routing list");
        }
      } else if (expectColon && c == K_COLON) {
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
   * Return value tuple. Returns the nickname in an e-mail address and the email
   * address itself.
   *
   * @author mitchellsundt@gmail.com
   *
   */
  public static class Email {
    public static enum Form {
      EMAIL, USERNAME
    };

    final Form type;
    final String username;
    final String email;
    String fullname;
    String uri;

    public Email(String name) {
      this.type = Form.USERNAME;
      fullname = null;
      username = name;
      email = null;
    }

    public Email(String fullname, String email) {
      this.type = Form.EMAIL;
      this.fullname = fullname;
      this.username = null;
      this.email = email;
    }

    public Form getType() {
      return type;
    }
    
    public String getUri() {
      return uri;
    }

    public void setUri(String value) {
      uri = value;
    }

    public String getFullName() {
      return fullname;
    }

    public void setFullName(String value) {
      fullname = value;
    }

    public String getUsername() {
      return username;
    }

    public String getEmail() {
      return email;
    }
  };

  /**
   * Trims the surrounding double qoutes from the email nickname and maintains a
   * map of e-mail addresses, resolving duplicates.
   *
   * @param eMails
   * @param email
   */
  private static final void insertEmail(Map<String, Email> eMails, Email email) {
    if (email.fullname != null && email.fullname.charAt(0) == K_DQ) {
      email.fullname = email.fullname.substring(1, email.fullname.length() - 1);
    }

    Email e;
    if (email.username != null) {
      e = eMails.get(email.username);
      if (e == null) {
        eMails.put(email.username, email);
        return;
      }
    } else {
      e = eMails.get(email.email);
      if (e == null) {
        eMails.put(email.email, email);
        return;
      }
    }

    // already in map...
    if (e.fullname == null && email.fullname != null) {
      // had no nickname; update with non-null nickname
      e.setFullName(email.fullname);
    }

    if (e.fullname != null && e.email != null && email.fullname != null
        && e.email.startsWith(K_MAILTO + e.fullname)) {
      // had e-mail user as nickname; update with newer nickname
      e.setFullName(email.fullname);
    }
  }

  public static final String parseEmail(String emailText) {
    try {
      Collection<Email> emails = parseEmails(emailText);
      if ( emails.size() != 1 ) {
        return null;
      }
      for ( Email e : emails ) {
        if ( e.type != Email.Form.EMAIL ) {
          return null;
        }
        return e.email;
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Parses a string of e-mails or user names that are space, comma or
   * semi-colon separated.
   *
   * @param emailText
   * @return collection of the found e-mails.
   */
  public static final Collection<Email> parseEmails(String emailText) {
    Map<String, Email> eMails = new HashMap<String, Email>();
    int len = emailText.length();
    int idx = 0;
    while (idx < len) {
      idx = advanceCFWS(emailText, idx, len);
      if (idx >= len)
        break;
      char c = emailText.charAt(idx);
      if (c == K_COMMA) {
        ++idx;
      } else if (c == K_SEMI) {
        ++idx;
      } else if (c == K_OPEN_ANGLE) {
        PositionString ps = advanceAngleAddr(emailText, idx, len);
        idx = ps.idxEnd;
        Email email = new Email(ps.cleanString.substring(0, ps.cleanString.indexOf(K_AT)), K_MAILTO
            + ps.cleanString);
        insertEmail(eMails, email);
      } else if (c == K_COLON) {
        // must be a group list with no name...
        ++idx;
      } else {
        int idxStart = idx;
        int idxEnd = advancePhrase(emailText, idx, len, true);
        if (idxEnd == idxStart) {
          throw new IllegalStateException("Expected e-mail address");
        }
        if (idxEnd >= len || emailText.charAt(idxEnd) != K_AT) {
          idx = advanceCFWS(emailText, idxEnd, len);
          c = K_COMMA; // fake terminator...
          if (idx < len) {
            c = emailText.charAt(idx);
          }
          if (c == K_COLON) {
            // this is a group -- ignore it...
            continue;
          } else if (c == K_OPEN_ANGLE) {
            PositionString ps = advanceAngleAddr(emailText, idx, len);
            idx = ps.idxEnd;
            Email email = new Email(emailText.substring(idxStart, idxEnd), K_MAILTO
                + ps.cleanString);
            insertEmail(eMails, email);
          } else {
            // must just be a naked name --
            Email email = new Email(emailText.substring(idxStart, idxEnd));
            insertEmail(eMails, email);
          }
        } else {
          PositionString ps = advanceAddrSpec(emailText, idxEnd, len, idxStart, false);

          Email email = new Email(ps.cleanString.substring(0, ps.cleanString.indexOf(K_AT)),
              K_MAILTO + ps.cleanString);
          insertEmail(eMails, email);
          idx = ps.idxEnd;
        }
      }
    }

    return eMails.values();
  }

  // this is a static class
  private EmailParser() {
  };
}
