package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.opendatakit.aggregate.client.odktables.TableTypeClient;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.entity.Row;

@Ignore
public class T {

  public static final String user = "someone@gmail.com";
  public static final String group = "somegroup";
  public static final String tableId = "people";
  public static final String tableKey = "peopleKey";
  public static final String dbTableName = "peopleDbTableName";
  public static final String tableIdAccessControls = "someId";
  public static final String tableName = "people";
  public static final String tableMetadata = null;
  public static final TableTypeClient tableType = TableTypeClient.DATA;
  public static final String propertiesEtag = "propertiesEtag";
  @SuppressWarnings("serial")
  public static final List<OdkTablesKeyValueStoreEntry> kvsEntries = 
      new ArrayList<OdkTablesKeyValueStoreEntry>() {

    {
      kvsEntries.add(T.OdkTablesKeyValueStoreEntries.entryOne);
      kvsEntries.add(T.OdkTablesKeyValueStoreEntries.entryTwo);
    }
  };
  @SuppressWarnings("serial")
  public static final List<Column> columns = new ArrayList<Column>() {
    {
      add(T.Columns.column_name);
      add(T.Columns.column_age);
      add(T.Columns.column_weight);
    }
  };
  @SuppressWarnings("serial")
  public static final List<Row> rows = new ArrayList<Row>() {
    {
      add(Row.forInsert(T.Data.DYLAN.getId(), T.Data.DYLAN.getValues()));
      add(Row.forInsert(T.Data.JOHN.getId(), T.Data.JOHN.getValues()));
    }
  };
  
  public static class OdkTablesKeyValueStoreEntries {
    public static final OdkTablesKeyValueStoreEntry entryOne;
    public static final OdkTablesKeyValueStoreEntry entryTwo;
    public static final String entryOnePartition = "tablePartition";
    public static final String entryOneAspect = "tableAspect";
    public static final String entryOneKey = "theKey";
    public static final String entryOneType = "object";
    public static final String entryOneValue = "objectValue";
    public static final String entryTwoPartition = "customPartition";
    public static final String entryTwoAspect = "customAspect";
    public static final String entryTwoKey = "laClave";
    public static final String entryTwoType = "text";
    public static final String entryTwoValue = "thereIsNoPassword";
    static {
      entryOne = new OdkTablesKeyValueStoreEntry();
      entryOne.tableId = T.tableId;
      entryOne.partition = entryOnePartition;
      entryOne.aspect = entryOneAspect;
      entryOne.key = entryOneKey;
      entryOne.type = entryOneType;
      entryOne.value = entryOneValue;
      entryTwo = new OdkTablesKeyValueStoreEntry();
      entryTwo.tableId = T.tableId;
      entryTwo.partition = entryTwoPartition;
      entryTwo.aspect = entryTwoAspect;
      entryTwo.key = entryTwoKey;
      entryTwo.type = entryTwoType;
      entryTwo.value = entryTwoValue;
    }
  }

  public static class Columns {

    public static final String name = "name";
    public static final String elementKey_suffix = "_elementKey";
    public static final String elementName_suffix = "_elementName";
    public static final String age = "age";
    public static final String weight = "weight";
    public static final String columnType_String = "colTypeString";
    public static final String columnType_Int = "colTypeInt";
    public static final Column column_name = new Column(name, 
        name + elementKey_suffix, name + elementName_suffix, columnType_String,
        null, 1, null);
    public static final Column column_age = new Column(age, 
        age + elementKey_suffix, age + elementName_suffix, columnType_Int,
        null, 1, null);    
    public static final Column column_weight = new Column(weight, 
        weight + elementKey_suffix, weight + elementName_suffix, 
        columnType_Int, null, 1, null);  }

  public static enum Data {
    DYLAN("1", "dylan", "23", "175"),
    JOHN("2", "john", "58", "200");
    private final String id;
    private final String name;
    private final String age;
    private final String weight;

    public Map<String, String> getValues() {
      final java.util.HashMap<java.lang.String, java.lang.String> map = new HashMap<String, String>();
      map.put(Columns.name, name);
      map.put(Columns.age, age);
      map.put(Columns.weight, weight);
      return map;
    }

    @java.lang.SuppressWarnings("all")
    private Data(final String id, final String name, final String age, final String weight) {
      this.id = id;
      this.name = name;
      this.age = age;
      this.weight = weight;
    }

    @java.lang.SuppressWarnings("all")
    public String getId() {
      return this.id;
    }

    @java.lang.SuppressWarnings("all")
    public String getName() {
      return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public String getAge() {
      return this.age;
    }

    @java.lang.SuppressWarnings("all")
    public String getWeight() {
      return this.weight;
    }
  }
}