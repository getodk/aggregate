package org.opendatakit.aggregate.odktablesperf.api;

import java.util.Map;

public interface PerfTest extends Runnable {
  public boolean setUp() throws Exception;
  public void tearDown();
  public TestInfo getTestInfo();

  public class TestInfo {
    private String type;
    private String testId;
    private String testName;
    private Map<String, Object> parameters;

    public TestInfo(String type, String testId, String testName, Map<String, Object> parameters) {
      this.type = type;
      this.testId = testId;
      this.testName = testName;
      this.parameters = parameters;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getTestId() {
      return testId;
    }

    public void setTestId(String testId) {
      this.testId = testId;
    }

    public String getTestName() {
      return testName;
    }

    public void setTestName(String testName) {
      this.testName = testName;
    }

    public Map<String, Object> getParameters() {
      return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
      this.parameters = parameters;
    }
  }
}
