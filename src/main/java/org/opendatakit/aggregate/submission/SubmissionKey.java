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
package org.opendatakit.aggregate.submission;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * The submission key represents an XPath-style identification of a particular
 * data element on the server.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class SubmissionKey {

  private final String key;

  public SubmissionKey(String key) {
    this.key = key;
  }

  /**
   * Used by form deletion.
   *
   * @param formId
   * @param modelVersion
   * @param uiVersion
   * @param topLevelGroupElementName
   * @param uri
   */
  public SubmissionKey(String formId, String topLevelGroupElementName, String uri) {
    StringBuilder b = new StringBuilder();
    b.append(formId);
    b.append(SubmissionKeyPart.K_OPEN_BRACKET_VERSION_EQUALS);
    b.append("null");
    b.append(SubmissionKeyPart.K_AND_UI_VERSION_EQUALS);
    b.append("null");
    b.append(SubmissionKeyPart.K_CLOSE_BRACKET);
    b.append(SubmissionKeyPart.K_SLASH);
    b.append(topLevelGroupElementName);
    b.append(SubmissionKeyPart.K_OPEN_BRACKET_KEY_EQUALS);
    b.append(uri);
    b.append(SubmissionKeyPart.K_CLOSE_BRACKET);
    this.key = b.toString();
  }

  public String toString() {
    return key;
  }

  /**
   * Parse the SubmissionKey into its individual parts.
   * This is complicated by:
   * <ol>
   * <li>formId may contain slashes</li>
   * <li>key may contain slashes</li>
   * </ol>
   *
   * @return List&lt;SubmissionKeyPart&gt; of the parts of the key
   */
  public final List<SubmissionKeyPart> splitSubmissionKey() {
    List<SubmissionKeyPart> parts = new ArrayList<SubmissionKeyPart>();
    List<String> stringParts = new ArrayList<String>();
    String firstPart;
    String remainder;
    // Step 1: parse out the 'formId[@version=... and @uiVersion=...]/' segment
    // The formId may contain slashes.
    // handle slashes in formId by looking backward for the first occurrence of
    // the [@version=
    int idxAfterFormId = key.lastIndexOf(SubmissionKeyPart.K_OPEN_BRACKET_VERSION_EQUALS);
    if (idxAfterFormId != -1) {
      int idxEndBracket = key.indexOf(SubmissionKeyPart.K_CLOSE_BRACKET_SLASH, idxAfterFormId);
      String formId = key.substring(0, idxAfterFormId);
      if (formId.contains(ParserConsts.FORWARD_SLASH)) {
        formId = formId.replaceAll(ParserConsts.FORWARD_SLASH,
            ParserConsts.FORWARD_SLASH_SUBSTITUTION);
      }
      firstPart = formId + key.substring(idxAfterFormId, idxEndBracket + 1);
      remainder = key.substring(idxEndBracket + SubmissionKeyPart.K_CLOSE_BRACKET_SLASH.length());
    } else {
      // no [@version=
      throw new IllegalStateException("Did not find [@version= qualifier in SubmissionKey: " + key);
    }

    // firstPart is the formId[@version...] string
    stringParts.add(firstPart);

    if (remainder.length() != 0) {
      // Step 2: parse out the 'elementname[@key=...]/' segment.
      // The elementname does not contain slashes, but the @key may.
      // Assume the @key does not contain ']/'

      int idx = remainder.indexOf(SubmissionKeyPart.K_CLOSE_BRACKET_SLASH);
      if (idx == -1) {
        int endIndex = remainder.length()-1;
        char lastChar = remainder.charAt(endIndex);
        if(lastChar == ']') {
          firstPart = remainder.substring(0, endIndex + 1);
          remainder = remainder.substring(endIndex + 1);
        } else {
          throw new IllegalStateException("Did not find [@key=...]/ within SubmissionKey: " + key);
        }
      } else {
        firstPart = remainder.substring(0, idx + 1);
        remainder = remainder.substring(idx + SubmissionKeyPart.K_CLOSE_BRACKET_SLASH.length());
      }
        
      // firstPart is the elementname[@key=...] string
      stringParts.add(firstPart);
    }

    if (remainder.length() != 0) {
      // the remaining parts can simply be split at slashes...
      String[] remainingParts = remainder.split(BasicConsts.FORWARDSLASH);
      for (String s : remainingParts) {
        stringParts.add(s);
      }
    }

    for (String s : stringParts) {
      parts.add(new SubmissionKeyPart(s));
    }
    return parts;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SubmissionKey))
      return false;
    SubmissionKey other = (SubmissionKey) obj;
    return (key == null) ? (other.key == null) : key.equals(other.key);
  }
}
