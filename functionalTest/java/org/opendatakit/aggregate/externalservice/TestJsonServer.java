package org.opendatakit.aggregate.externalservice;

import java.io.File;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.selenium.BaseWebDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class TestJsonServer {

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
    webDriver.get("/Aggregate.html");
    WebDriverWait wait = webDriver.webDriverWait();
    wait.until((Function<? super WebDriver, Boolean>) ExpectedConditions.titleIs("ODK Aggregate"));
  }

  public void uploadFormAndSubmissions() throws Exception {
    webDriver.uploadForm(dylansForm, null);

    for (int i = 0; i <= 9; i++) {
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
