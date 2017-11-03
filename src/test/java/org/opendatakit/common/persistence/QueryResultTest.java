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
package org.opendatakit.common.persistence;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;


public class QueryResultTest {

	static int SET_SIZE = 10; // This used to be 2000
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {

		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = MyRelation.assertRelation(cc);
		
		System.out.println("dropping the large dataset");
		cc.getDatastore().dropRelation(rel, cc.getCurrentUser());// drop it, in case prior test was messed up...
		
		rel = MyRelation.assertRelation(cc);
		
		System.out.println("writing the large dataset");
		
		// write a lot of data...
		for ( DataValue value : values ) {
			value.assertInstance(rel, cc);
		}
		
		try {
			Thread.sleep(PersistConsts.MAX_SETTLE_MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("done writing the large dataset");
	}
	
	@AfterClass
	public static void afterClass() throws Exception {

		CallingContext cc = TestContextFactory.getCallingContext();
		
		MyRelation rel = MyRelation.assertRelation(cc);
		
		System.out.println("dropping the large dataset");
		cc.getDatastore().dropRelation(rel, cc.getCurrentUser());// drop it, in case prior test was messed up...
		
	}

	static class DataValue {
		String str;
		Long l;
		WrappedBigDecimal bd;
		Date d;
		Boolean b;
		
		DataValue(String str, Integer n, Double bd, String dateStr, Boolean b ) {
			this.str = str;
			this.l = (n == null) ? null : Long.valueOf(n);
			this.bd = (bd == null) ? null : WrappedBigDecimal.fromDouble(bd);
			this.d = WebUtils.parseDate(dateStr);
			this.b = b;
		}
		
		void assertInstance(MyRelation rel, CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
			Datastore ds = cc.getDatastore();
			User user = cc.getCurrentUser();
			
			for ( int i = 0 ; i < SET_SIZE ; ++i ) {
				MyRelation element = ds.createEntityUsingRelation(rel, user);
				element.setStringField(MyRelation.fieldStr, str);
				element.setLongField(MyRelation.fieldInt, Long.valueOf(i));
				element.setNumericField(MyRelation.fieldDbl, bd);
				element.setDateField(MyRelation.fieldDate, d);
				element.setBooleanField(MyRelation.fieldBool, b);
				
				ds.putEntity(element, user);
			}
			
			for ( int i = 0 ; i < SET_SIZE ; ++i ) {
				MyRelation element = ds.createEntityUsingRelation(rel, user);
				element.setStringField(MyRelation.fieldStr, str);
				element.setLongField(MyRelation.fieldInt, Long.valueOf(i));
				element.setNumericField(MyRelation.fieldDbl, new WrappedBigDecimal(Integer.toString(i)));
				element.setDateField(MyRelation.fieldDate, d);
				element.setBooleanField(MyRelation.fieldBool, b);
				
				ds.putEntity(element, user);
			}
		}
	}
	
	static DataValue[] values = { 
		/* String str, Integer n, Double bd, String dateStr, Boolean b */
			new DataValue("A", 1, 0.9, "2009-01-30", false), 
			new DataValue("A", 1, 1.9, "2009-01-30", false), 
			new DataValue("A", 1, 2.9, "2009-01-30", false), 
			new DataValue("A", 1, 3.9, "2009-01-30", false), 
			new DataValue("A", 1, 4.9, "2009-01-30", false), 
			new DataValue("A", 1, 5.9, "2009-01-30", false), 
			new DataValue("A", 1, 6.9, "2009-01-30", false), 
			new DataValue("A", 1, 0.9, "2009-02-30", false), 
			new DataValue("A", 1, 0.9, "2009-03-30", null), 
			new DataValue("A", 1, 0.9, "2009-04-30", false), 
			new DataValue("A", 1, 0.9, "2009-05-30", false), 
			new DataValue("A", 1, 0.9, "2009-06-30", false), 
			new DataValue("A", 1, 1.9, "2009-02-30", false), 
			new DataValue("A", 1, 1.9, "2009-02-30", false), 
			new DataValue("A", 1, 1.9, "2009-02-30", false), 
			new DataValue("A", 1, 1.9, "2009-02-30", false), 
			new DataValue("A", 1, 1.9, "2009-02-30", false), 
			new DataValue("B", 1, 0.9, "2009-01-30", false), 
			new DataValue("C", 1, 0.9, "2009-01-30", null), 
			new DataValue("D", 1, 0.9, "2009-01-30", false), 
			new DataValue("E", 1, 0.9, "2009-01-30", false), 
			new DataValue("F", 1, 0.9, "2009-01-30", false),
			new DataValue("A", 1, 0.9, "2009-01-30", true), 
			new DataValue("A", 1, 1.9, "2009-01-30", true), 
			new DataValue("A", 1, 2.9, "2009-01-30", true), 
			new DataValue("A", 1, 3.9, "2009-01-30", true), 
			new DataValue("A", 1, 4.9, "2009-01-30", true), 
			new DataValue("A", 1, 5.9, "2009-01-30", true), 
			new DataValue("A", 1, 6.9, "2009-01-30", true), 
			new DataValue("B", 1, 0.9, "2009-02-30", false), 
			new DataValue("B", 1, 0.9, "2009-03-30", true), 
			new DataValue("B", 1, 0.9, "2009-04-30", false), 
			new DataValue("B", 1, 0.9, "2009-05-30", true), 
			new DataValue("B", 1, 0.9, "2009-06-30", false), 
			new DataValue("B", 1, 3.9, "2009-01-30", true), 
			new DataValue("B", 1, 3.9, "2009-02-30", false), 
			new DataValue("B", 1, 3.9, "2009-03-30", true), 
			new DataValue("B", 1, 3.9, "2009-04-30", false), 
			new DataValue("B", 1, 3.9, "2009-05-30", true), 
			new DataValue("B", 1, 3.9, "2009-06-30", false), 
	};
	
	@Ignore
	static class MyRelation extends CommonFieldsBase {

		static final DataField fieldStr = new DataField("THIS_IS_IT", DataField.DataType.STRING, true, 90L );
		static final DataField fieldInt = new DataField("SECOND_FIELD", DataField.DataType.INTEGER, true);
		static final DataField fieldDbl = new DataField("THIRD_FIELD", DataField.DataType.DECIMAL, true);
		static final DataField fieldDate = new DataField("FOURTH_FIELD", DataField.DataType.DATETIME, true);
		static final DataField fieldBool = new DataField("FIFTH_FIELD", DataField.DataType.BOOLEAN, true);
		
		/**
		 * Construct a relation prototype.
		 * 
		 * @param databaseSchema
		 */
		private MyRelation(String databaseSchema) {
			super(databaseSchema, "MY_QRT_TABLE" );
			fieldList.add(fieldStr);
			fieldList.add(fieldInt);
			fieldList.add(fieldDbl);
			fieldList.add(fieldDate);
			fieldList.add(fieldBool);
		}

		/**
		 * Construct an empty entity.
		 * 
		 * @param ref
		 * @param user
		 */
		private MyRelation(MyRelation ref, User user) {
			super(ref, user);
		}

		@Override
		public CommonFieldsBase getEmptyRow(User user) {
			return new MyRelation(this, user);
		}
		
		public void print() {
			System.out.println("PK " + getUri() + " " 
					+ fieldStr.getName() + " " 
					+ getStringField(fieldStr) + " " 
					+ getLongField(fieldInt) + " "
					+ getNumericField(fieldDbl) + " " 
					+ getDateField(fieldDate) + " " 
					+ getBooleanField(fieldBool) );
		}
		
		static synchronized final MyRelation assertRelation(CallingContext cc) throws ODKDatastoreException {
			MyRelation relationPrototype;
			Datastore ds = cc.getDatastore();
			User user = cc.getUserService().getDaemonAccountUser();
			relationPrototype = new MyRelation(ds.getDefaultSchemaName());
		    ds.assertRelation(relationPrototype, user); // may throw exception...
		    // at this point, the prototype has become fully populated
		    return relationPrototype; // set static variable only upon success...
		}
	}
	
	@Test
	public void testCase1str() throws ODKDatastoreException {
		
		CallingContext cc = TestContextFactory.getCallingContext();
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		MyRelation rel = MyRelation.assertRelation(cc);
		
		Query query = ds.createQuery(rel, "QueryResultTest.testCase1str", user);
		
		List<?> values = query.executeDistinctValueForDataField(MyRelation.fieldStr);
		assertEquals(6, values.size());
	}

	@Test
	public void testCase1dbl() throws ODKDatastoreException {
		
		CallingContext cc = TestContextFactory.getCallingContext();
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		MyRelation rel = MyRelation.assertRelation(cc);
		
		Query query = ds.createQuery(rel, "QueryResultTest.testCase1dbl", user);
		
		List<?> values = query.executeDistinctValueForDataField(MyRelation.fieldDbl);
		assertEquals(SET_SIZE + 7, values.size());
	}
	
	@Test
	public void testCase1bool() throws ODKDatastoreException {
		
		CallingContext cc = TestContextFactory.getCallingContext();
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		MyRelation rel = MyRelation.assertRelation(cc);
		
		Query query = ds.createQuery(rel, "QueryResultTest.testCase1bool", user);
		
		List<?> values = query.executeDistinctValueForDataField(MyRelation.fieldBool);
		// true, false, null
		assertEquals(3, values.size());
	}
	
	@Test
	public void testCase1int() throws ODKDatastoreException {
		
		CallingContext cc = TestContextFactory.getCallingContext();
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		MyRelation rel = MyRelation.assertRelation(cc);
		
		Query query = ds.createQuery(rel, "QueryResultTest.testCase1int", user);
		
		List<?> values = query.executeDistinctValueForDataField(MyRelation.fieldInt);
		assertEquals(SET_SIZE, values.size());
	}
	
	@Test
	public void testCase2() throws ODKDatastoreException {
		
		CallingContext cc = TestContextFactory.getCallingContext();
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		MyRelation rel = MyRelation.assertRelation(cc);
		
		Query query = ds.createQuery(rel, "QueryResultTest.testCase2", user);
		query.addFilter(MyRelation.fieldInt, FilterOperation.GREATER_THAN, SET_SIZE - 2);
		query.addSort(MyRelation.fieldDate, Direction.ASCENDING);
		query.addSort(MyRelation.fieldDbl, Direction.DESCENDING);
		
		Set<String> pkSet = new HashSet<String>();
		
		int len = 0;
		QueryResult result = query.executeQuery(null, 10);
		len += result.getResultList().size();
		assertEquals( true, result.hasMoreResults());
		assertEquals( 10, result.getResultList().size());
		
		for ( CommonFieldsBase cb : result.getResultList() ) {
			assertEquals(false, pkSet.contains(cb.getUri()));
			pkSet.add(cb.getUri());
			((MyRelation) cb).print();
		}
		boolean done = false;
		QueryResumePoint startCursor = result.getResumeCursor();
		while ( !done ) {
			System.out.println("Issuing follow-up query");
			result = query.executeQuery(startCursor, 20);
			len += result.getResultList().size();
			startCursor = result.getResumeCursor();
			done = !result.hasMoreResults();
	
			for ( CommonFieldsBase cb : result.getResultList() ) {
				assertEquals(false, pkSet.contains(cb.getUri()));
				pkSet.add(cb.getUri());
				((MyRelation) cb).print();
			}
		}	
		
		assertEquals( false, result.hasMoreResults());
		assertEquals( 2*values.length, len);
		System.out.println("done with testCase2");
	}
	
	@Test
	public void testCase3() throws ODKDatastoreException {
		
		CallingContext cc = TestContextFactory.getCallingContext();
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		MyRelation rel = MyRelation.assertRelation(cc);
		System.out.println("start testCase3");
		
		Query query = ds.createQuery(rel, "QueryResultTest.testCase3(1st)", user);
		query.addFilter(MyRelation.fieldDbl, FilterOperation.EQUAL, new BigDecimal("0.9"));
		query.addFilter(MyRelation.fieldBool, FilterOperation.EQUAL, true);
		query.addSort(MyRelation.fieldInt, Direction.ASCENDING);
		query.addSort(MyRelation.fieldDate, Direction.DESCENDING);

		Query backquery = ds.createQuery(rel, "QueryResultTest.testCase3(2nd)", user);
		backquery.addFilter(MyRelation.fieldDbl, FilterOperation.EQUAL, new BigDecimal("0.9"));
		backquery.addFilter(MyRelation.fieldBool, FilterOperation.EQUAL, true);
		backquery.addSort(MyRelation.fieldInt, Direction.DESCENDING);
		backquery.addSort(MyRelation.fieldDate, Direction.ASCENDING);

		Set<String> pkTotalSet = new HashSet<String>();
		
		List<String> pkOrdering = new ArrayList<String>();
		
		int TOTAL_SIZE = 3*SET_SIZE;
		int fetchSizes[] = {1, 2, 1021, 303, 101, 2831 };
		int idxFetch = 4;
		int len = 0;
		QueryResult result = query.executeQuery(null, fetchSizes[idxFetch]);
		len += result.getResultList().size();
		assertEquals( true, result.hasMoreResults());
		assertEquals( fetchSizes[idxFetch], result.getResultList().size());
		
		System.out.println("Accumulating forward query results");
		for ( CommonFieldsBase cb : result.getResultList() ) {
			assertEquals(false, pkOrdering.contains(cb.getUri()));
			assertEquals(false, pkTotalSet.contains(cb.getUri()));
			pkOrdering.add(cb.getUri());
			pkTotalSet.add(cb.getUri());
			((MyRelation) cb).print();
		}
		
		System.out.println("Verifying initial backward query is empty");
		QueryResult backResult = backquery.executeQuery( result.getBackwardCursor(), pkOrdering.size());
		assertEquals( false, backResult.hasMoreResults());
		assertEquals(0, backResult.getResultList().size());
		
		boolean notFirst = false;
		boolean done = false;
		QueryResumePoint startCursor = result.getResumeCursor();
		while ( !done ) {
			idxFetch = ( idxFetch + 1) % fetchSizes.length;
			System.out.println("Issuing follow-up query");
			result = query.executeQuery(startCursor, fetchSizes[idxFetch]);
			len += result.getResultList().size();
			startCursor = result.getResumeCursor();
			done = !result.hasMoreResults();
	
			System.out.println("Verifying backward query against ordering of earlier result");
			backResult = backquery.executeQuery( result.getBackwardCursor(), pkOrdering.size());
			assertEquals( notFirst, backResult.hasMoreResults());
			notFirst = true;
			assertEquals( pkOrdering.size(), backResult.getResultList().size());
			for ( int i = 0 ; i < pkOrdering.size() ; ++i ) {
				CommonFieldsBase cb = backResult.getResultList().get(i);
				((MyRelation) cb).print();
				assertEquals( pkOrdering.get(pkOrdering.size()-i-1), cb.getUri());
			}
			
			System.out.println("Accumulating forward query results");
			pkOrdering.clear();
			for ( CommonFieldsBase cb : result.getResultList() ) {
				assertEquals(false, pkOrdering.contains(cb.getUri()));
				assertEquals(false, pkTotalSet.contains(cb.getUri()));
				pkOrdering.add(cb.getUri());
				pkTotalSet.add(cb.getUri());
				((MyRelation) cb).print();
			}
		}	
		
		idxFetch = ( idxFetch + 1) % fetchSizes.length;
		System.out.println("Before Issuing (what should be empty) follow-up query");
		result = query.executeQuery(startCursor, fetchSizes[idxFetch]);
		len += result.getResultList().size();
		startCursor = result.getResumeCursor();
		done = !result.hasMoreResults();

		System.out.println("Verifying backward query against ordering of earlier result");
		// backquery should match existing data
		backResult = backquery.executeQuery( result.getBackwardCursor(), pkOrdering.size());
		assertEquals( true, backResult.hasMoreResults());
		assertEquals( pkOrdering.size(), backResult.getResultList().size());
		for ( int i = 0 ; i < pkOrdering.size() ; ++i ) {
			CommonFieldsBase cb = backResult.getResultList().get(i);
			((MyRelation) cb).print();
			assertEquals( pkOrdering.get(pkOrdering.size()-i-1), cb.getUri());
		}
		
		assertEquals(false, result.hasMoreResults());
		assertEquals(0, result.getResultList().size());
		
		idxFetch = ( idxFetch + 1) % fetchSizes.length;
		System.out.println("Before Re-Issuing (what should be empty) follow-up query");
		// this should be an empty list
		result = query.executeQuery(startCursor, fetchSizes[idxFetch]);
		assertEquals(false, result.hasMoreResults());
		assertEquals(0, result.getResultList().size());

		System.out.println("Verifying backward query (again) against ordering of earlier result");
		// backquery should match existing data
		backResult = backquery.executeQuery( result.getBackwardCursor(), pkOrdering.size());
		assertEquals( true, backResult.hasMoreResults());
		assertEquals( pkOrdering.size(), backResult.getResultList().size());
		for ( int i = 0 ; i < pkOrdering.size() ; ++i ) {
			CommonFieldsBase cb = backResult.getResultList().get(i);
			((MyRelation) cb).print();
			assertEquals( pkOrdering.get(pkOrdering.size()-i-1), cb.getUri());
		}
		
		assertEquals( TOTAL_SIZE, len);
	}
}
