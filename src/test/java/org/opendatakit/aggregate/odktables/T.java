package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.junit.Ignore;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;
import org.opendatakit.aggregate.odktables.entity.Row;

@Ignore
public class T {

  public static final String tableId = "people";

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
      add(Row.forInsert(T.Data.DYLAN.getId(), null, T.Data.DYLAN.getValues()));
      add(Row.forInsert(T.Data.JOHN.getId(), null, T.Data.JOHN.getValues()));
    }
  };

  public static class Columns {
    public static final String name = "name";
    public static final String age = "age";
    public static final String weight = "weight";

    public static final Column column_name = new Column(name, ColumnType.STRING);
    public static final Column column_age = new Column(age, ColumnType.INTEGER);
    public static final Column column_weight = new Column(weight, ColumnType.INTEGER);
  }

  @RequiredArgsConstructor
  @Getter
  public static enum Data {
    DYLAN("1", "dylan", "23", "175"), JOHN("2", "john", "58", "200");

    private final String id;
    private final String name;
    private final String age;
    private final String weight;

    public Map<String, String> getValues() {
      val map = new HashMap<String, String>();
      map.put(Columns.name, name);
      map.put(Columns.age, age);
      map.put(Columns.weight, weight);
      return map;
    }
  }
}
