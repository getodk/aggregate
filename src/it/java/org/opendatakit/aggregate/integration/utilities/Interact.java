package org.opendatakit.aggregate.integration.utilities;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.selenium.BaseWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Interact {

	/**
	 * Upload a form to Aggregate. Assumes {@code driver} is logged in and at
	 * the Aggregate home page.
	 * @throws IOException 
	 */
	public static void uploadForm(BaseWebDriver webDriver, String pathToForm,
			String pathToMediaFile) throws IOException {
		Validate.notNull(webDriver);
		Validate.notEmpty(pathToForm);
		
	    webDriver.get("upload");
	    
	    Boolean found;
	    
	    // and verify that the upload page appears...
	    WebDriverWait wait = webDriver.webDriverWait();
	    found = wait.until(webDriver.byContainingText(By.tagName("h2"), "Upload one form into ODK Aggregate"));
	    assertTrue("Upload page did not render", found);
	    
	    File form = new File(pathToForm);
	    webDriver.findElement(By.id("form_def_file")).sendKeys(form.getCanonicalPath());
	    if ( pathToMediaFile != null && pathToMediaFile.length() != 0 ) {
	      File mediaFile = new File(pathToMediaFile);
	      webDriver.findElement(By.id("mediaFiles")).sendKeys(mediaFile.getCanonicalPath());
	    }
	    WebElement theUploadButton = webDriver.findElement(By.id("upload_form"));
	    if (theUploadButton == null) {
	      throw new IllegalStateException("could not find the upload button");
	    }
	    theUploadButton.submit();
	    try {
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	    // and wait for it to reload
	    wait = webDriver.webDriverWait();
	    found = wait.until(webDriver.byContainingText(By.tagName("p"), "Successful form upload."));
	    assertTrue("Upload was not successful or did not return", found);
	}

	/**
	 * Upload a submission to Aggregate. Assumes {@code driver} is logged in and
	 * at the Aggregate home page.
	 * @throws IOException 
	 */
	public static void uploadSubmission(BaseWebDriver webDriver,
			String pathToSubmission, String pathToMediaFile) throws IOException {
		Validate.notNull(webDriver);
		Validate.notEmpty(pathToSubmission);

		webDriver.get("submission");
      
      Boolean found;
      
      // and verify that the upload page appears...
      WebDriverWait wait = webDriver.webDriverWait();
      found = wait.until(webDriver.byContainingText(By.tagName("b"), "Upload one submission into ODK Aggregate"));
      assertTrue("Upload page did not render", found);
      
      File form = new File(pathToSubmission);
      webDriver.findElement(By.id("xml_submission_file")).sendKeys(form.getCanonicalPath());
      if ( pathToMediaFile != null && pathToMediaFile.length() != 0 ) {
        File mediaFile = new File(pathToMediaFile);
        webDriver.findElement(By.id("mediaFiles")).sendKeys(mediaFile.getCanonicalPath());
      }
      WebElement theUploadButton = webDriver.findElement(By.id("upload_submission"));
      if (theUploadButton == null) {
        throw new IllegalStateException("could not find the upload button");
      }
      theUploadButton.submit();
      try {
       Thread.sleep(1000L);
     } catch (InterruptedException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
      // and wait for it to reload
      wait = webDriver.webDriverWait();
      found = wait.until(webDriver.byContainingText(By.tagName("p"), "Successful submission upload."));
      assertTrue("Upload was not successful or did not return", found);
	}
}
