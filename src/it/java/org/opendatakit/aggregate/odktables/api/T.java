package org.opendatakit.aggregate.odktables.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;

@Ignore
public class T {

  public static final String tableId = "people";
  public static final String displayName = "\"people\"";

  public static final String savepoint_creator_1 = null;
  public static final String form_id_1 = null;
  public static final String locale_1 = "default";
  public static final String savepoint_timestamp_1 = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis()-1000);

  public static final String savepoint_creator_2 = "fred";
  public static final String form_id_2 = "ralph";
  public static final String locale_2 = "default";
  public static final String savepoint_timestamp_2 = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis()-2000);

  @SuppressWarnings("serial")
  public static final ArrayList<Column> columns = new ArrayList<Column>() {
    {
      add(T.Columns.column_name);
      add(T.Columns.column_age);
      add(T.Columns.column_weight);
    }
  };
  @SuppressWarnings("serial")
  public static final List<Row> rows = new ArrayList<Row>() {
    {
      add(Row.forInsert(T.Data.DYLAN.getId(), T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
          T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, T.Data.DYLAN.getValues()));
      add(Row.forInsert(T.Data.JOHN.getId(), T.form_id_2, T.locale_2, SavepointTypeManipulator.complete(),
          T.savepoint_timestamp_2, T.savepoint_creator_2, Scope.EMPTY_SCOPE, T.Data.JOHN.getValues()));
    }
  };

  public static class Columns {

    public static final String tableId = "my_table_id";
    public static final String name = "name";
    public static final String age = "age";
    public static final String weight = "weight";
    public static final Column column_name = new Column(tableId, name, name, "STRING", null, true);
    public static final Column column_age = new Column(tableId, age, age, "INTEGER", null, true);
    public static final Column column_weight = new Column(tableId, weight, weight, "INTEGER", null, true);
  }
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