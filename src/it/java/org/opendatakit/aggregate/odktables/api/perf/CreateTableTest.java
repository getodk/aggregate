package org.opendatakit.aggregate.odktables.api.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientWebException;
import org.opendatakit.aggregate.odktables.api.ColumnDefinition;
import org.opendatakit.aggregate.odktables.api.SyncRow;
import org.opendatakit.aggregate.odktables.api.T;
import org.opendatakit.aggregate.odktables.api.exceptions.InvalidAuthTokenException;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.common.collect.Lists;

public class CreateTableTest implements PerfTest {
  private static final Log logger = LogFactory.getLog(CreateTableTest.class);

  private AggregateSynchronizer synchronizer;
  private int numCols;
  private int numRows;
  private String tableId;
  private String displayName;

  public CreateTableTest(AggregateSynchronizer synchronizer, int numCols, int numRows)
      throws org.opendatakit.aggregate.odktables.api.exceptions.InvalidAuthTokenException {
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

  private TableResource rsc = null;
  
  public void run() {
    try {
      // create table
      ArrayList<Column> columns = new ArrayList<Column>();
      for (int i = 0; i < numCols; i++) {
        columns.add(new Column(colName(i), colName(i), "STRING", null));
      }
      rsc = synchronizer.createTable(tableId, null, columns);

      // insert rows
      ArrayList<SyncRow> syncRows = new ArrayList<SyncRow>();
      List<Row> rows = Lists.newArrayList();
      for (int i = 0; i < numRows; i++) {
        ArrayList<DataKeyValue> values = new ArrayList<DataKeyValue>();
        for (int j = 0; j < numCols; j++) {
          values.add(new DataKeyValue(colName(j), "value_" + j));
        }

        Row row = Row.forInsert(UUID.randomUUID().toString(), T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
            T.savepoint_timestamp_1, T.savepoint_creator_1, RowFilterScope.EMPTY_ROW_FILTER, values);
        rows.add(row);
        SyncRow sr = new SyncRow(row.getRowId(), row.getRowETag(), row.isDeleted(),
            row.getFormId(), row.getLocale(), row.getSavepointType(),
            row.getSavepointTimestamp(), row.getSavepointCreator(), row.getRowFilterScope(),
            row.getValues(), new ArrayList<ColumnDefinition>());
        syncRows.add(sr);
      }
      
      RowOutcomeList inserted = synchronizer.alterRows(rsc, syncRows);
      rsc.setDataETag(inserted.getDataETag());
      // update rows
      syncRows.clear();
      for (Row row : rows) {
        ArrayList<DataKeyValue> values = row.getValues();
        for (int i = 0; i < numCols; i++) {
          values.add(new DataKeyValue(colName(i), "new_value_" + i));
        }
        row.setValues(values);
        SyncRow sr = new SyncRow(row.getRowId(), row.getRowETag(), row.isDeleted(),
            row.getFormId(), row.getLocale(), row.getSavepointType(),
            row.getSavepointTimestamp(), row.getSavepointCreator(), row.getRowFilterScope(),
            row.getValues(), new ArrayList<ColumnDefinition>());
        syncRows.add(sr);
      }
      RowOutcomeList updated = synchronizer.alterRows(rsc, syncRows);
      rsc.setDataETag(updated.getDataETag());
    } catch (ClientWebException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage() + " " + e.getResponse().getStatusCode(), e);
    } catch (InvalidAuthTokenException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public void tearDown() {
    // delete table
    try {
      synchronizer.deleteTable(rsc);
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
