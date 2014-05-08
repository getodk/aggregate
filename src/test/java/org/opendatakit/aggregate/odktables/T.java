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

package org.opendatakit.aggregate.odktables;

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

  public static class Columns {

    public static final String name = "name";
    public static final String elementKey_suffix = "_elementKey";
    public static final String elementName_suffix = "_elementName";
    public static final String age = "age";
    public static final String weight = "weight";
    public static final String columnType_String = "colTypeString";
    public static final String columnType_Int = "colTypeInt";
    public static final Column column_name = new Column(name, name + elementKey_suffix, name
        + elementName_suffix, "NONE", null, true);
    public static final Column column_age = new Column(age, age + elementKey_suffix, age
        + elementName_suffix, "NONE", null, true);
    public static final Column column_weight = new Column(weight, weight + elementKey_suffix,
        weight + elementName_suffix, "NONE", null, true);
  }

  public static final String savepoint_creator_1 = null;
  public static final String form_id_1 = null;
  public static final String locale_1 = "default";
  public static final String savepoint_timestamp_1 = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis()-1000);

  public static final String savepoint_creator_2 = "fred";
  public static final String form_id_2 = "ralph";
  public static final String locale_2 = "default";
  public static final String savepoint_timestamp_2 = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis()-2000);

  public static enum Data {
    DYLAN("1", "dylan", "23", "175"), JOHN("2", "john", "58", "200");
    private final String id;
    private final String name;
    private final String age;
    private final String weight;

    public Map<String, String> getValues() {
      final java.util.HashMap<java.lang.String, java.lang.String> map = new HashMap<String, String>();
      map.put(Columns.column_name.getElementKey(), name);
      map.put(Columns.column_age.getElementKey(), age);
      map.put(Columns.column_weight.getElementKey(), weight);
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

  public static final String user = "someone@gmail.com";
  public static final String group = "somegroup";
  public static final String appId = "app1";
  public static final String tableId = "people";
  public static final String tableMetadata = null;

  public static final List<Column> columns = new ArrayList<Column>();
  public static final List<Column> columns2 = new ArrayList<Column>();

  public static final List<Row> rows = new ArrayList<Row>();

  static {
    columns.add(T.Columns.column_name);
    columns.add(T.Columns.column_age);
    columns.add(T.Columns.column_weight);
    columns2.add(T.Columns.column_name);
    columns2.add(T.Columns.column_age);
    rows.add(Row.forInsert(T.Data.DYLAN.getId(), T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, T.Data.DYLAN.getValues()));
    rows.add(Row.forInsert(T.Data.JOHN.getId(), T.form_id_2, T.locale_2, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_2, T.savepoint_creator_2, Scope.EMPTY_SCOPE, T.Data.JOHN.getValues()));
  };
}