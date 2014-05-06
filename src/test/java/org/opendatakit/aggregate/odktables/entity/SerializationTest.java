/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.odktables.entity;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.T;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

public class SerializationTest {

  private Serializer serializer;
  private StringWriter writer;

  @Before
  public void setUp() throws Exception {
    Registry registry = new Registry();
    Strategy strategy = new RegistryStrategy(registry);
    serializer = new Persister(strategy);

    writer = new StringWriter();
  }

  @Test
  public void testRowForUpdate() throws Exception {
    Row expected = Row.forUpdate("1", "5", T.form_id_2, T.locale_2,  SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_2, T.savepoint_creator_2, Scope.EMPTY_SCOPE, T.Data.DYLAN.getValues());
    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    Row actual = serializer.read(Row.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testRowInsert() throws Exception {
    Row expected = Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, T.Data.DYLAN.getValues());

    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);

    Row actual = serializer.read(Row.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testColumn() throws Exception {
//    Column expected = new Column("name", ColumnType.STRING);
//    serializer.write(expected, writer);
//    String xml = writer.toString();
//    System.out.println(xml);
//    Column actual = serializer.read(Column.class, xml);
//    assertEquals(expected, actual);
  }

  @Test
  public void testTableEntry() throws Exception {
    TableEntry expected = new TableEntry("1", "data2", "schema4");
    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    TableEntry actual = serializer.read(TableEntry.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testTableAcl() throws Exception {
    TableAcl expected = new TableAcl(TableRole.FILTERED_READER);
    expected.setScope(new Scope(Scope.Type.USER, "0"));
    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    TableAcl actual = serializer.read(TableAcl.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testRowResource() throws Exception {
    Map<String, String> values = T.Data.DYLAN.getValues();
    RowResource expected = new RowResource(Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, values));
    expected.setSelfUri("http://localhost:8080/odktables/tables/1/rows/1");
    expected.setTableUri("http://localhost:8080/odktables/tables/1");

    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    RowResource actual = serializer.read(RowResource.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testTableDefinition() throws Exception {
//    TableDefinition expected = new TableDefinition(T.tableName, T.columns, T.tableMetadata);
//    serializer.write(expected, writer);
//    String xml = writer.toString();
//    System.out.println(xml);
//    TableDefinition actual = serializer.read(TableDefinition.class, xml);
//    assertEquals(expected, actual);
  }

  @Test
  public void testTableResource() throws Exception {
    TableEntry entry = new TableEntry("1", "data2", "schema4");
    TableResource expected = new TableResource(entry);
    expected.setSelfUri("http://localhost:8080/odktables/tables/1");
    expected.setDataUri("http://localhost:8080/odktables/tables/1/rows");
    expected.setInstanceFilesUri("http://localhost:8080/odktables/tables/1/attachments");
    expected.setDefinitionUri("http://localhost:8080/odktables/tables/1/definition");
    expected.setDiffUri("http://localhost:8080/odktables/tables/1/rows/diff");
    expected.setAclUri("http://localhost:8080/odktables/tables/1/acl");
    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    TableResource actual = serializer.read(TableResource.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testTableAclResource() throws Exception {
    TableAcl acl = new TableAcl(TableRole.FILTERED_WRITER);
    acl.setScope(new Scope(Scope.Type.USER, "0"));
    TableAclResource expected = new TableAclResource(acl);
    expected.setSelfUri("http://localhost:8080/odktables/tables/1/acl/user/0");
    expected.setTableUri("http://localhost:8080/odktables/tables/1");
    expected.setAclUri("http://localhost:8080/odktables/tables/1/acl");
    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    TableAclResource actual = serializer.read(TableAclResource.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testListOfRowResource() throws Exception {
    ArrayList<RowResource> expected = new ArrayList<RowResource>();
    RowResource one = new RowResource(Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, T.Data.DYLAN.getValues()));
    one.setSelfUri("http://localhost/tables/1/rows/1");
    one.setTableUri("http://localhost/tables/1");
    RowResource two = new RowResource(Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, T.Data.JOHN.getValues()));
    two.setSelfUri("http://localhost/tables/1/rows/2");
    two.setTableUri("http://localhost/tables/1");
    expected.add(one);
    expected.add(two);
    RowResourceList rrl = new RowResourceList(expected);
    serializer.write(rrl, writer);
    String xml = writer.toString();
    System.out.println(xml);
    RowResourceList rrList = serializer.read(RowResourceList.class, xml);
    List<RowResource> actual = rrList.getEntries();
    assertEquals(expected, actual);
  }

}
