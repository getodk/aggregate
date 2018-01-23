/**
 * Copyright (C) 2011 University of Washington
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opendatakit.common.persistence.Query.Direction.ASCENDING;
import static org.opendatakit.common.persistence.Query.Direction.DESCENDING;
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


public class NewQueryResultTest {
  private static final int SET_SIZE = 3;
  private static final String[] STRINGS = new String[]{"A", "B", "C"};
  private static final double[] DOUBLES = new double[]{0.9, 1.9, 2.9};
  private static final String[] DATES = new String[]{"2009-01-30", "2009-02-30", "2009-03-30"};
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
    dropTable();
    table = createTestTable();
    writeLargeDataset();
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
  public void query_executeQuery_unique_pks() throws ODKDatastoreException {
    // This test focuses on the insertions... it should be here
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
    addSort(query, ASCENDING);

    QueryResult result = query.executeQuery(null, TEST_ROWS.length / 2);

    assertEquals(true, result.hasMoreResults());
    assertEquals(TEST_ROWS.length / 2, result.getResultList().size());
  }

  @Test
  public void query_executeQuery_forward_cursor_last_page() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeQuery_fetchSize", user);
    // We need to sort in order to execute a "cursor-style query"
    addSort(query, ASCENDING);

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
    Query query = ds.createQuery(table, "query_executeQuery_fetchSize", user);
    // We need to sort in order to execute a "cursor-style query"
    addSort(query, DESCENDING);

    QueryResult result = query.executeQuery(null, TEST_ROWS.length / 2);

    assertEquals(true, result.hasMoreResults());
    assertEquals(TEST_ROWS.length / 2, result.getResultList().size());
  }

  @Test
  public void query_executeQuery_backward_cursor_last_page() throws ODKDatastoreException {
    Query query = ds.createQuery(table, "query_executeQuery_fetchSize", user);
    // We need to sort in order to execute a "cursor-style query"
    addSort(query, DESCENDING);

    int firstPageSize = TEST_ROWS.length / 2;
    int lastPageSize = TEST_ROWS.length - 1;

    QueryResult firstPage = query.executeQuery(null, firstPageSize);
    QueryResumePoint startCursor = firstPage.getBackwardCursor();
    QueryResult lastPage = query.executeQuery(startCursor, lastPageSize);

    assertEquals(lastPageSize, lastPage.getResultList().size());
    assertFalse(lastPage.hasMoreResults());
  }

  private static synchronized TestTable createTestTable() throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getUserService().getDaemonAccountUser();
    TestTable tablePrototype = new TestTable(ds.getDefaultSchemaName());
    ds.assertRelation(tablePrototype, user); // may throw exception...
    // at this point, the prototype has become fully populated
    return tablePrototype; // set static variable only upon success...
  }

  private static void insertRowSets(TestTable table, TestRow row, CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    TestTable element = ds.createEntityUsingRelation(table, user);
    element.setStringField(TestTable.stringField, row.stringField);
    // This field will change for each loop iteration
    element.setLongField(TestTable.integerField, row.integerField.longValue());
    element.setNumericField(TestTable.doubleField, row.doubleField);
    element.setDateField(TestTable.dateField, row.dateField);
    element.setBooleanField(TestTable.booleanField, row.booleanField);

    ds.putEntity(element, user);
  }

  private static void writeLargeDataset() throws ODKEntityPersistException, ODKOverQuotaException {
    System.out.println("writing the large dataset");

    // write a lot of data...
    System.out.println("Inserting " + TEST_ROWS.length + " test rows");
    int n = 0;
    for (TestRow row : TEST_ROWS) {
      insertRowSets(table, row, cc);
      if (++n % 100 == 0)
        System.out.println("Inserted " + n + " rows");
    }
    System.out.println("done writing the large dataset");
  }

  private static void dropTable() throws ODKDatastoreException {
    TestTable table = createTestTable();
    System.out.println("dropping the large dataset");
    cc.getDatastore().dropRelation(table, cc.getCurrentUser());// drop it, in case prior test was messed up...
  }

  private void addSort(Query query, Query.Direction direction) {
    query.addSort(stringField, direction);
    query.addSort(integerField, direction);
    query.addSort(doubleField, direction);
    query.addSort(dateField, direction);
    query.addSort(booleanField, direction);
  }
}
