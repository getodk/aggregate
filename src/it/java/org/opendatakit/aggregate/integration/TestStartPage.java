package org.opendatakit.aggregate.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

@RunWith(org.junit.runners.JUnit4.class)
public class TestStartPage {
  private static long TIMEOUT_INTERVAL_SECONDS = 120L; // seconds

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
    Random r = new Random();
    profile.setPreference(FirefoxProfile.PORT_PREFERENCE, r.nextInt(50) + 7000);
    driver = new FirefoxDriver(profile);
    //    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    driver.get(fullRootUrl);

    driver.get(fullRootUrl + "local_login.html");

    // wait for login process to complete...
    try {
      Thread.sleep(10000L);
    } catch (Exception e) {
    }

    // and verify that the Form Management tab appears...
    boolean found = (new WebDriverWait(driver, TIMEOUT_INTERVAL_SECONDS))
        .until(new Function<WebDriver, Boolean>() {

          @Override
          public Boolean apply(WebDriver driver) {
            List<WebElement> elements = driver.findElements(By.className("gwt-Label"));
            for ( WebElement el : elements ) {
              String txt = el.getText();
              if ( txt != null && txt.equals("Form Management") ) {
                return true;
              }
            }
            return false;
          }});
    
    assertTrue("Login did not progress to Aggregate.html page", found);
  }

  @Test
  public void testStartPageHasCorrectTitle() throws Exception {
    driver.get(fullRootUrl + "Aggregate.html");

    // wait for login process to complete...
    try {
      Thread.sleep(10000);
    } catch (Exception e) {
    }

    // and verify that the Form Management tab appears...
    boolean found = (new WebDriverWait(driver, TIMEOUT_INTERVAL_SECONDS))
        .until(new Function<WebDriver, Boolean>() {

          @Override
          public Boolean apply(WebDriver driver) {
            List<WebElement> elements = driver.findElements(By.className("gwt-Label"));
            for ( WebElement el : elements ) {
              String txt = el.getText();
              if ( txt != null && txt.equals("Form Management") ) {
                return true;
              }
            }
            return false;
          }});
    
    assertTrue("Login did not progress to Aggregate.html page", found);
    assertEquals("ODK Aggregate", driver.getTitle());
    // assertEquals("Name", selenium.getText("//th[1]"));
    // assertTrue(selenium.isElementPresent("link=List Forms"));
  }

  @AfterClass
  public static void tearDown() throws Exception {
    driver.quit();
    Thread.sleep(3000L);
  }
}
