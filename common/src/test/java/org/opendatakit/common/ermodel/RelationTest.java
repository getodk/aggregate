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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

/**
 * Simple test case of the AbstractRelation class. Also an example of how to use
 * it.
 *
 * @author mitchellsundt@gmail.com
 *
 */

public class RelationTest {

  @Before
  public void setUp() throws Exception {
    CallingContext cc = TestContextFactory.getCallingContext();

    MyRelation rel = new MyRelation(cc);
    rel.dropRelation(cc); // drop it, in case prior test was messed up...
  }

  private static final long DATE_CONSTANT_VALUE = 5055505L;

  static class MyRelation extends Relation {

    static final DataField fieldStr = new DataField("THIS_IS_IT", DataField.DataType.STRING, true, 90L);
    static final DataField fieldInt = new DataField("SECOND_FIELD", DataField.DataType.INTEGER, true);
    static final DataField fieldDbl = new DataField("THIRD_FIELD", DataField.DataType.DECIMAL, true);
    static final DataField fieldApproxDbl = new DataField("THIRD_APPROX_FIELD", DataField.DataType.DECIMAL, true).asDoublePrecision(true);
    static final DataField fieldDate = new DataField("FOURTH_FIELD", DataField.DataType.DATETIME, true);
    static final DataField fieldBool = new DataField("FIFTH_FIELD", DataField.DataType.BOOLEAN, true);
    static final List<DataField> fields;
    static {
      fields = new ArrayList<DataField>();
      fields.add(fieldStr);
      fields.add(fieldInt);
      fields.add(fieldDbl);
      fields.add(fieldApproxDbl);
      fields.add(fieldDate);
      fields.add(fieldBool);
    }

    MyRelation(CallingContext cc) throws ODKDatastoreException {
      super(TableNamespace.EXTENSION, "__MY_TABLE", fields, cc);
    }
  }

