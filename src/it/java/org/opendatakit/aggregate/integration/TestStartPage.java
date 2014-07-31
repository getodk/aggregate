package org.opendatakit.aggregate.integration;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

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
  private static FirefoxDriver driver;
  private static String fullRootUrl;

  @BeforeClass
  public static void setUp() throws Exception {
    formsDir = System.getProperty("test.forms.dir");
    hostname = System.getProperty("test.server.hostname");
    baseUrl = System.getProperty("test.server.baseUrl");
    port = Integer.parseInt(System.getProperty("test.server.port"));
    fullRootUrl = "http://" + username + ":" + password + "@" + hostname + ":" + port + baseUrl;
    // We should also test different browsers?
    FirefoxProfile profile = new FirefoxProfile();
    profile.setEnableNativeEvents(false);
    profile.setPreference("network.negotiate-auth.trusteduris", hostname);
    driver = new FirefoxDriver(profile);
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    driver.get(fullRootUrl);

    driver.get(fullRootUrl + "local_login.html");

    // wait for login process to complete...
    try {
      Thread.sleep(10000);
    } catch (Exception e) {
    }

    // and verify that the Form Management tab appears...
    Wait mainload = new Wait() {
      public boolean until() {
        try {
          List<WebElement> elements = driver.findElements(By.className("gwt-Label"));
          for (WebElement e : elements) {
            if (e.getText().equals("Form Management"))
              return true;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
      }
    };

    mainload.wait("Login did not progress to Aggregate.html page", TIMEOUT_INTERVAL_MS,
        RETRY_INTERVAL_MS);
  }

  @Test
  public void testStartPageHasCorrectTitle() throws Exception {
    driver.get(fullRootUrl + "Aggregate.html");

    // wait for login process to complete...
    try {
      Thread.sleep(10000);
    } catch (Exception e) {
    }

    Wait mainload = new Wait() {
      public boolean until() {
        try {
          List<WebElement> elements = driver.findElements(By.className("gwt-Label"));
          for (WebElement e : elements) {
            if (e.getText().equals("Form Management"))
              return true;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
      }
    };
    mainload.wait("Login did not progress to Aggregate.html page", TIMEOUT_INTERVAL_MS,
        RETRY_INTERVAL_MS);
    assertEquals("ODK Aggregate", driver.getTitle());
    // assertEquals("Name", selenium.getText("//th[1]"));
    // assertTrue(selenium.isElementPresent("link=List Forms"));
  }

  @AfterClass
  public static void tearDown() throws Exception {
    driver.close();
  }
}
