package org.opendatakit.aggregate.integration;

/* We are not currently set up to run these.
import java.io.File;

import com.thoughtworks.selenium.SeleneseTestCase;

public class TestUploadForm extends SeleneseTestCase 
{
	private static String TIMEOUT = "10000"; // ms

	private String formsDir;
	private String baseUrl;
	private int port;

	public void setUp() throws Exception
	{
		formsDir = System.getProperty("test.forms.dir");
		baseUrl = System.getProperty("project.artifactId");
		port = Integer.parseInt(System.getProperty("test.server.port"));
		// We should also test different browsers?
		setUp("http://localhost:" + port + "/" + baseUrl + "/", "*chrome");
	}

	public void testUploadForm() throws Exception
	{
		selenium.open("upload");
		selenium.waitForPageToLoad(TIMEOUT);
		File form = new File(formsDir + "/landUse.xml");
		selenium.type("form_def_file", form.getCanonicalPath());
		selenium.click("//input[@value='Upload']");
		selenium.waitForPageToLoad(TIMEOUT);
		// Need to check form was uploaded properly
	}
}
*/