  @Test
  public void testCase1() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    Date theDate = new Date(DATE_CONSTANT_VALUE);

    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);
    e.set(MyRelation.fieldStr, "This is a string");
    e.set(MyRelation.fieldDbl, 4.4);
    e.set(MyRelation.fieldInt, Integer.MIN_VALUE);
    e.set(MyRelation.fieldDate, theDate);
    e.set(MyRelation.fieldBool, (Boolean) null);
    e.put(cc);

    Query query;
    List<Entity> entities;

    query = rel.query("DbTable.testCase1.fieldDbl-lessThan", cc);
    query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.LESS_THAN, 5.0);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase1.fieldDbl-lessThanBigDecimal", cc);
    query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.LESS_THAN, new BigDecimal(5.0));
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase1.fieldDbl-lessThanWrappedBigDecimal", cc);
    query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.LESS_THAN,
        WrappedBigDecimal.fromDouble(5.0));
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase1.fieldDbl-greaterThan", cc);
    query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.GREATER_THAN, 4.0);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase1.fieldDbl-greaterThanBigDecimal", cc);
    query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.GREATER_THAN,
        new BigDecimal(4.0));
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase1.fieldDbl-greaterThanWrappedBigDecimal", cc);
    query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.GREATER_THAN,
        WrappedBigDecimal.fromDouble(4.0));
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase1.intMinValue", cc);
    query.addFilter(MyRelation.fieldInt.getName(), FilterOperation.EQUAL, Integer.MIN_VALUE);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    Entity eKey = rel.getEntity(e.getId(), cc);
    assertEquals(e.getId(), eKey.getId());
    Date eDate = e.getDate(MyRelation.fieldDate);
    Date eKeyDate = eKey.getDate(MyRelation.fieldDate);
    assertEquals(
        (eDate.getTime() / PersistConsts.MIN_DATETIME_RESOLUTION)
            * PersistConsts.MIN_DATETIME_RESOLUTION,
        (eKeyDate.getTime() / PersistConsts.MIN_DATETIME_RESOLUTION)
            * PersistConsts.MIN_DATETIME_RESOLUTION);

    assertNull(eKey.getBoolean(MyRelation.fieldBool));

    eKey.set(MyRelation.fieldInt, 40);
    eKey.set(MyRelation.fieldBool, true);
    MyRelation.putEntity(eKey, cc);

    Entity eNew = rel.getEntity(e.getId(), cc);
    assertEquals(eNew.getInteger(MyRelation.fieldInt), Integer.valueOf(40));
    assertTrue(eNew.getBoolean(MyRelation.fieldBool));

    e.put(cc);
    eKey = rel.getEntity(e.getId(), cc);
    assertEquals(eKey.getInteger(MyRelation.fieldInt), Integer.valueOf(Integer.MIN_VALUE));

    rel.dropRelation(cc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCase2() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);

    e.setAsString("secondField", "alpha");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCase3() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);

    e.setAsString("thirdField", "alpha");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCase4() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);

    e.setAsString("fourthField", "alpha");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCase5() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);

    e.set("thisIsIt", "12345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890");
  }

  @Test
  public void testCase6() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);

    e.setAsString("secondField", "5");
    e.setAsString("thirdField", "5.81");
    e.setAsString("thirdApproxField", "8.8");
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
    e.setAsString("thirdApproxField", "8.8");
    e2.setAsString("fourthField", (new Date(0)).toString());
    e2.set("thisIsIt", "another simple long string");

    e2.put(cc);

    Query query;
    List<Entity> entities;

    query = rel.query("DbTable.testCase7.fieldDbl-notEqual", cc);
    query.addFilter(MyRelation.fieldDbl.getName(), FilterOperation.NOT_EQUAL, 5.81);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e2.getId(), entities.get(0).getId());

    rel.dropRelation(cc);
  }

  @Test
  public void testCase8() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    Datastore ds = cc.getDatastore();
    if (ds instanceof org.opendatakit.common.persistence.engine.mysql.DatastoreImpl) {
      // MySQL does not support NaN, Infinity, -Infinity
      return;
    } else if (ds instanceof org.opendatakit.common.persistence.engine.sqlserver.DatastoreImpl) {
      // SqlServer does not support NaN, Infinity, -Infinity
      return;
    }
    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);

    e.setAsString("secondField", "5");
    e.setAsString("thirdField", "3.3");
    e.setAsString("thirdApproxField", Double.toString(Double.NaN));
    e.setAsString("fourthField", (new Date()).toString());
    e.set("thisIsIt", "a simple long string");

    e.put(cc);

    Entity e2 = rel.newEntity(cc);

    e2.setAsString("secondField", "6");
    e2.setAsString("thirdApproxField", "6.81");
    e2.setAsString("fourthField", (new Date(0)).toString());
    e2.set("thisIsIt", "another simple long string");

    e2.put(cc);

    Entity e3 = rel.newEntity(cc);

    e3.setAsString("secondField", "7");
    e3.setAsString("thirdApproxField", Double.toString(Double.POSITIVE_INFINITY));
    e3.setAsString("fourthField", (new Date(0)).toString());
    e3.set("thisIsIt", "another simple long string");

    e3.put(cc);

    Entity e4 = rel.newEntity(cc);

    e4.setAsString("secondField", "8");
    e4.setAsString("thirdField", "3.3");
    e4.setAsString("thirdApproxField", Double.toString(Double.NEGATIVE_INFINITY));
    e4.setAsString("fourthField", (new Date(0)).toString());
    e4.set("thisIsIt", "another simple long string");

    e4.put(cc);

    Query query;
    List<Entity> entities;

    query = rel.query("DbTable.testCase8.fieldApproxDbl-notEqual", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.NOT_EQUAL, Double.NaN);
    entities = query.execute();
    assertEquals(3, entities.size());

    query = rel.query("DbTable.testCase8.fieldApproxDbl-Equal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.EQUAL, Double.NaN);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase8.fieldApproxDbl-notEqual", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.NOT_EQUAL, 6.81);
    entities = query.execute();
    assertEquals(3, entities.size());

    query = rel.query("DbTable.testCase8.fieldApproxDbl-Equal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.EQUAL, 6.81);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e2.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase8.fieldDbl-notEqual", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.NOT_EQUAL,
        Double.POSITIVE_INFINITY);
    entities = query.execute();
    assertEquals(3, entities.size());

    query = rel.query("DbTable.testCase8.fieldApproxDbl-Equal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.EQUAL,
        Double.POSITIVE_INFINITY);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e3.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase8.fieldApproxDbl-notEqual", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.NOT_EQUAL,
        Double.NEGATIVE_INFINITY);
    entities = query.execute();
    assertEquals(3, entities.size());

    query = rel.query("DbTable.testCase8.fieldApproxDbl-Equal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.EQUAL,
        Double.NEGATIVE_INFINITY);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e4.getId(), entities.get(0).getId());

    rel.dropRelation(cc);
  }

  public void testCase8b() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);

    e.setAsString("secondField", "5");
    e.setAsString("thirdApproxField", "3.3");
    e.setAsString("fourthField", (new Date()).toString());
    e.set("thisIsIt", "a simple long string");

    e.put(cc);

    Entity e2 = rel.newEntity(cc);

    e2.setAsString("secondField", "6");
    e2.setAsString("thirdApproxField", "6.81");
    e2.setAsString("fourthField", (new Date(0)).toString());
    e2.set("thisIsIt", "another simple long string");

    e2.put(cc);

    Query query;
    List<Entity> entities;

    query = rel.query("DbTable.testCase8b.fieldApproxDbl-notEqual", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.NOT_EQUAL, 3.3);
    entities = query.execute();
    assertEquals(1, entities.size());

    query = rel.query("DbTable.testCase8b.fieldApproxDbl-Equal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.EQUAL, 3.3);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase8b.fieldApproxDbl-notEqual", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.NOT_EQUAL, 6.81);
    entities = query.execute();
    assertEquals(1, entities.size());

    query = rel.query("DbTable.testCase8b.fieldApproxDbl-Equal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.EQUAL, 6.81);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e2.getId(), entities.get(0).getId());

    rel.dropRelation(cc);
  }

  @Test
  public void testCase9() throws ODKDatastoreException {
    CallingContext cc = TestContextFactory.getCallingContext();

    MyRelation rel = new MyRelation(cc);
    rel = new MyRelation(cc);
    Entity e = rel.newEntity(cc);
    e.set(MyRelation.fieldStr, "This is a string");
    e.set(MyRelation.fieldApproxDbl, 4.4);
    e.set(MyRelation.fieldInt, Integer.MIN_VALUE);
    e.set(MyRelation.fieldDate, new Date(DATE_CONSTANT_VALUE));
    e.set(MyRelation.fieldBool, (Boolean) null);
    e.put(cc);

    Query query;
    List<Entity> entities;

    query = rel.query("DbTable.testCase9.fieldApproxDbl-lessThan", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.LESS_THAN, 5.0);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase9.fieldApproxDbl-lessThanBigDecimal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.LESS_THAN,
        new BigDecimal(5.0));
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase9.fieldApproxDbl-lessThanWrappedBigDecimal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.LESS_THAN,
        WrappedBigDecimal.fromDouble(5.0));
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase9.fieldApproxDbl-greaterThan", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.GREATER_THAN, 4.0);
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase9.fieldApproxDbl-greaterThanBigDecimal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.GREATER_THAN,
        new BigDecimal(4.0));
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    query = rel.query("DbTable.testCase9.fieldApproxDbl-greaterThanWrappedBigDecimal", cc);
    query.addFilter(MyRelation.fieldApproxDbl.getName(), FilterOperation.GREATER_THAN,
        WrappedBigDecimal.fromDouble(4.0));
    entities = query.execute();
    assertEquals(1, entities.size());
    assertEquals(e.getId(), entities.get(0).getId());

    rel.dropRelation(cc);
  }

}
