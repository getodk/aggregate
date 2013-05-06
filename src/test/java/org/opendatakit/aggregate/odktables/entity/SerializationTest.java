package org.opendatakit.aggregate.odktables.entity;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.T;
import org.opendatakit.aggregate.odktables.entity.api.PropertiesResource;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.entity.api.TableAclResource;
import org.opendatakit.aggregate.odktables.entity.api.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
import org.opendatakit.aggregate.odktables.entity.serialization.ListConverter;
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
    ListConverter converter = new ListConverter(serializer);
    registry.bind(ArrayList.class, converter);

    writer = new StringWriter();
  }

  @Test
  public void testRowForUpdate() throws Exception {
    Row expected = Row.forUpdate("1", "5", T.Data.DYLAN.getValues());
    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    Row actual = serializer.read(Row.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testRowInsert() throws Exception {
    Row expected = Row.forInsert("1", T.Data.DYLAN.getValues());

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
    TableEntry expected = new TableEntry("1", "2", "3", "4");
    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    TableEntry actual = serializer.read(TableEntry.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testTableProperties() throws Exception {
//    TableProperties expected = new TableProperties("0", "1", "2");
//    serializer.write(expected, writer);
//    String xml = writer.toString();
//    System.out.println(xml);
//    TableProperties actual = serializer.read(TableProperties.class, xml);
//    assertEquals(expected, actual);
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
  public void testTablePropertiesIsNotStrict() throws Exception {
//    TableProperties expected = new TableProperties("0", "1", "2");
//    PropertiesResource resource = new PropertiesResource(expected);
//    resource.setSelfUri("http://localhost:8080/odktables/tables/1/properties");
//    resource.setTableUri("http://localhost:8080/odktables/tables/1");
//    serializer.write(resource, writer);
//    String xml = writer.toString();
//    System.out.println(xml);
//    TableProperties actual = serializer.read(TableProperties.class, xml);
//    assertEquals(expected, actual);
  }

  @Test
  public void testRowResource() throws Exception {
    Map<String, String> values = T.Data.DYLAN.getValues();
    RowResource expected = new RowResource(Row.forInsert("1", values));
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
    TableEntry entry = new TableEntry("1", "2", "3", "4");
    TableResource expected = new TableResource(entry);
    expected.setSelfUri("http://localhost:8080/odktables/tables/1");
    expected.setDataUri("http://localhost:8080/odktables/tables/1/rows");
    expected.setPropertiesUri("http://localhost:8080/odktables/tables/1/columns");
    expected.setDiffUri("http://localhost:8080/odktables/tables/1/rows/diff");
    expected.setAclUri("http://localhost:8080/odktables/tables/1/acl");
    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    TableResource actual = serializer.read(TableResource.class, xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testPropertiesResource() throws Exception {
//    TableProperties properties = new TableProperties("0", "1", "2");
//    PropertiesResource expected = new PropertiesResource(properties);
//    expected.setSelfUri("http://localhost:8080/odktables/tables/1/properties");
//    expected.setTableUri("http://localhost:8080/odktables/tables/1");
//    serializer.write(expected, writer);
//    String xml = writer.toString();
//    System.out.println(xml);
//    PropertiesResource actual = serializer.read(PropertiesResource.class, xml);
//    assertEquals(expected, actual);
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
  public void testListOfRow() throws Exception {
    List<Row> expected = new ArrayList<Row>();
    Row one = Row.forInsert("1", T.Data.DYLAN.getValues());
    Row two = Row.forInsert("1", T.Data.JOHN.getValues());
    expected.add(one);
    expected.add(two);

    serializer.write(expected, writer);
    String xml = writer.toString();
    System.out.println(xml);
    @SuppressWarnings("unchecked")
    List<Row> actual = (List<Row>) serializer.read(ArrayList.class, xml);
    assertEquals(expected, actual);
  }

}
