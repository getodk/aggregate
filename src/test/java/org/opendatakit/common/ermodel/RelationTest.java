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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
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
public class RelationTest {

	@Before
	public void setUp() throws Exception {
		CallingContext cc = TestContextFactory.getCallingContext();

		MyRelation rel = new MyRelation(cc);
		rel.dropRelation(cc); // drop it, in case prior test was messed up...
	}

	private static final long DATE_CONSTANT_VALUE = 5055505L;

	static class MyRelation extends Relation {

		static final DataField fieldStr = new DataField("THIS_IS_IT", DataField.DataType.STRING, true, 90L );
		static final DataField fieldInt = new DataField("SECOND_FIELD", DataField.DataType.INTEGER, true);
		static final DataField fieldDbl = new DataField("THIRD_FIELD", DataField.DataType.DECIMAL, true);
		static final DataField fieldDate = new DataField("FOURTH_FIELD", DataField.DataType.DATETIME, true);
		static final DataField fieldBool = new DataField("FIFTH_FIELD", DataField.DataType.BOOLEAN, true);
		static final List<DataField> fields;
		static {
			fields = new ArrayList<DataField>();
			fields.add(fieldStr);
			fields.add(fieldInt);
			fields.add(fieldDbl);
			fields.add(fieldDate);
			fields.add(fieldBool);
		}

		MyRelation(CallingContext cc) throws ODKDatastoreException {
			super(TableNamespace.EXTENSION, "__MY_TABLE", fields, cc );
		}
	}

	@Test
	public void testCase1() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();

		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);
		e.set(MyRelation.fieldStr, "This is a string");
		e.set(MyRelation.fieldDbl, 4.4);
		e.set(MyRelation.fieldInt, Integer.MIN_VALUE);
		e.set(MyRelation.fieldDate, new Date(DATE_CONSTANT_VALUE));
		e.set(MyRelation.fieldBool, (Boolean) null);
		e.put(cc);


	    Query query;
	    List<Entity> entities;

	    query = rel.query("DbTable.testCase1.fieldDbl-lessThan", cc);
	    query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.LESS_THAN, 5.0);
	    entities = query.execute();
	   assertEquals(1, entities.size());
		assertEquals( e.getId(), entities.get(0).getId());

      query = rel.query("DbTable.testCase1.intMinValue", cc);
      query.addFilter(MyRelation.fieldInt.getName(), FilterOperation.EQUAL, Integer.MIN_VALUE);
      entities = query.execute();
		assertEquals(1, entities.size());
		assertEquals( e.getId(), entities.get(0).getId());

		Entity eKey = rel.getEntity(e.getId(), cc);
		assertEquals( e.getId(), eKey.getId());
		assertEquals( ( e.getDate(MyRelation.fieldDate).getTime() / PersistConsts.MIN_DATETIME_RESOLUTION) * PersistConsts.MIN_DATETIME_RESOLUTION,
					  ( eKey.getDate(MyRelation.fieldDate).getTime() / PersistConsts.MIN_DATETIME_RESOLUTION) * PersistConsts.MIN_DATETIME_RESOLUTION);

		assertNull( eKey.getBoolean(MyRelation.fieldBool));

		eKey.set(MyRelation.fieldInt, 40);
		eKey.set(MyRelation.fieldBool, true);
		MyRelation.putEntity(eKey, cc);

		Entity eNew = rel.getEntity(e.getId(), cc);
		assertEquals( eNew.getInteger(MyRelation.fieldInt), Integer.valueOf(40));
		assertTrue( eNew.getBoolean(MyRelation.fieldBool));

		e.put(cc);
		eKey = rel.getEntity(e.getId(), cc);
		assertEquals( eKey.getInteger(MyRelation.fieldInt), Integer.valueOf(Integer.MIN_VALUE));

		rel.dropRelation(cc);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCase2() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();

		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);

		e.setAsString("secondField", "alpha");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCase3() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();

		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);

		e.setAsString("thirdField", "alpha");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCase4() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();

		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);

		e.setAsString("fourthField", "alpha");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCase5() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();

		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);

		e.set("thisIsIt",
				"12345678901234567890123456789012345678901234567890" +
				"12345678901234567890123456789012345678901234567890" );
	}

	@Test
	public void testCase6() throws ODKDatastoreException {
		CallingContext cc = TestContextFactory.getCallingContext();

		MyRelation rel = new MyRelation(cc);
		rel = new MyRelation(cc);
		Entity e = rel.newEntity(cc);

		e.setAsString("secondField", "5");
		e.setAsString("thirdField", "5.81");
		e.setAsString("fourthField", (new Date()).toString());
		e.set("thisIsIt", "a simple long string");

		e.put(cc);

		rel.dropRelation(cc);
	}

   @Test
   public void testCase7() throws ODKDatastoreException {
      CallingContext cc = TestContextFactory.getCallingContext();

      MyRelation rel = new MyRelation(cc);
      rel = new MyRelation(cc);
      Entity e = rel.newEntity(cc);

      e.setAsString("secondField", "5");
      e.setAsString("thirdField", "5.81");
      e.setAsString("fourthField", (new Date()).toString());
      e.set("thisIsIt", "a simple long string");

      e.put(cc);

      Entity e2 = rel.newEntity(cc);

      e2.setAsString("secondField", "6");
      e2.setAsString("thirdField", "6.81");
      e2.setAsString("fourthField", (new Date(0)).toString());
      e2.set("thisIsIt", "another simple long string");

      e2.put(cc);

      Query query;
      List<Entity> entities;

      query = rel.query("DbTable.testCase7.fieldDbl-notEqual", cc);
      query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.NOT_EQUAL, 5.81);
      entities = query.execute();
      assertEquals(1, entities.size());
      assertEquals( e2.getId(), entities.get(0).getId());

      rel.dropRelation(cc);
   }
}
