package org.opendatakit.aggregate.odktables.entity;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.client.exception.ImportFromCSVExceptionClient;
import org.opendatakit.aggregate.client.odktables.ColumnClient;

/**
 * TableProperties has a metadata field that is a string. This is an object 
 * that can be used to represent that string when more needs to be done with
 * it than simply passing it around. It is really intended only for creation
 * from the server. Using it in other ways may lead to data growing stale
 * and having conflicting information about the state of the table. 
 * User beware.
 * <p>
 * Metadata is the home for various information about the table. Most of the 
 * information is about how the table exists on the phone, including things
 * like the name of the table backing it, as well as column order, etc. Column
 * definitions are also stored here, which means there is redundant 
 * information, as this information is also stored in the server's column
 * properties table. For this reason the object should be used with extreme 
 * care.
 * @author sudar.sam@gmail.com
 *
 */
public class PropertiesMetadata {
  
  // These are the keys that exist in the metadata when it comes up from
  // the phone: 
  public static final String PRIME_COLS = "primeCols";
  public static final String OV_VIEW_SETTINGS = "ovViewSettings";
  public static final String TABLE_ID = "tableId";
  public static final String COLUMNS = "columns";
  public static final String DB_TABLE_NAME = "dbTableName";
  public static final String COL_ORDER = "colOrder";
  public static final String CO_VIEW_SETTINGS = "coViewSettings";
  public static final String TABLE_TYPE = "type";
  public static final String TABLE_DISPLAY_NAME = "displayName";
  public static final String J_VERSION = "jVersion";
  
  // These are the default values that should be used for keys when it is 
  // non-obvious.
  private static final String OV_VIEW_SETTINGS_DEFAULT = 
      "{\"boxStem\":{},\"map\":{\"mapSizeRulers\":{},\"mapColorRulers\":{}}," +
      "\"line\":{},\"list\":{},\"table\":{},\"bar\":{},\"viewType\":0," +
      "\"custom\":{}}";
  private static final String CO_VIEW_SETTINGS_DEFAULT = 
      "{\"boxStem\":{},\"map\":{\"mapSizeRulers\":{},\"mapColorRulers\":{}}," +
      "\"line\":{},\"list\":{},\"table\":{},\"bar\":{},\"viewType\":0," +
      "\"custom\":{}}";
  private static final int J_VERSION_DEFAULT = 1;
  private static final ArrayList<String> PRIME_COLS_DEFAULT = 
      new ArrayList<String>();
  private static final int TABLE_TYPE_DEFAULT = 0;
  
  private Map<String, Object> map;
  
  /**
   * Create a PropertiesMetadata object. This should be used to generate a
   * metadata String to be put into the datastore upon creation of a table on
   * the server. The dbName will be the lower case version of the passed in 
   * name, prepended with an underscore and with the spaces changed to 
   * underscores. It is assumed that conflict checking happens outside of this
   * object.
   * @param tableId
   * @param tableName
   * @param columns
   */
  public PropertiesMetadata(String tableId, String displayName, 
      List<ColumnClient> columns) {
    this.map = new HashMap<String, Object>();
    initDefaults();
    this.map.put(TABLE_ID, tableId);
    //TODO here I need to add the table name and dbname!
    List<MetadataColumn> colsForMetadata = new ArrayList<MetadataColumn>();
    for (ColumnClient col : columns) {
      colsForMetadata.add(new MetadataColumn(col, tableId));
    }
    this.map.put(COLUMNS, colsForMetadata);
    this.map.put(TABLE_DISPLAY_NAME, displayName);
    String dbName = "_" + displayName.toLowerCase().replace(" ", "_");
    this.map.put(DB_TABLE_NAME, dbName);
  }
  
  /**
   * Get the map for the properties metadata. This is so that Jackson can 
   * serialize it.
   * @return
   */
  public Map<String, Object> getMap() {
    return this.map;
  }
  
  /**
   * The setter for the map. This is so jackson can serialize it.
   * @param newMap
   */
  public void setMap(Map<String, Object> newMap) {
    this.map = newMap;
  }
  
