package org.opendatakit.aggregate.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.selenium.BaseWebDriver;


/**
 * Will be replaced by a pupeteer test suite
 */
@Ignore
public class TestUploadForm {

  private static BaseWebDriver webDriver;
  private static String formsDir;

  @BeforeClass
  public static void setUp() throws Exception, Throwable {
    formsDir = System.getProperty("test.forms.dir");
    String username = System.getProperty("test.server.username");
    String password = System.getProperty("test.server.password");
    webDriver = new BaseWebDriver();
    webDriver.authenticateToSite(username, password); 
  }

  @Test
  public void testUploadForm() throws Exception {
    webDriver.uploadForm(formsDir + "/landUse.xml", null);
    webDriver.get("upload");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    webDriver.tearDown();  }
}
