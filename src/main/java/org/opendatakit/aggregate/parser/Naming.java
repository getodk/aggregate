/*
  Copyright (C) 2010 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.aggregate.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for converting a list of XForm (XML) tag names to the SQL
 * naming used for database tables and columns. <b>This is a non-invertible
 * mapping.</b> I.e., two lists of XForm tags can map to the same SQL naming.
 * <p>
 * The conversion generally attempts to make ANSI SQL table and column names
 * that are ALL_CAPITALS_WITH_UNDERSCORES. These have the benefit of being
 * proper SQL identifiers that are usable without quotation marks in ordinary
 * command line tools.
 * <p>
 * To do this, the following assumptions are made:
 * <ol>
 * <li>the form will not simultaneously contain two or more tags with dash, dot
 * or underscore separators but which are otherwise identical. E.g., a form will
 * not simultaneously contain tags called <code>&lt;three_part_label/&gt;</code>, <code>&lt;three-part-label/&gt;</code> or
 * <code>&lt;three.part.label/&gt;</code>. The conversion routine will collapse
 * underscores, dashes and dots to the same (underscore) character, resulting in
 * the same SQL naming.</li>
 * <li>if you are using a underscore, dash or dot separator in your tag name,
 * then you are not also using leading capitalization to distinguish between two
 * tags. I.e., the form will not simultaneously contain tags called
 * <code>&lt;three_part_label/&gt;</code> and
 * <code>&lt;three_Part_Label/&gt;</code>. The conversion routine will collapse
 * these to the same SQL naming.</li>
 * <li>leading capitalization will produce an SQL naming that inserts an
 * underscore in front of the first capitalized letter if it is not already
 * preceded by an underscore or not the leading character of a tag. Thus, forms
 * cannot contain two tags that are otherwise identical alphanumeric strings
 * with a change in leading capitalization or with dashes, underscores or dots
 * inserted where one string has a leading capital letter. E.g.,
 * <code>&lt;Three-part-label/&gt;</code>,
 * <code>&lt;three-part-label/&gt;</code>, <code>&lt;three-PartLabel/&gt;</code>, <code>&lt;threePartLabel/&gt;</code> and
 * <code>&lt;threePart.label/&gt;</code> all map to the same SQL naming.</li>
 * <li>if a tag contains two or more consecutive underscores, dashes or dots,
 * the same number of consecutive underscores will appear in the SQL naming.
 * I.e., the naming does not collapse the underscores.</li>
 * </ol>
 * <p>
 * If <code>\p{Lu}</code> stands for all uppercase UTF-8 characters and
 * <code>\p{Ll}</code> stands for all lowercase UTF-8 characters, then the
 * allowed format for an XML tag name (per the standard) is roughly:
 *
 * <pre>
 * ^[_\p{Lu}\p{Ll}][-.0-9_\p{Lu}\p{Ll}]*$
 * </pre>
 * <p>
 *
 * @author mitchellsundt@gmail.com
 */
class Naming {

  // what a legal persistence naming should be...
  private static final String utf8String = "^[\\p{Lu}\\p{Lo}_][0-9\\p{Lu}\\p{Lo}_]*$";
  private static final Pattern p = Pattern.compile(utf8String);

  public static final boolean isValidString(final String xmlString) {
    Matcher m = p.matcher(xmlString);
    return m.matches();
  }

  /**
   * Converts the string to a SQL naming
   *
   * @param xmlString the xml tag name
   */
  public static final String toPersistenceNaming(final String xmlString) {
    final StringBuilder sb = new StringBuilder();
    boolean priorUnderscoreOrCap = true;
    for (int i = 0; i < xmlString.length(); ++i) {
      final char ch = xmlString.charAt(i);
      if (Character.isUpperCase(ch)) {
        if (!priorUnderscoreOrCap)
          sb.append('_');
        sb.append(ch);
        priorUnderscoreOrCap = true;
      } else if (ch == '-' || ch == '.' || ch == '_') {
        sb.append('_');
        priorUnderscoreOrCap = true;
      } else {
        sb.append(Character.toUpperCase(ch));
        priorUnderscoreOrCap = false;
      }
    }
    assert isValidString(sb.toString());
    return sb.toString();
  }
}
