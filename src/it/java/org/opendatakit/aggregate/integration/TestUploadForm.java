package org.opendatakit.aggregate.integration;

import java.io.File;
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
public class TestUploadForm {
  private static long TIMEOUT_INTERVAL_MS = 30000L; // ms
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

    System.out.println(formsDir);
    System.out.println(hostname);
    System.out.println(baseUrl);
    System.out.println(port + "");
    
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
  public void testUploadForm() throws Exception {
    selenium.open("upload");
    Wait newload = new Wait() {
      public boolean until() {
        return selenium.isTextPresent("Upload one form into ODK Aggregate");
      }
    };
    newload.wait("Upload page did not render", TIMEOUT_INTERVAL_MS, RETRY_INTERVAL_MS);
    File form = new File(formsDir + "/landUse.xml");
    selenium.type("form_def_file", form.getCanonicalPath());
    selenium.click("//input[@value='Upload Form']");
    Wait reload = new Wait() {
      public boolean until() {
        return selenium.isTextPresent("Successful form upload.");
      }
    };
    reload.wait("Upload was not successful or did not return", TIMEOUT_INTERVAL_MS,
        RETRY_INTERVAL_MS);
    // Need to check form was uploaded properly
  }

  @AfterClass
  public static void tearDown() throws Exception {
    selenium.close();
  }
}
