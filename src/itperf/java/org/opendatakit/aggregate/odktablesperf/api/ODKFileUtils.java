package org.opendatakit.aggregate.odktablesperf.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ODKFileUtils {
  private static final Log logger = LogFactory.getLog(ODKFileUtils.class);

  public static final String MD5_COLON_PREFIX = "md5:";

  public static final ObjectMapper mapper = new ObjectMapper();

  private static final String ASSETS_FOLDER_NAME = "assets";

  private static final String CSV_FOLDER_NAME = "csv";

  private static final String METADATA_FOLDER_NAME = "metadata";

  private static final String OUTPUT_FOLDER_NAME = "output";

  public static final String TABLES_FOLDER_NAME = "tables";

  private static final String LOGGING_FOLDER_NAME = "logging";

  private static final String FRAMEWORK_FOLDER_NAME = "framework";

  private static final String STALE_FRAMEWORK_FOLDER_NAME = "framework.old";

  private static final String STALE_FORMS_FOLDER_NAME = "forms.old";

  // under the tables directory...
  public static final String FORMS_FOLDER_NAME = "forms";
  public static final String INSTANCES_FOLDER_NAME = "instances";

  // under the metadata directory...
  private static final String WEB_DB_FOLDER_NAME = "webDb";
  private static final String GEO_CACHE_FOLDER_NAME = "geoCache";
  private static final String APP_CACHE_FOLDER_NAME = "appCache";

  // under the output directory...

  /** The name of the folder where the debug objects are written. */
  private static final String DEBUG_FOLDER_NAME = "debug";

  /**
   * Miscellaneous well-known file names
   */

  /** Filename for the top-level configuration file (in assets) */
  private static final String ODK_TABLES_INIT_FILENAME =
      "tables.init";

  /** Filename for the top-level configuration file (in assets) */
  private static final String ODK_TABLES_CONFIG_PROPERTIES_FILENAME =
      "tables.properties";

  /** Filename for the top-level configuration file (in assets) */
  private static final String ODK_SURVEY_CONFIG_PROPERTIES_FILENAME =
      "survey.properties";

  private static final String ODK_SURVEY_TEMP_CONFIG_PROPERTIES_FILENAME =
      "survey.temp";

  /** Filename for the ODK Tables home screen (in assets) */
  private static final String ODK_TABLES_HOME_SCREEN_FILE_NAME =
      "index.html";

  /**
   * Filename of the tables/tableId/properties.csv file
   * that holds all kvs properties for this tableId.
   */
  private static final String PROPERTIES_CSV = "properties.csv";

  /**
   * Filename of the tables/tableId/definition.csv file
   * that holds the table schema for this tableId.
   */
  private static final String DEFINITION_CSV = "definition.csv";

  private static final Set<String> topLevelExclusions;

  private static final Set<String> topLevelPlusTablesExclusions;

  /**
   * directories within an application that are inaccessible via the
   * getAsFile() API.
   */
  private static final Set<String> topLevelWebServerExclusions;
  static {

    TreeSet<String> temp;

    temp = new TreeSet<String>();
    temp.add(LOGGING_FOLDER_NAME);
    temp.add(METADATA_FOLDER_NAME);
    temp.add(OUTPUT_FOLDER_NAME);
    temp.add(STALE_FORMS_FOLDER_NAME);
    temp.add(STALE_FRAMEWORK_FOLDER_NAME);
    topLevelWebServerExclusions = Collections.unmodifiableSet(temp);

    /**
     * Going forward, we do not want to sync the framework directory
     * or any of the stale, local or output directories.
     *
     * Only the assets directory should be sync'd.
     */

    temp = new TreeSet<String>();
    temp.add(FRAMEWORK_FOLDER_NAME);
    temp.add(LOGGING_FOLDER_NAME);
    temp.add(METADATA_FOLDER_NAME);
    temp.add(OUTPUT_FOLDER_NAME);
    temp.add(STALE_FORMS_FOLDER_NAME);
    temp.add(STALE_FRAMEWORK_FOLDER_NAME);
    topLevelExclusions = Collections.unmodifiableSet(temp);

    temp = new TreeSet<String>();
    temp.add(FRAMEWORK_FOLDER_NAME);
    temp.add(LOGGING_FOLDER_NAME);
    temp.add(METADATA_FOLDER_NAME);
    temp.add(OUTPUT_FOLDER_NAME);
    temp.add(STALE_FORMS_FOLDER_NAME);
    temp.add(STALE_FRAMEWORK_FOLDER_NAME);
    temp.add(TABLES_FOLDER_NAME);
    topLevelPlusTablesExclusions = Collections.unmodifiableSet(temp);
  }

  public static Set<String> getDirectoriesToExcludeFromWebServer() {
    return topLevelWebServerExclusions;
  }

  public static Set<String> getDirectoriesToExcludeFromSync(boolean excludeTablesDirectory) {
    if ( excludeTablesDirectory ) {
      return topLevelPlusTablesExclusions;
    } else {
      return topLevelExclusions;
    }
  }


  public static String getOdkFolder() {
    String path = System.getProperty("test.sync.dir");
    return path;
  }

  public static String getAppFolder(String appName) {
    String path = getOdkFolder() + File.separator + appName;
    return path;
  }

  public static String getTablesFolder(String appName) {
    String path = getAppFolder(appName) + File.separator + TABLES_FOLDER_NAME;
    return path;
  }

  public static String getTablesFolder(String appName, String tableId) {
    String path;
    if (tableId == null || tableId.length() == 0) {
      throw new IllegalArgumentException("getTablesFolder: tableId is null or the empty string!");
    } else {
      if ( !tableId.matches("^\\p{L}\\p{M}*(\\p{L}\\p{M}*|\\p{Nd}|_)+$") ) {
        throw new IllegalArgumentException(
            "getFormFolder: tableId does not begin with a letter and contain only letters, digits or underscores!");
      }
      path = getTablesFolder(appName) + File.separator + tableId;
    }
    File f = new File(path);
    f.mkdirs();
    return f.getAbsolutePath();
  }

  public static String getTableDefinitionCsvFile(String appName, String tableId) {
    return getTablesFolder(appName, tableId) + File.separator + DEFINITION_CSV;
  }

  public static String getTablePropertiesCsvFile(String appName, String tableId) {
    return getTablesFolder(appName, tableId) + File.separator + PROPERTIES_CSV;
  }

  public static String getAssetsFolder(String appName) {
    String appFolder = ODKFileUtils.getAppFolder(appName);
    String result = appFolder + File.separator + ASSETS_FOLDER_NAME;
    return result;
  }

  public static String getAssetsCsvFolder(String appName) {
    String appFolder = ODKFileUtils.getAppFolder(appName);
    String result = appFolder + File.separator + ASSETS_FOLDER_NAME + File.separator + CSV_FOLDER_NAME;
    return result;
  }
  
  public static String getInstanceFolder(String appName, String tableId, String instanceId) {
    String path;
    if (instanceId == null || instanceId.length() == 0) {
      throw new IllegalArgumentException("getInstanceFolder: instanceId is null or the empty string!");
    } else {
      String instanceFolder = instanceId.replaceAll("[\\p{P}\\p{Z}]", "_");

      path = getTablesFolder(appName, tableId) + File.separator + INSTANCES_FOLDER_NAME + File.separator + instanceFolder;
    }

    File f = new File(path);
    f.mkdirs();
    return f.getAbsolutePath();
  }


  public static boolean createFolder(String folderPath)  {
    boolean made = true;
    File dir = new File(folderPath);
    if (!dir.exists()) {
      made = dir.mkdirs();
    }
    return made;
  }

  public static String getMd5Hash(String appName, File file) {
    return MD5_COLON_PREFIX + getNakedMd5Hash(appName, file);
  }

  public static String getNakedMd5Hash(String appName, File file) {
    try {
      // CTS (6/15/2010) : stream file through digest instead of handing
      // it the byte[]
      MessageDigest md = MessageDigest.getInstance("MD5");
      int chunkSize = 8192;

      byte[] chunk = new byte[chunkSize];

      // Get the size of the file
      long lLength = file.length();

      if (lLength > Integer.MAX_VALUE) {
        logger.error("File " + file.getName() + "is too large");
        return null;
      }

      int length = (int) lLength;

      InputStream is = null;
      is = new FileInputStream(file);

      int l = 0;
      for (l = 0; l + chunkSize < length; l += chunkSize) {
        is.read(chunk, 0, chunkSize);
        md.update(chunk, 0, chunkSize);
      }

      int remaining = length - l;
      if (remaining > 0) {
        is.read(chunk, 0, remaining);
        md.update(chunk, 0, remaining);
      }
      byte[] messageDigest = md.digest();

      BigInteger number = new BigInteger(1, messageDigest);
      String md5 = number.toString(16);
      while (md5.length() < 32)
        md5 = "0" + md5;
      is.close();
      return md5;

    } catch (NoSuchAlgorithmException e) {
      logger.error("MD5 " + e.getMessage());
      return null;

    } catch (FileNotFoundException e) {
      logger.error("No Cache File " + e.getMessage());
      return null;
    } catch (IOException e) {
      logger.error("Problem reading from file " + e.getMessage());
      return null;
    }

  }

  public static String getNakedMd5Hash(String appName, String contents) {
    try {
      // CTS (6/15/2010) : stream file through digest instead of handing
      // it the byte[]
      MessageDigest md = MessageDigest.getInstance("MD5");
      int chunkSize = 256;

      byte[] chunk = new byte[chunkSize];

      // Get the size of the file
      long lLength = contents.length();

      if (lLength > Integer.MAX_VALUE) {
        logger.error("Contents is too large");
        return null;
      }

      int length = (int) lLength;

      InputStream is = null;
      is = new ByteArrayInputStream(contents.getBytes(CharEncoding.UTF_8));

      int l = 0;
      for (l = 0; l + chunkSize < length; l += chunkSize) {
        is.read(chunk, 0, chunkSize);
        md.update(chunk, 0, chunkSize);
      }

      int remaining = length - l;
      if (remaining > 0) {
        is.read(chunk, 0, remaining);
        md.update(chunk, 0, remaining);
      }
      byte[] messageDigest = md.digest();

      BigInteger number = new BigInteger(1, messageDigest);
      String md5 = number.toString(16);
      while (md5.length() < 32)
        md5 = "0" + md5;
      is.close();
      return md5;

    } catch (NoSuchAlgorithmException e) {
      logger.error("MD5 " + e.getMessage());
      return null;

    } catch (FileNotFoundException e) {
      logger.error("No Cache File " + e.getMessage());
      return null;
    } catch (IOException e) {
      logger.error("Problem reading from file " + e.getMessage());
      return null;
    }

  }

  public static String extractAppNameFromPath(File path) {

    if ( path == null ) {
      return null;
    }

    File parent = path.getParentFile();
    File odkDir = new File(getOdkFolder());
    while (parent != null && !parent.equals(odkDir)) {
      path = parent;
      parent = path.getParentFile();
    }

    if ( parent == null ) {
      return null;
    } else {
      return path.getName();
    }
  }

  public static String asRelativePath(String appName, File fileUnderAppName) {
    // convert fileUnderAppName to a relative path such that if
    // we just append it to the AppFolder, we have a full path.
    File parentDir = new File(ODKFileUtils.getAppFolder(appName));

    ArrayList<String> pathElements = new ArrayList<String>();

    File f = fileUnderAppName;
    while (f != null && !f.equals(parentDir)) {
      pathElements.add(f.getName());
      f = f.getParentFile();
    }

    if (f == null) {
      throw new IllegalArgumentException("file is not located under this appName (" + appName + ")!");
    }

    StringBuilder b = new StringBuilder();
    for (int i = pathElements.size() - 1; i >= 0; --i) {
      String element = pathElements.get(i);
      b.append(element);
      if ( i != 0 ) {
        b.append(File.separator);
      }
    }
    return b.toString();

  }

  public static File asAppFile(String appName, String relativePath) {
    return new File(ODKFileUtils.getAppFolder(appName) + File.separator + relativePath);
  }

  public static File fromAppPath(String appPath) {
    String[] terms = appPath.split(File.separator);
    if (terms == null || terms.length < 1) {
      return null;
    }
    File f = new File(new File(getOdkFolder()), appPath);
    return f;
  }

  public static File getAsFile(String appName, String uriFragment) {
    // forward slash always...
    if ( uriFragment == null || uriFragment.length() == 0 ) {
      throw new IllegalArgumentException("Not a valid uriFragment: " +
          appName + "/" + uriFragment +
          " application or subdirectory not specified.");
    }

    File f = ODKFileUtils.fromAppPath(appName);
    if (f == null || !f.exists() || !f.isDirectory()) {
      throw new IllegalArgumentException("Not a valid uriFragment: " +
            appName + "/" + uriFragment + " invalid application.");
    }

    String[] segments = uriFragment.split("/");
    for ( int i = 0 ; i < segments.length ; ++i ) {
      String s = segments[i];
      f = new File(f, s);
    }
    return f;
  }

}
