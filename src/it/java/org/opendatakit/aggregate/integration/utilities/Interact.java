package org.opendatakit.aggregate.integration.utilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Interact {

	/**
	 * Upload a form to Aggregate. Assumes {@code driver} is logged in and at
	 * the Aggregate home page.
	 */
	public static void uploadForm(WebDriver driver, String pathToForm,
			String... pathToMediaFiles) {
		Validate.notNull(driver);
		Validate.notEmpty(pathToForm);

		driver.findElement(
				By.xpath("//table[@id='mainNav']/tbody/tr/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td[2]/div/div/div"))
				.click();
		driver.findElement(
				By.xpath("//table[@id='mainNav']/tbody/tr[2]/td/div/div[2]/table/tbody/tr/td/table/tbody/tr/td[2]/div/div"))
				.click();
		driver.findElement(
				By.cssSelector("div.gwt-TabPanelBottom > div > table > tbody > tr > td > button.gwt-Button"))
				.click();

		WebElement iframe = driver.findElement(By
				.cssSelector("iframe.uploadFrame"));
		driver.switchTo().frame(iframe);

		WebElement formDefFile = driver.findElement(By.id("form_def_file"));
		formDefFile.clear();
		formDefFile.sendKeys(pathToForm);
		WebElement mediaFiles = driver.findElement(By.id("mediaFiles"));
		mediaFiles.clear();
		if (pathToMediaFiles != null)
			mediaFiles.sendKeys(StringUtils.join(pathToMediaFiles, ','));
		driver.findElement(By.name("button")).click();

		driver.switchTo().defaultContent();
		driver.findElement(By.xpath("//div/div/button")).click();
	}

	/**
	 * Upload a submission to Aggregate. Assumes {@code driver} is logged in and
	 * at the Aggregate home page.
	 */
	public static void uploadSubmission(WebDriver driver,
			String pathToSubmission, String... pathToMediaFiles) {
		Validate.notNull(driver);
		Validate.notEmpty(pathToSubmission);

		driver.findElement(
				By.xpath("//table[@id='mainNav']/tbody/tr/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td[2]/div/div/div"))
				.click();
		driver.findElement(
				By.xpath("//table[@id='mainNav']/tbody/tr[2]/td/div/div[2]/table/tbody/tr/td/table/tbody/tr/td[4]/div/div"))
				.click();
		driver.findElement(
				By.xpath("//table[@id='submission_admin_bar']/tbody/tr/td[2]/button"))
				.click();

		WebElement iframe = driver.findElement(By
				.cssSelector("iframe.uploadFrame"));
		driver.switchTo().frame(iframe);

		driver.findElement(By.id("xml_submission_file")).clear();
		driver.findElement(By.id("xml_submission_file")).sendKeys(
				pathToSubmission);
		driver.findElement(By.id("mediaFiles")).clear();
		if (pathToMediaFiles != null) {
			driver.findElement(By.id("mediaFiles")).sendKeys(
					StringUtils.join(pathToMediaFiles, ','));
		}
		driver.findElement(By.name("button")).click();

		driver.switchTo().defaultContent();
		driver.findElement(By.xpath("//div/div/button")).click();
	}
}
