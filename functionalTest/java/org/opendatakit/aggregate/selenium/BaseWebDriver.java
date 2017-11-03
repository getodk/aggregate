package org.opendatakit.aggregate.selenium;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.opendatakit.aggregate.integration.utilities.Interact;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.auth.AuthType;

public class BaseWebDriver {

  private static long TIMEOUT_INTERVAL_SECONDS = 120L; // seconds

  private BrowserMobProxy proxy;
  private String hostname;
  private String baseUrl;
  private int port;
  private ChromeDriver driver;
  private String fullRootUrl;

  public BaseWebDriver() {
  }

  public void authenticateToSite(String username, String password) throws Exception, Throwable {
    hostname = System.getProperty("test.server.hostname");
    baseUrl = System.getProperty("test.server.baseUrl");
    port = Integer.parseInt(System.getProperty("test.server.port"));

    try {
      proxy = new BrowserMobProxyServer();
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
    proxy.start(0);

    proxy.stopAutoAuthorization(hostname);
    if (username != null && username.length() != 0) {
      proxy.autoAuthorization(hostname, username, password, AuthType.BASIC);
    }
    /*
     * 
     * String base64UserPass = java.util.Base64.getEncoder()
     * .encodeToString((username + ":" +
     * password).getBytes(Charsets.UTF_8)).trim(); final String authHeader =
     * "\nAuthorization: Basic " + base64UserPass;
     * 
     * proxy.addRequestFilter(new RequestFilter() {
     * 
     * @Override public HttpResponse filterRequest(HttpRequest request,
     * HttpMessageContents contents, HttpMessageInfo messageInfo) { byte[]
     * content = contents.getBinaryContents(); int idx = 0; while ( idx <
     * content.length-1 ) { if ( content[idx] == '\n' && content[idx+1] == '\n'
     * ) { break; } } ByteArrayOutputStream bas = new ByteArrayOutputStream();
     * bas.write(content, 0, idx); try {
     * bas.write(authHeader.getBytes(Charsets.UTF_8)); } catch (IOException e) {
     * e.printStackTrace(); } bas.write(content, idx, content.length);
     * 
     * contents.setBinaryContents(bas.toByteArray());
     * 
     * // in the request filter, you can return an HttpResponse object to
     * "short-circuit" the request return null; } });
     */

    fullRootUrl = "http://" + hostname + ":" + port + baseUrl;

    // get the Selenium proxy object
    Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

    // configure it as a desired capability
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

    // start the browser up
    try {
      driver = new ChromeDriver(capabilities);
    } catch ( Throwable t ) {
      t.printStackTrace();
      throw t;
    }
    
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
    driver.manage().timeouts().setScriptTimeout(15, TimeUnit.SECONDS);

    try {
      driver.get(fullRootUrl);
    } catch ( Throwable t) {
      t.printStackTrace();
      throw t;
    }

    System.out.println(hostname);
    System.out.println(baseUrl);
    System.out.println(port + "");

    // this may not work...
    driver.get(fullRootUrl + "local_login.html");

    // and verify that the Form Management tab appears...
    WebDriverWait wait = new WebDriverWait(driver, TIMEOUT_INTERVAL_SECONDS);
    Boolean found = wait
        .until(this.byContainingText(By.className("gwt-Label"), "Submissions"));

    assertTrue("Login did not progress to Aggregate.html page", found);
  }

  public java.util.function.Function<? super WebDriver, Boolean> byContainingText(final By by,
      final String text) {
    
    ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {

      @Override
      public Boolean apply(WebDriver driver) {
        List<WebElement> elements = driver.findElements(by);
        for (WebElement el : elements) {
          String txt = el.getText();
          if (txt != null && txt.equals(text)) {
            return true;
          }
        }
        return false;
      }
    };

    return (Function<? super WebDriver, Boolean>) condition;
  }

  public void get(String urlFragment) {
    if (urlFragment != null) {
      driver.get(fullRootUrl + urlFragment);
    } else {
      driver.get(fullRootUrl);
    }
  }

  public void uploadForm(String form, String mediaFile) throws IOException {
    Interact.uploadForm(this, form, mediaFile);
  }

  public void uploadSubmission(String submissionFile, String submissionPic) throws IOException {
    Interact.uploadSubmission(this, submissionFile, submissionPic);
  }

  public WebElement findElement(By by) {
    return driver.findElement(by);
  }

  public WebDriverWait webDriverWait() {
    return new WebDriverWait(driver, TIMEOUT_INTERVAL_SECONDS);
  }

  public String getTitle() {
    return driver.getTitle();
  }

  public void tearDown() throws Exception {
    driver.quit();
    Thread.sleep(500L);
    proxy.stop();
    Thread.sleep(500L);
  }

}
