package org.opendatakit.aggregate.integration;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Random;

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
public class TestUploadForm {
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
    Random r = new Random();
    profile.setPreference(FirefoxProfile.PORT_PREFERENCE, r.nextInt(50) + 7000);
    driver = new FirefoxDriver(profile);
    driver.manage().timeouts().implicitlyWait(TIMEOUT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    driver.get(fullRootUrl);

    System.out.println(formsDir);
    System.out.println(hostname);
    System.out.println(baseUrl);
    System.out.println(port + "");

    // this may not work...
    driver.get(fullRootUrl + "local_login.html");

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
  }

  @Test
  public void testUploadForm() throws Exception {
    driver.get(fullRootUrl + "upload");
    Wait newload = new Wait() {
      public boolean until() {
        try {
          List<WebElement> elements = driver.findElements(By.tagName("h2"));
          for (WebElement e : elements) {
            if (e.getText().equals("Upload one form into ODK Aggregate"))
              return true;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
      }
    };
    newload.wait("Upload page did not render", TIMEOUT_INTERVAL_MS, RETRY_INTERVAL_MS);
    File form = new File(formsDir + "/landUse.xml");
    driver.findElementById("form_def_file").sendKeys(form.getCanonicalPath());
    WebElement theUploadButton = driver.findElement(By.id("upload_form"));
    if (theUploadButton == null) {
      throw new IllegalStateException("could not find the upload button");
    }
    theUploadButton.submit();
    Thread.sleep(1000);
    // and wait for it to reload
    Wait reload = new Wait() {
      public boolean until() {
        try {
          List<WebElement> ps = driver.findElements(By.tagName("p"));
          for (WebElement e : ps) {
            if (e.getText().equals("Successful form upload."))
              return true;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
      }
    };
    reload.wait("Upload was not successful or did not return", TIMEOUT_INTERVAL_MS,
        RETRY_INTERVAL_MS);
    // Need to check form was uploaded properly
  }

  @AfterClass
  public static void tearDown() throws Exception {
    driver.quit();
  }
}
