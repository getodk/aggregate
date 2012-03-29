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
@RunWith(org.junit.runners.JUnit4.class)
public class NamingTest {

	@Test
    public void testSimpleMappings() {
	assertEquals("THREE_PART_LABEL",Naming.toPersistenceNaming("three_part_label"));
	assertEquals("THREE_PART_LABEL",Naming.toPersistenceNaming("three-part.label"));
	assertEquals("THREE_PART_LABEL",Naming.toPersistenceNaming("threePart_label"));
	assertEquals("THREE_PART_LABEL",Naming.toPersistenceNaming("Three_part_label"));
	assertEquals("THREE_PART_LABEL",Naming.toPersistenceNaming("three_PARtLabel"));
    }
    
	@Test
    public void testPreserveDashes() {
	assertEquals("THREE__PART__LABEL",Naming.toPersistenceNaming("three__Part__Label"));
	assertEquals("THREE__PART__LABEL",Naming.toPersistenceNaming("three_.Part-_Label"));
	assertEquals("THREE___PART____LABEL",Naming.toPersistenceNaming("three_-.Part____Label"));
    }
    
	@Test
    public void testAccentsMappings() {
	assertEquals("\u00caTES_VOUS",Naming.toPersistenceNaming("\u00cates-vous"));
	char uch = Character.toUpperCase('\u00e9');
	assertEquals(("R"+uch+"P"+uch+"TER_EFFACER"),Naming.toPersistenceNaming("R\u00e9p\u00e9terEffacer"));
    }
    
	@Test
    public void testUnicodeMappings() {
	
	// chinese...
    String unicode="\u5728\u87a2\u5e55\u4e0a\u53ef\u6ed1\u52d5\u624b\u6307\u9032\u884c\u700f\u89bd\u6309\u4e0b\u9078\u55ae\u518d\u9032\u884c\u66f4\u591a\u9805\u76ee\u7684\u64cd\u4f5c";
	// todo -- uppercase test in unicode?
	assertEquals(unicode,Naming.toPersistenceNaming(unicode));	
    }
}
