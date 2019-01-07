/*
 * Copyright (C) 2018 Nafundi
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
package org.opendatakit.common.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opendatakit.common.persistence.Query.Direction.ASCENDING;
import static org.opendatakit.common.persistence.TestTable.booleanField;
import static org.opendatakit.common.persistence.TestTable.dateField;
import static org.opendatakit.common.persistence.TestTable.doubleField;
import static org.opendatakit.common.persistence.TestTable.integerField;
import static org.opendatakit.common.persistence.TestTable.stringField;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;


public class QueryResultTest {
  private static final int SET_SIZE = 3;
  private static final String[] STRINGS = new String[]{"A", "B", "C"};
  private static final double[] DOUBLES = new double[]{0.9, 1.9, 2.9};
  private static final String[] DATES = new String[]{"2009-01-30", "2009-02-28", "2009-03-30"};
  private static final Boolean[] BOOLEANS = new Boolean[]{true, false, null};
  private static TestRow[] TEST_ROWS = new TestRow[STRINGS.length * SET_SIZE * DOUBLES.length * DATES.length * BOOLEANS.length];

  static {
    int i = 0;
    for (String someString : STRINGS)
      for (int someInteger = 0; someInteger < SET_SIZE; someInteger++)
        for (Double someDouble : DOUBLES)
          for (String someDate : DATES)
            for (Boolean someBoolean : BOOLEANS)
              TEST_ROWS[i++] = new TestRow(someString, someInteger, someDouble, someDate, someBoolean);
  }

  private static TestTable table;
  private static CallingContext cc;
  private static Datastore ds;
  private static User user;

  @BeforeClass
  public static void oneTimeSetUp() throws Exception {
    cc = TestContextFactory.getCallingContext();
    ds = cc.getDatastore();
    user = cc.getCurrentUser();
    // Make sure that the table is empty before running any test on this class
    dropTable();
    // Create the table and insert some fixtures
    table = createTestTable();
    insertTestFixtures();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    dropTable();
  }

  @Test
  public void query_executeDistinctValueForDataField_string_field() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeDistinctValueForDataField_string_field", user);

    assertEquals(STRINGS.length, query.executeDistinctValueForDataField(stringField).size());
  }

  @Test
  public void query_executeDistinctValueForDataField_double_field() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeDistinctValueForDataField_double_field", user);

    assertEquals(DOUBLES.length, query.executeDistinctValueForDataField(doubleField).size());
  }

  @Test
  public void query_executeDistinctValueForDataField_boolean_field() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeDistinctValueForDataField_boolean_field", user);

    assertEquals(BOOLEANS.length, query.executeDistinctValueForDataField(booleanField).size());
  }

  @Test
  public void query_executeDistinctValueForDataField_integer_field() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeDistinctValueForDataField_integer_field", user);

    assertEquals(SET_SIZE, query.executeDistinctValueForDataField(integerField).size());
  }

  @Test
  public void query_executeDistinctValueForDataField_date_field() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeDistinctValueForDataField_date_field", user);

    assertEquals(DATES.length, query.executeDistinctValueForDataField(dateField).size());
  }

  @Test
  public void query_executeQuery_unique_pks() throws ODKDatastoreException {
    // This test focuses on the insertions... it shouldn't be here
    Query query = ds.createQuery(table, "query_executeQuery_unique_pks", user);

    List<? extends CommonFieldsBase> result = query.executeQuery();

    Set<String> pks = new HashSet<>();
    for (CommonFieldsBase cb : result)
      pks.add(cb.getUri());

    assertEquals(result.size(), pks.size());
  }

  @Test
  public void query_executeQuery_forward_cursor_first_page() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeQuery_forward_cursor_first_page", user);
    // We need to sort in order to execute a "cursor-style query"
    addSort(query);

    QueryResult result = query.executeQuery(null, TEST_ROWS.length / 2);

    assertEquals(true, result.hasMoreResults());
    assertEquals(TEST_ROWS.length / 2, result.getResultList().size());
  }

  @Test
  public void query_executeQuery_forward_cursor_last_page() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeQuery_forward_cursor_last_page", user);
    // We need to sort in order to execute a "cursor-style query"
    addSort(query);

    int firstPageSize = TEST_ROWS.length / 2;
    int lastPageSize = TEST_ROWS.length - firstPageSize;

    QueryResult firstPage = query.executeQuery(null, firstPageSize);
    QueryResumePoint startCursor = firstPage.getResumeCursor();
    QueryResult lastPage = query.executeQuery(startCursor, lastPageSize);

    assertEquals(lastPageSize, lastPage.getResultList().size());
    assertFalse(lastPage.hasMoreResults());
  }

  @Test
  public void query_executeQuery_backward_cursor_first_page() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeQuery_backward_cursor_first_page", user);
    // We need to sort in order to execute a "cursor-style query"
    addSort(query);

    QueryResult result = query.executeQuery(null, TEST_ROWS.length / 2);

    assertEquals(true, result.hasMoreResults());
    assertEquals(TEST_ROWS.length / 2, result.getResultList().size());
  }

  @Test
  public void query_executeQuery_backward_cursor_last_page() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeQuery_backward_cursor_last_page", user);
    // We need to sort in order to execute a "cursor-style query"
    addSort(query);

    int firstPageSize = TEST_ROWS.length / 2;
    // The backward cursor pivots on the first row, therefore,
    // pending rows amount is the total of rows minus the one
    // row we're pivoting on
    int lastPageSize = TEST_ROWS.length - 1;

    QueryResult firstPage = query.executeQuery(null, firstPageSize);
    QueryResumePoint startCursor = firstPage.getBackwardCursor();
    QueryResult lastPage = query.executeQuery(startCursor, lastPageSize);

    assertEquals(lastPageSize, lastPage.getResultList().size());
    assertFalse(lastPage.hasMoreResults());
  }

  private static synchronized TestTable createTestTable() throws ODKDatastoreException {
    System.out.println("Creating the test table");
    TestTable tablePrototype = new TestTable(ds.getDefaultSchemaName());
    ds.assertRelation(tablePrototype, user);
    return tablePrototype;
  }

  private static void insertRowSets(TestTable table, TestRow row) throws ODKEntityPersistException, ODKOverQuotaException {
    TestTable element = ds.createEntityUsingRelation(table, user);
    element.setStringField(TestTable.stringField, row.stringField);
    element.setLongField(TestTable.integerField, row.integerField.longValue());
    element.setNumericField(TestTable.doubleField, row.doubleField);
    element.setDateField(TestTable.dateField, row.dateField);
    element.setBooleanField(TestTable.booleanField, row.booleanField);

    ds.putEntity(element, user);
  }

  private static void insertTestFixtures() throws ODKEntityPersistException, ODKOverQuotaException {
    System.out.println("Inserting " + TEST_ROWS.length + " test rows");
    int n = 0;
    for (TestRow row : TEST_ROWS) {
      insertRowSets(table, row);
      if (++n % 100 == 0)
        System.out.println("Inserted " + n + " rows");
    }
    System.out.println("Done writing the large dataset");
  }

  private static void dropTable() throws ODKDatastoreException {
    System.out.println("Dropping the test table");
    TestTable table = createTestTable();
    cc.getDatastore().dropRelation(table, cc.getCurrentUser());
  }

  private void addSort(Query query) {
    query.addSort(stringField, ASCENDING);
    query.addSort(integerField, ASCENDING);
    query.addSort(doubleField, ASCENDING);
    query.addSort(dateField, ASCENDING);
    query.addSort(booleanField, ASCENDING);
  }
}
