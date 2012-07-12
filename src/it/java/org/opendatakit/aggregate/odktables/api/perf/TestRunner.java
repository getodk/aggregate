package org.opendatakit.aggregate.odktables.api.perf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.api.perf.AggregateSynchronizer.InvalidAuthTokenException;
import org.opendatakit.aggregate.odktables.api.perf.PerfTest.TestInfo;

import com.google.common.collect.Lists;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestRunner {

  private static final Log logger = LogFactory.getLog(TestRunner.class);

  private PerfTest test;
  private PrintWriter writer;
  private Gson gson;

  public TestRunner(PerfTest test, String outputFile) throws InvalidAuthTokenException,
      FileNotFoundException {
    this.test = test;
    this.writer = new PrintWriter(new FileOutputStream(outputFile, true), true);
    GsonBuilder builder = new GsonBuilder();
    builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    this.gson = builder.create();
  }

  public void run() {
    long startTime = System.currentTimeMillis();
    try {
      if (test.setUp()) {
        test.run();
      } else {
        logMessage(buildDidNotRunResults("test.setUp() returned false"));
      }
    } catch (Exception e) {
      logMessage(buildDidNotRunResults("ERROR: " + e.getMessage()));
      throw new RuntimeException(e);
    } finally {
      test.tearDown();
    }
    long stopTime = System.currentTimeMillis();
    long runTime = stopTime - startTime;
    logMessage(buildRanResults(runTime));
  }

  private Map<String, Object> buildDidNotRunResults(String message) {
    Map<String, Object> results = new HashMap<String, Object>();
    results.put(Key.ran, false);
    results.put(Key.message, message);
    return results;
  }

  private Map<String, Object> buildRanResults(long runTime) {
    Map<String, Object> results = new HashMap<String, Object>();
    results.put(Key.ran, true);
    results.put(Key.runTimeMs, runTime);
    return results;
  }

  private void logMessage(Map<String, Object> results) {
    TestResults testResults = new TestResults(test.getTestInfo(), results);
    String message = gson.toJson(testResults);
    writer.println(message);
    logger.info(message);
  }

  @Override
  protected void finalize() throws Throwable {
    writer.flush();
    writer.close();
    super.finalize();
  }

  public class TestResults {
    private TestInfo info;
    private Map<String, Object> results;

    public TestResults(TestInfo info, Map<String, Object> results) {
      this.info = info;
      this.results = results;
    }

    public TestInfo getInfo() {
      return info;
    }

    public Map<String, Object> getResults() {
      return results;
    }
  }

  public class Key {
    public static final String ran = "ran";
    public static final String runTimeMs = "run_time_ms";
    public static final String message = "message";
  }

  public static void main(String[] args) throws InvalidAuthTokenException, IOException,
      InterruptedException {
    String aggregateUrl = "http://odk-test.appspot.com";
    // System.out.println("Google OAuth2 token: ");
    Scanner scanner = new Scanner(System.in);
    // String token = scanner.nextLine();
    AggregateSynchronizer synchronizer = new AggregateSynchronizer(aggregateUrl, "");

    // List<Integer> numRowsValues = Lists.newArrayList(100, 1000);//, 10000);
    // for (int numRows : numRowsValues) {
    // CreateTableTest test = new CreateTableTest(synchronizer, 5, numRows);
    // TestRunner runner = new TestRunner(test, "table_size_test.log");
    // runner.run();
    // System.out.println("Running next test.");
    // }

    List<Integer> numUsersValues = Lists.newArrayList(1, 10, 20, 30, 40, 100);
    for (int numUsers : numUsersValues) {
      MultipleUsersTest test = new MultipleUsersTest(synchronizer, numUsers, 5, 5);
      TestRunner runner = new TestRunner(test, "multiple_users_test.log");
      runner.run();
      Thread.sleep(60000);
    }
  }
}
