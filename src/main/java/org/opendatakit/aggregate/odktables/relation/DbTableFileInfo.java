package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This is the table in the database that holds information about the files that
 * have been uploaded to be associated with certain ODKTables tables.
 * <p>
 * The files themselves will be stored in another collection of tables and
 * managed with the blob relation API provided by Mitch. This is the
 * user-friendly table that has information about how to get at the actual
 * files. The table is structured with the following columns: --URI (the actual
 * URI for the row) --Tables_URI (the UUID for the table this file is associated
 * with) --KEY (a key that is interpreted by OdkTables. Likely things like
 * "list", which would mean this was the file meant for the listview, etc.)
 * --BLOB_TYPE (the type of what the value is pointing to. eg file, int, String)
 * --VALUE (the unique identifer to the set in the blob relation. So this is the
 * value that you would get and then use to query the blobset to get the actual
 * set of 1 file.) 
 * --IS_MEDIA: this tells you if the file is actually a media file that 
 * should not be displayed with the regular files. Instead it should be 
 * accessed as a link to a popup as in FormList.
 * <p>
 * Each file is uploaded as an "EntitySet" of size 1. This set comes with a
 * unique key that allows access of all the files in the set, which in this case
 * will just have an "attachment count" of one, as the set is only of size one.
 * 
 * @author sudar.sam@gmail.com
 * 
 */
public class DbTableFileInfo {

  // these are the user-friendly names that are displayed when the user
  // views the contents of this table on the server.
  public static final String UI_ONLY_FILENAME_HEADING = "_FILENAME";
  public static final String UI_ONLY_TABLENAME_HEADING = "_TABLE_NAME";

  // The column names in the table. If you add any to these,
  // be sure to also add them to the columnNames list via the
  // static block.
  // Leading underscores are meant (and necessary) to indicate that these will
  // be displayed to the user on the server. The underscore will be truncated.
  public static final String TABLE_ID = "TABLE_UUID";
  public static final String KEY = "_KEY";
  
  // This is really the type of the entry to say what the value will be. if it
  // is of type file, then the value is the key to the blobset of size one
  // that has the file. If the type is string, then the value is the actual
  // string, etc.
  // (this was formerly BLOB_TYPE, in case that lingers somewhere in the
  // comments.)
  public static final String VALUE_TYPE = "_TYPE";
  public static final String VALUE = "VALUE";
  public static final String IS_MEDIA = "IS_MEDIA";

  public static final List<String> columnNames;

  public static final String RELATION_NAME = "TABLE_FILE_INFO";

  // the list of the datafields/columns
  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(TABLE_ID, DataType.STRING, false)
        .setIndexable(IndexType.HASH));
    dataFields.add(new DataField(KEY, DataType.STRING, false));
    dataFields.add(new DataField(VALUE_TYPE, DataType.STRING, false));
    dataFields.add(new DataField(VALUE, DataType.STRING, false));
    dataFields.add(new DataField(IS_MEDIA, DataType.BOOLEAN, false));
    // and add the things from DbTable
    dataFields.addAll(DbTable.getStaticFields());
    // populate the list with all the column names
    List<String> columns = new ArrayList<String>();
    // first we want to add the columns that are present in all the
    // DbTables.
    columns.add(TABLE_ID);
    columns.add(KEY);
    columns.add(VALUE_TYPE);
    columns.add(VALUE);
    columns.add(DbTable.ROW_VERSION);
    columns.add(DbTable.DATA_ETAG_AT_MODIFICATION);
    columns.add(DbTable.CREATE_USER);
    columns.add(DbTable.LAST_UPDATE_USER);
    columns.add(DbTable.FILTER_TYPE);
    columns.add(DbTable.FILTER_VALUE);
    columns.add(DbTable.DELETED);
    columnNames = Collections.unmodifiableList(columns);
  }

  public static Relation getRelation(CallingContext cc) 
      throws ODKDatastoreException {
    Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME, 
        dataFields, cc);
    return relation;
  }

  /**
   * I'm pretty sure this returns the entries for the passed in table id.
   */
  public static List<Entity> query(String tableId, CallingContext cc) 
      throws ODKDatastoreException {
    return getRelation(cc).query("DbTableFileInfo.query()", cc)
        .equal(TABLE_ID, tableId).execute();
  }
  
  /**
   * Get all the non-media files that have been uploaded for the given table
   * id. This will be things that have been uploaded directly, not as media
   * files.
   * @param tableId
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static List<Entity> queryForNonMediaFiles(String tableId, 
      CallingContext cc) throws ODKDatastoreException {
    return getRelation(cc).query("DbTableFileInfo.queryForNonMediaFiles", cc)
        .equal(TABLE_ID, tableId).equal(IS_MEDIA, false).execute();
  }
  
  /**
   * Return the media files for the given table that are associated with the
   * given key. Being associated with the given key is checked by comparing
   * the "key" entry for all the IS_MEDIA==true files whose keys begin with
   * "key_". "key" in this case will be something like "box", "graph", etc.
   * @param tableId
   * @param key
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static List<Entity> queryForMediaFiles(String tableId, String key, 
      CallingContext cc) throws ODKDatastoreException {
    List<Entity> allFiles = query(tableId, cc);
    List<Entity> mediaFiles = new ArrayList<Entity>();
    for (Entity e : allFiles) {
      String keyInDb = e.getAsString(KEY);
      if (keyInDb.startsWith(key + "_")) {
        mediaFiles.add(e);
      }
    }
    // at this point mediaFiles.size == allFiles.size()-1. If not, something
    // weird has been going on.
    return mediaFiles;
  }

  /**
   * These are the types that are currently supported in the datastore. They are
   * important for knowing how to generate the manifest of what needs to be
   * pushed to the phone.
   * 
   * @author sudars
   * 
   */
  public enum Type {
    // TODO: this should maybe be "text" rather than string to match the sql
    // lite db on the phone, and also contain doubles. also shouldn't use the 
    // name field, as that is an enum term.
    STRING("string"), INTEGER("integer"), FILE("file");

    public final String name; // what you call the enum

    Type(String name) {
      this.name = name;
    }
  }
}
