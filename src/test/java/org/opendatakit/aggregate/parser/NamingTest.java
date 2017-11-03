/**
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.parser;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 *
 */

public class NamingTest {

  @Test
  public void testSimpleMappings() {
    assertEquals("THREE_PART_LABEL", Naming.toPersistenceNaming("three_part_label"));
    assertEquals("THREE_PART_LABEL", Naming.toPersistenceNaming("three-part.label"));
    assertEquals("THREE_PART_LABEL", Naming.toPersistenceNaming("threePart_label"));
    assertEquals("THREE_PART_LABEL", Naming.toPersistenceNaming("Three_part_label"));
    assertEquals("THREE_PART_LABEL", Naming.toPersistenceNaming("three_PARtLabel"));
  }

  @Test
  public void testPreserveDashes() {
    assertEquals("THREE__PART__LABEL", Naming.toPersistenceNaming("three__Part__Label"));
    assertEquals("THREE__PART__LABEL", Naming.toPersistenceNaming("three_.Part-_Label"));
    assertEquals("THREE___PART____LABEL", Naming.toPersistenceNaming("three_-.Part____Label"));
  }

  @Test
  public void testTrimName() {
    NamingSet ns = new NamingSet();
    String res;
    assertEquals("", ns.trimName("a", 0));
    assertEquals("", ns.trimName("ag", 0));
    assertEquals("", ns.trimName("age", 0));
    assertEquals("", ns.trimName("ages", 0));

    assertEquals("a", ns.trimName("a", 1));
    assertEquals("a", ns.trimName("ag", 1));
    assertEquals("a", ns.trimName("age", 1));
    assertEquals("a", ns.trimName("ages", 1));

    assertEquals("a", ns.trimName("a", 2));
    assertEquals("ag", ns.trimName("ag", 2));
    assertEquals("ag", ns.trimName("age", 2));
    assertEquals("ag", ns.trimName("ages", 2));

    assertEquals("reT", ns.trimName("reT", 3));
    assertEquals("r2T", ns.trimName("reaT", 3));
    assertEquals("r3T", ns.trimName("realT", 3));
    assertEquals("r4T", ns.trimName("reallT", 3));
    assertEquals("r5T", ns.trimName("reallyT", 3));
    assertEquals("r6T", ns.trimName("reallyLT", 3));
    assertEquals("r7T", ns.trimName("reallyLoT", 3));
    assertEquals("r8T", ns.trimName("reallyLonT", 3));
    assertEquals("r9T", ns.trimName("reallyLongT", 3));
    assertEquals("rea", ns.trimName("reallyLongNT", 3));
    assertEquals("re9T", ns.trimName("reallyLongNT", 4));
    assertEquals("r11T", ns.trimName("reallyLongNaT", 4));
    assertEquals("r12T", ns.trimName("reallyLongNamT", 4));
    assertEquals("r13T", ns.trimName("reallyLongNameT", 4));
    assertEquals("r14T", ns.trimName("reallyLongNamesT", 4));
    assertEquals("r15T", ns.trimName("reallyLongNamersT", 4));
    String value;
    String tv;
    value = "r" + "12345678901234567890123456789012345678901234567890"
        + "123456789012345678901234567890123456789012345678" + "T";
    tv = ns.trimName(value, 4);
    assertEquals("r98T", tv);

    value = "r" + "12345678901234567890123456789012345678901234567890"
        + "1234567890123456789012345678901234567890123456789" + "T";
    tv = ns.trimName(value, 4);
    assertEquals("r99T", tv);

    value = "r" + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890" + "T";
    tv = ns.trimName(value, 4);
    assertEquals("r123", tv);

    value = "r" + "e2345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890" + "T";
    tv = ns.trimName(value, 5);
    assertEquals("re99T", tv);

    value = "r" + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890" + "1T";
    tv = ns.trimName(value, 5);
    assertEquals("r101T", tv);

    value = "r" + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "1234567890123456789012345678901234567890123456789" + "T";
    tv = ns.trimName(value, 5);
    assertEquals("r999T", tv);

    value = "r" + "s2345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890" + "T";
    tv = ns.trimName(value, 6);
    assertEquals("rs999T", tv);
  }

  @Test
  public void testAccentsMappings() {
    assertEquals("\u00caTES_VOUS", Naming.toPersistenceNaming("\u00cates-vous"));
    char uch = Character.toUpperCase('\u00e9');
    assertEquals(("R" + uch + "P" + uch + "TER_EFFACER"),
        Naming.toPersistenceNaming("R\u00e9p\u00e9terEffacer"));
  }

  @Test
  public void testUnicodeMappings() {

    // chinese...
    String unicode = "\u5728\u87a2\u5e55\u4e0a\u53ef\u6ed1\u52d5\u624b\u6307\u9032\u884c\u700f\u89bd\u6309\u4e0b\u9078\u55ae\u518d\u9032\u884c\u66f4\u591a\u9805\u76ee\u7684\u64cd\u4f5c";
    // todo -- uppercase test in unicode?
    assertEquals(unicode, Naming.toPersistenceNaming(unicode));
  }
}
