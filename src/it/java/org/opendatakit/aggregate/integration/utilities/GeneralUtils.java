package org.opendatakit.aggregate.integration.utilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.Properties;

public class GeneralUtils {
	private static Properties fileProps;
	static {
		fileProps = new Properties();
		try {
			String className = GeneralUtils.class.getSimpleName();
			File thisDirectory = new File(GeneralUtils.class.getResource(
					className + ".class").toURI()).getParentFile();
			File itRootDirectory = thisDirectory
			    .getParentFile().getParentFile().getParentFile()
			    .getParentFile().getParentFile().getParentFile();
			File propertiesFile = new File( new File( itRootDirectory, "resources"),
			                                "Integration.properties");
			Reader reader = new FileReader(propertiesFile);
			fileProps.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * First checks the System properties for {@code prop}. If not found, checks
	 * Integration.properties. If still not found, returns the provided
	 * {@code default}.
	 * 
	 * This makes it easy to run integration tests both through maven and
	 * individually (i.e. through eclipse) and have usable properties either
	 * way.
	 */
	public static String getProperty(String prop, String defaultValue) {
		return System.getProperty(prop,
				fileProps.getProperty(prop, defaultValue));
	}
}
