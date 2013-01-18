package org.opendatakit.aggregate.externalservice;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opendatakit.aggregate.integration.utilities.GeneralUtils;
import org.opendatakit.aggregate.integration.utilities.Interact;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestJsonServer {

	private static WebDriver driver;
	private static WebDriverWait wait;
	private static String aggregateURL;
	private static String dylansForm;
	private static String dylansSubmissionsDir;

	@BeforeClass
	public static void setUpClass() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(5, TimeUnit.SECONDS);
		wait = new WebDriverWait(driver, 10);

		String hostname = GeneralUtils.getProperty("test.server.hostname",
				"localhost");
		int port = Integer.parseInt(GeneralUtils.getProperty(
				"test.server.port", "8888"));
		String superUserName = GeneralUtils.getProperty("test.superusername",
				"aggregate");
		String superUserPassword = GeneralUtils.getProperty(
				"test.superuserpassword", "aggregate");

		String formsDir = GeneralUtils.getProperty("test.forms.dir",
				"../../../../../src/it/testfiles/forms");
		dylansForm = formsDir + File.separatorChar + "dylan_form.xml";

		String submissionsDir = GeneralUtils.getProperty(
				"test.submissions.dir",
				"../../../../../src/it/testfiles/submissions");
		dylansSubmissionsDir = submissionsDir + File.separatorChar
				+ "dylan_form";

		aggregateURL = String.format("http://%s:%d", hostname, port);

		driver.get(String.format("http://%s:%s@%s:%d/local_login.html",
				superUserName, superUserPassword, hostname, port));
	}

	@Before
	public void setUp() {
		driver.get(aggregateURL + "/Aggregate.html");
		wait.until(ExpectedConditions.titleIs("ODK Aggregate"));
	}

	public void uploadFormAndSubmissions() throws Exception {
		Interact.uploadForm(driver, dylansForm, (String[]) null);
		
		for (int i = 0; i <= 9; i++) {
			String submissionFile = StringUtils.join(
					new String[] { dylansSubmissionsDir, "submission_" + i,
							String.format("submission_%d.xml", i) },
					File.separatorChar);
			
			String submissionPic = StringUtils.join(
					new String[] { dylansSubmissionsDir, "submission_" + i,
							String.format("img_%d.jpg", i) },
					File.separatorChar);
			
			Interact.uploadSubmission(driver, submissionFile, submissionPic);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		driver.close();
	}
}
