package org.opendatakit.aggregate.odktables.entity;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.T;
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.entity.api.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class SerializationTest {

  private XStream xstream;

  @Before
  public void setUp() throws Exception {
    this.xstream = new XStream(new StaxDriver());
    this.xstream.processAnnotations(new Class[] { Column.class, Row.class, TableEntry.class,
        RowResource.class, TableDefinition.class, TableResource.class });
  }

  @Test
  public void testRow() {
    Row expected = Row.forUpdate("1", "5", T.Data.DYLAN.getValues());
    String xml = xstream.toXML(expected);
    System.out.println(xml);
    Row actual = (Row) xstream.fromXML(xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testColumn() {
    Column expected = new Column("name", ColumnType.STRING);
    String xml = xstream.toXML(expected);
    System.out.println(xml);
    Column actual = (Column) xstream.fromXML(xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testTableEntry() {
    TableEntry expected = new TableEntry("1", "2");
    String xml = xstream.toXML(expected);
    System.out.println(xml);
    TableEntry actual = (TableEntry) xstream.fromXML(xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testRowResource() {
    Map<String, String> values = T.Data.DYLAN.getValues();
    RowResource expected = new RowResource(Row.forInsert("1", null, values));
    expected.setSelfUri("http://localhost:8080/odktables/tables/1/rows/1");
    expected.setTableUri("http://localhost:8080/odktables/tables/1");

    String xml = xstream.toXML(expected);
    System.out.println(xml);
    RowResource actual = (RowResource) xstream.fromXML(xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testTableDefinition() {
    TableDefinition expected = new TableDefinition(T.columns);
    String xml = xstream.toXML(expected);
    System.out.println(xml);
    TableDefinition actual = (TableDefinition) xstream.fromXML(xml);
    assertEquals(expected, actual);
  }

  @Test
  public void testTableResource() {
    TableEntry entry = new TableEntry("1", "2");
    TableResource expected = new TableResource(entry);
    expected.setSelfUri("http://localhost:8080/odktables/tables/1");
    expected.setDataUri("http://localhost:8080/odktables/tables/1/rows");
    expected.setColumnsUri("http://localhost:8080/odktables/tables/1/columns");
    expected.setDiffUri("http://localhost:8080/odktables/tables/1/rows/diff");
    String xml = xstream.toXML(expected);
    System.out.println(xml);
    TableResource actual = (TableResource) xstream.fromXML(xml);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testDeserialize()
  {
    String xml = "<list><org.opendatakit.aggregate.odktables.entity.api.TableResource><tableId>people</tableId><dataEtag>0</dataEtag><selfUri>http://localhost:8888/odktables/tables/people</selfUri><columnsUri>http://localhost:8888/odktables/tables/people/columns</columnsUri><dataUri>http://localhost:8888/odktables/tables/people/rows</dataUri><diffUri>http://localhost:8888/odktables/tables/people/rows/diff</diffUri></org.opendatakit.aggregate.odktables.entity.api.TableResource></list>" ;
    List<TableResource> resources = (List<TableResource>)xstream.fromXML(xml);
        
  }
}
