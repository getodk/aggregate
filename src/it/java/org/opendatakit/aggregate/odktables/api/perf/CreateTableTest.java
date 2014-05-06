package org.opendatakit.aggregate.odktables.api.perf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.api.T;
import org.opendatakit.aggregate.odktables.api.perf.AggregateSynchronizer;
import org.opendatakit.aggregate.odktables.api.perf.AggregateSynchronizer.InvalidAuthTokenException;
import org.opendatakit.aggregate.odktables.api.perf.PerfTest;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CreateTableTest implements PerfTest {
  private static final Log logger = LogFactory.getLog(CreateTableTest.class);

  private AggregateSynchronizer synchronizer;
  private int numCols;
  private int numRows;
  private String tableId;
  private String displayName;

  public CreateTableTest(AggregateSynchronizer synchronizer, int numCols, int numRows)
      throws InvalidAuthTokenException {
    this.synchronizer = synchronizer;
    this.numCols = numCols;
    this.numRows = numRows;
    this.tableId = "test_cols_" + numCols + "_rows_" + numRows;
    this.displayName = "\"Display test_cols_" + numCols + "_rows_" + numRows + "\"";
  }

  private String colName(int colNum) {
    return "column_" + colNum;
  }

  public boolean setUp() {
    return true;
  }

  public void run() {
    try {
      // create table
      ArrayList<Column> columns = new ArrayList<Column>();
      for (int i = 0; i < numCols; i++) {
        columns.add(new Column(tableId, colName(i), colName(i), "STRING", null, true));
      }
      synchronizer.createTable(tableId, null, columns);

      // insert rows
      List<Row> rows = Lists.newArrayList();
      for (int i = 0; i < numRows; i++) {
        Map<String, String> values = Maps.newHashMap();
        for (int j = 0; j < numCols; j++) {
          values.put(colName(j), "value_" + j);
        }
        Row row = Row.forInsert(UUID.randomUUID().toString(), T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
            T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, values);
        RowResource inserted = synchronizer.putRow(tableId, row);
        rows.add(inserted);
      }
      // update rows
      for (Row row : rows) {
        Map<String, String> values = row.getValues();
        for (int i = 0; i < numCols; i++) {
          values.put(colName(i), "new_value_" + i);
        }
        row.setValues(values);
        synchronizer.putRow(tableId, row);
      }
    } catch (HttpStatusCodeException e) {
      throw new RuntimeException(e.getMessage() + " " + e.getResponseBodyAsString(), e);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public void tearDown() {
    // delete table
    try {
      synchronizer.deleteTable(tableId);
    } catch (HttpStatusCodeException e) {
      logger.warn("message: " + e.getMessage() + ", body: " + e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      logger.warn(e.getMessage(), e);
      // ignore
    }
  }

  @Override
  public TestInfo getTestInfo() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Key.numCols, numCols);
    params.put(Key.numRows, numRows);
    return new TestInfo(this.getClass().getSimpleName(), tableId, displayName, params);
  }

  public class Key {
    public static final String numCols = "num_cols";
    public static final String numRows = "num_rows";
  }
}
