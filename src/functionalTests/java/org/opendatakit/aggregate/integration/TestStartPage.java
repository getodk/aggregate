package org.opendatakit.aggregate.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.selenium.BaseWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Will be replaced by a pupeteer test suite
 */
@Ignore
public class TestStartPage {
  
  private static BaseWebDriver webDriver;

  @BeforeClass
  public static void setUp() throws Exception, Throwable {
    String username = System.getProperty("test.server.username");
    String password = System.getProperty("test.server.password");
    webDriver = new BaseWebDriver();
    webDriver.authenticateToSite(username, password); 
  }

  @Test
  public void testStartPageHasCorrectTitle() throws Exception {
    webDriver.get("Aggregate.html");

    // and verify that the Form Management tab appears...
    WebDriverWait wait = webDriver.webDriverWait();
    Boolean found = wait.until(webDriver.byContainingText(By.className("gwt-Label"), "Submissions"));

    assertTrue("Login did not progress to Aggregate.html page", found);
    assertEquals("ODK Aggregate", webDriver.getTitle());
    // assertEquals("Name", selenium.getText("//th[1]"));
    // assertTrue(selenium.isElementPresent("link=List Forms"));
  }

  @AfterClass
  public static void tearDown() throws Exception {
    webDriver.tearDown();
  }
}
