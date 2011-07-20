/**
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.common.ermodel;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

import junit.framework.TestCase;

/**
 * Simple test case for the AbstractBlobRelationSet class.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class BlobRelationSetTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		CallingContext cc = TestContextFactory.getCallingContext();

		// ensure that blobset tables are not present...
		MyBlobRelationSet set = new MyBlobRelationSet(cc);
		set.dropBlobEntitySet(cc);
	}
	
	static class MyBlobRelationSet extends AbstractBlobRelationSet {
		
		MyBlobRelationSet(CallingContext cc) throws ODKDatastoreException {
			super("TEST_BLOB", cc);
		}
	}
	
	public void testCase1() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyBlobRelationSet rel = new MyBlobRelationSet(cc);
		
		BlobEntitySet instance = rel.newBlobEntitySet(cc);
		assertEquals( 0, instance.getAttachmentCount());
		
		String s = "this is a string";
		instance.addBlob(s.getBytes(), "text/plain", null, cc);
		String t = "another string";
		instance.addBlob(t.getBytes(), "text/xml", "different", cc);
		instance.persist(cc);
		BlobEntitySet alt = rel.getBlobEntitySet(instance.getUri(), cc);
		assertEquals(2, alt.getAttachmentCount());
		String name = alt.getUnrootedFilename(1);
		String otherType = null;
		if ( name == null ) {
			assertEquals("text/plain", alt.getContentType(1));
			assertEquals(s.getBytes().length, alt.getContentLength(1).intValue());
			otherType = "text/xml";
		} else {
			assertEquals("text/xml", alt.getContentType(1));
			assertEquals(t.getBytes().length, alt.getContentLength(1).intValue());
			otherType = "text/plain";
		}
		assertEquals(otherType, alt.getContentType(2));
		
		rel.dropBlobEntitySet(cc);
	}
}
