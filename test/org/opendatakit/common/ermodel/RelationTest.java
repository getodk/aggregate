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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

/**
 * Simple test case of the AbstractRelation class.  Also an example of how to use it.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
@RunWith(org.junit.runners.JUnit4.class)
public class RelationTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = new MyRelation(cc);
		rel.dropRelation(cc); // drop it, in case prior test was messed up...
	}

	private static final long DATE_CONSTANT_VALUE = 5055505L;

	static class MyRelation extends AbstractRelation {

		static final DataField fieldStr = new DataField("THIS_IS_IT", DataField.DataType.STRING, true, 90L );
		static final DataField fieldInt = new DataField("SECOND_FIELD", DataField.DataType.INTEGER, true);
		static final DataField fieldDbl = new DataField("THIRD_FIELD", DataField.DataType.DECIMAL, true);
		static final DataField fieldDate = new DataField("FOURTH_FIELD", DataField.DataType.DATETIME, true);
		static final List<DataField> fields;
		static {
			fields = new ArrayList<DataField>();
			fields.add(fieldStr);
			fields.add(fieldInt);
			fields.add(fieldDbl);
			fields.add(fieldDate);
		}
		
		MyRelation(CallingContext cc) throws ODKDatastoreException {
			super("MY_TABLE", fields, cc );
		}
	}
	
	@Test
	public void testCase1() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);
		e.setString(MyRelation.fieldStr, "This is a string");
		e.setDouble(MyRelation.fieldDbl, 4.4);
		e.setInteger(MyRelation.fieldInt, Integer.MIN_VALUE);
		e.setDate(MyRelation.fieldDate, new Date(DATE_CONSTANT_VALUE));
		e.persist(cc);
		
		List<Entity> entities = rel.getEntities(MyRelation.fieldDbl, FilterOperation.LESS_THAN, 5.0, cc);
		assertEquals(1, entities.size());
		assertEquals( e.getUri(), entities.get(0).getUri());
		
		entities = rel.getEntities(MyRelation.fieldInt, FilterOperation.EQUAL, Integer.MIN_VALUE, cc);
		assertEquals(1, entities.size());
		assertEquals( e.getUri(), entities.get(0).getUri());
		
		Entity eKey = rel.getEntity(e.getUri(), cc);
		assertEquals( e.getUri(), eKey.getUri());
		assertEquals( ( e.getDate(MyRelation.fieldDate).getTime() / PersistConsts.MIN_DATETIME_RESOLUTION) * PersistConsts.MIN_DATETIME_RESOLUTION, 
						eKey.getDate(MyRelation.fieldDate).getTime());

		eKey.setInteger(MyRelation.fieldInt, 40);
		rel.putEntity(eKey, cc);
		
		Entity eNew = rel.getEntity(e.getUri(), cc);
		assertEquals( eNew.getInteger(MyRelation.fieldInt), Integer.valueOf(40));
		
		e.persist(cc);
		eKey = rel.getEntity(e.getUri(), cc);
		assertEquals( eKey.getInteger(MyRelation.fieldInt), Integer.valueOf(Integer.MIN_VALUE));
		
		rel.dropRelation(cc);
	}		
	
	@Test(expected=IllegalArgumentException.class)
	public void testCase2() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);
		
		e.setField("secondField", "alpha");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCase3() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);
		
		e.setField("thirdField", "alpha");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCase4() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);
		
		e.setField("fourthField", "alpha");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCase5() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);
		
		e.setField("thisIsIt", 
				"12345678901234567890123456789012345678901234567890" +
				"12345678901234567890123456789012345678901234567890" );
	}

	@Test
	public void testCase6() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);
		
		e.setField("secondField", "5");
		e.setField("thirdField", "5.81");
		e.setField("fourthField", (new Date()).toString());
		e.setField("thisIsIt", "a simple long string");
		
		e.persist(cc);
		
		rel.dropRelation(cc);
	}
}
