package org.opendatakit.aggregate.integration;

import com.thoughtworks.selenium.SeleneseTestCase;

public class TestStartPage extends SeleneseTestCase 
{
	private String baseUrl;
	private int port;

	public void setUp() throws Exception
	{
		baseUrl = System.getProperty("project.artifactId");
		port = Integer.parseInt(System.getProperty("test.server.port"));
		// We should also test different browsers?
		setUp("http://localhost:" + port + "/" + baseUrl + "/", "*chrome");
	}

	public void testStartPageHasCorrectTitle() throws Exception
	{
		selenium.open("forms");
		assertEquals("ODK AGGREGATE", selenium.getTitle());
		assertEquals("Name", selenium.getText("//th[1]"));
		assertTrue(selenium.isElementPresent("link=List Forms"));
	}
}
