package org.opendatakit.aggregate.odktables.api.perf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.api.Error;
import org.opendatakit.aggregate.odktables.entity.api.Error.ErrorType;
import org.opendatakit.aggregate.odktables.entity.serialization.SimpleXMLSerializerForAggregate;
import org.simpleframework.xml.Serializer;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.common.collect.Maps;

public class MultipleUsersTest implements PerfTest {
  private static final Log logger = LogFactory.getLog(MultipleUsersTest.class);

  private AggregateSynchronizer synchronizer;
  private int numUsers;
  private int numCols;
  private int numRows;
  private String tableId;
  private String tableName;
  private Serializer serializer;

  public MultipleUsersTest(AggregateSynchronizer synchronizer, int numUsers, int numCols,
      int numRows) {
    this.synchronizer = synchronizer;
    this.numUsers = numUsers;
    this.numCols = numCols;
    this.numRows = numRows;
    this.tableId = UUID.randomUUID().toString();
    this.tableName = "test_users_" + numUsers + "_cols_" + numCols + "_rows_" + numRows;
    this.serializer = SimpleXMLSerializerForAggregate.getSerializer();
  }

  public class UserTest implements Runnable {
    public void run() {
      Random rand = new Random();
      int maxBackoff = 2;
      try {
        for (int i = 0; i < numRows; i++) {
          boolean tryAgain = true;
          do {
            try {
              // insert a row
              Map<String, String> values = Maps.newHashMap();
              for (int j = 0; j < numCols; j++) {
                values.put(colName(j), "value_" + j);
              }
              Row row = Row.forInsert(UUID.randomUUID().toString(), values);
              synchronizer.putRow(tableId, row);
              tryAgain = false;
            } catch (HttpStatusCodeException e) {
              checkLockTimeout(e);
              Thread.sleep((int) (rand.nextDouble() * maxBackoff));
              maxBackoff *= 2;
            }
          } while (tryAgain);
        }

      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

  private void checkLockTimeout(HttpStatusCodeException e) {
    String message = e.getMessage();
    String body = e.getResponseBodyAsString();
    try {
      Error error = serializer.read(Error.class, body);
      if (error.getType() != ErrorType.LOCK_TIMEOUT) {
        throw new RuntimeException("Error: " + error.toString(), e);
      }
    } catch (Exception e2) {
      logger.warn("Could not deserialize response body. Message: " + message + "\tBody: " + body,
          e2);
    }
  }

  private String colName(int colNum) {
    return "column_" + colNum;
  }

  @Override
  public boolean setUp() throws IOException {
    // create table
    List<Column> columns = new ArrayList<Column>();
    for (int i = 0; i < numCols; i++) {
      columns.add(new Column(colName(i), ColumnType.STRING));
    }
    synchronizer.createTable(tableId, tableName, columns, null);
    return true;
  }

  @Override
  public void run() {
    ExecutorService executor = Executors.newFixedThreadPool(numUsers);
    List<Future<?>> futures = new ArrayList<Future<?>>();
    for (int i = 0; i < numUsers; i++) {
      UserTest user = new UserTest();
      Future<?> future = executor.submit(user);
      futures.add(future);
    }
    try {
      for (Future<?> future : futures) {
        future.get();
      }
    } catch (Exception e) {
      shutdown(executor);
      throw new RuntimeException(e.getMessage(), e);
    }
    shutdown(executor);
  }

  private void shutdown(ExecutorService executor) {
    executor.shutdown();
    try {
      executor.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void tearDown() {
    while (true) {
      try {
        synchronizer.deleteTable(tableId);
        break;
      } catch (HttpStatusCodeException e) {
        logger.warn("message: " + e.getMessage() + ", body: " + e.getResponseBodyAsString(), e);
      } catch (Exception e) {
        logger.warn(e.getMessage(), e);
        // ignore
      }
    }
  }

  @Override
  public TestInfo getTestInfo() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(Key.numUsers, numUsers);
    parameters.put(Key.numCols, numCols);
    parameters.put(Key.numRows, numRows);
    return new TestInfo(this.getClass().getSimpleName(), tableId, tableName, parameters);
  }

  public class Key {
    public static final String numUsers = "num_users";
    public static final String numCols = "num_cols";
    public static final String numRows = "num_rows";
  }
}