  /*
   * Add the keys and values for those keys which have default values.
   * Should only be called upon construction and on an empty map. Afterwards
   * you will only have to add the properties that change based on the table's
   * definition.
   */
  private void initDefaults() {
    this.map.put(PRIME_COLS, PRIME_COLS_DEFAULT);
    this.map.put(OV_VIEW_SETTINGS, OV_VIEW_SETTINGS_DEFAULT);
    this.map.put(CO_VIEW_SETTINGS, CO_VIEW_SETTINGS_DEFAULT);
    this.map.put(TABLE_TYPE, TABLE_TYPE_DEFAULT);
    this.map.put(J_VERSION, J_VERSION_DEFAULT);
  }
  
  /**
   * Get the metadata object as a JSON string.
   * @return
   * @throws ImportFromCSVExceptionClient if there is a problem mapping the map
   * to json
   */
  public String getAsJson() throws ImportFromCSVExceptionClient{
    ObjectMapper mapper = new ObjectMapper();
    Writer strWriter = new StringWriter();
    try {
      mapper.writeValue(strWriter, this.map);
      String jsonMetadata = strWriter.toString();
      return jsonMetadata;
    } catch (JsonMappingException e) {
      throw new ImportFromCSVExceptionClient(
          "problem mapping json in PropertiesMetadata", e);
    } catch (JsonGenerationException e) {
      throw new ImportFromCSVExceptionClient(
          "problem gneerating json in PropertiesMetadata", e);
    } catch (IOException e) {
      throw new ImportFromCSVExceptionClient(
          "IOException mapping json in PropertiesMetadata", e);
    }

  }
  
  /**
   * The COLUMNS key in the metadata list maps to a list of column objects. 
   * This class represents those object.
   * @author sudar.sam@gmail.com
   *
   */
  public class MetadataColumn {
    // these are the keys that exist for each column entry in the list attached
    // to the COLUMNS key:
    public static final String FOOTER_MODE = "footerMode";
    public static final String SMS_IN = "smsIn";
    public static final String MC_OPTIONS = "mcOptions";
    public static final String COL_TYPE = "colType";
    public static final String SMS_OUT = "smsOut";
    public static final String COL_DISPLAY_NAME = "displayName";
    public static final String J_VERSION = "jVersion";
    public static final String DB_COL_NAME = "dbColumnName";
    public static final String TABLE_ID = "tableId";
    
    // These are the default values that should be used for keys when it is 
    // non-obvious.    
    private static final boolean SMS_IN_DEFAULT = true;
    private static final boolean SMS_OUT_DEFAULT = false;
    private static final int J_VERSION_DEFAULT = 1;
    private static final int FOOTER_MODE_DEFAULT = 0;
    private static final int COL_TYPE_DEFAULT = 0;
    
    private Map<String, Object> colMap;
    
    /**
     * Construct a MetadataColumn object.
     * @param col the column you want to put into the metadata
     * @param tableId the tableId to which the column belongs
     */
    public MetadataColumn(ColumnClient col, String tableId) {
      colMap = new HashMap<String, Object>();
      initDefaults();
      map.put(COL_DISPLAY_NAME, col.getName());
      map.put(DB_COL_NAME, col.getDbName());
      map.put(TABLE_ID, tableId);
    }
    
    /**
     * Get the map holding the value for the column object. This method needs
     * to exist for serialization.
     * @return
     */
    public Map<String, Object> getMap() {
      return this.colMap;
    }
    
    /**
     * Set the map for the column properties. This method needs to exist for
     * serialization.
     * @param newColMap
     */
    public void setMap(Map<String, Object> newColMap) {
      this.colMap = newColMap;
    }
 
    /*
     * Add the keys and values for those keys which have default values.
     * Should only be called upon construction and on an empty map. Afterwards
     * you will only have to add the properties that change based on the table's
     * definition.
     */
    private void initDefaults() {
      this.colMap.put(FOOTER_MODE, FOOTER_MODE_DEFAULT);
      this.colMap.put(SMS_IN, SMS_IN_DEFAULT);
      this.colMap.put(SMS_OUT, SMS_OUT_DEFAULT);
      this.colMap.put(J_VERSION, J_VERSION_DEFAULT);
      this.colMap.put(COL_TYPE, COL_TYPE_DEFAULT);
      this.colMap.put(MC_OPTIONS, new ArrayList<String>());
    }
  }
  
}
