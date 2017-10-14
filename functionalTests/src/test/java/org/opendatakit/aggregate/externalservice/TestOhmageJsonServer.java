package org.opendatakit.aggregate.externalservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.selenium.BaseWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * Will be replaced by a pupeteer test suite
 */
@Ignore
public class TestOhmageJsonServer {

  private static BaseWebDriver webDriver;
  private static String dylansForm;
  private static String dylansSubmissionsDir;

  @BeforeClass
  public static void setUpClass() throws Exception, Throwable {
    String username = System.getProperty("test.server.username");
    String password = System.getProperty("test.server.password");
    webDriver = new BaseWebDriver();
    webDriver.authenticateToSite(username, password);

    String formsDir = System.getProperty("test.forms.dir");
    dylansForm = formsDir + File.separatorChar + "dylan_form.xml";

    String submissionsDir = System.getProperty("test.submissions.dir");
    dylansSubmissionsDir = submissionsDir + File.separatorChar + "dylan_form";
  }

  @Before
  public void setUp() {
    webDriver.get("Aggregate.html");

    // and verify that the Form Management tab appears...
    WebDriverWait wait = webDriver.webDriverWait();
    Boolean found = wait.until(webDriver.byContainingText(By.className("gwt-Label"), "Info"));
    
    assertTrue("Login did not progress to Aggregate.html page", found);
    assertEquals("ODK Aggregate", webDriver.getTitle());
  }

  @Test
  public void uploadFormAndSubmissions() throws Exception {
    webDriver.uploadForm(dylansForm, null);

    for (int i = 1; i <= 9; i++) {
      String submissionFile = StringUtils.join(new String[] { dylansSubmissionsDir,
          "submission_" + i, String.format("submission_%d.xml", i) }, File.separatorChar);

      String submissionPic = StringUtils.join(
          new String[] { dylansSubmissionsDir, "submission_" + i, String.format("img_%d.jpg", i) },
          File.separatorChar);

      webDriver.uploadSubmission(submissionFile, submissionPic);
    }
  }

  @AfterClass
  public static void tearDown() throws Exception {
    webDriver.tearDown();
  }
}
