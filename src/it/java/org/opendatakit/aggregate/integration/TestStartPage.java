package org.opendatakit.aggregate.integration;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.Wait;

@RunWith(org.junit.runners.JUnit4.class)
public class TestStartPage {
  private static long TIMEOUT_INTERVAL_MS = 120000L; // ms
  private static long RETRY_INTERVAL_MS = 100L; // ms

  private static String formsDir;
  private static String hostname;
  private static String baseUrl;
  private static String username = "aggregate";
  private static String password = "aggregate";
  private static int port;
  private static WebDriver driver;
  private static Selenium selenium;

  @BeforeClass
  public static void setUp() throws Exception {
    formsDir = System.getProperty("test.forms.dir");
    hostname = System.getProperty("test.server.hostname");
    baseUrl = System.getProperty("test.server.baseUrl");
    port = Integer.parseInt(System.getProperty("test.server.port"));
    // We should also test different browsers?
    driver = new FirefoxDriver();
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    selenium = new WebDriverBackedSelenium(driver, "http://" + username + ":" + password + "@"
        + hostname + ":" + port + baseUrl);

    selenium.open("local_login.html");
    Wait mainload = new Wait() {
      public boolean until() {
        return selenium.isTextPresent("Form Management");
      }
    };
    mainload.wait("Login did not progress to Aggregate.html page", TIMEOUT_INTERVAL_MS,
        RETRY_INTERVAL_MS);
  }

  @Test
  public void testStartPageHasCorrectTitle() throws Exception {
    selenium.open("Aggregate.html");
    Wait mainload = new Wait() {
      public boolean until() {
        return selenium.isTextPresent("Form Management");
      }
    };
    mainload.wait("Login did not progress to Aggregate.html page", TIMEOUT_INTERVAL_MS,
        RETRY_INTERVAL_MS);
    assertEquals("ODK Aggregate", selenium.getTitle());
    // assertEquals("Name", selenium.getText("//th[1]"));
    // assertTrue(selenium.isElementPresent("link=List Forms"));
  }

  @AfterClass
  public static void tearDown() throws Exception {
    selenium.close();
  }
}
