/*
 * Copyright (C) 2012-2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * This is the table in the database that holds information about the files that
 * have been uploaded to be associated with certain ODKTables tables.
 *
 * We assume one ODK Aggregate per Tables App. So the AppId is something that
 * should NOT appear in this table -- it should be in the server settings table.
 * <p>
 * Each entry is a three member tuple of (tableId, pathToFile). In this way all
 * are guaranteed to be unique.
 * <p>
 * The files themselves are stored in {@link DbTablefiles} by their pathToFile
 * parameter. Each pathToFile points to a {@link BlobEntitySet} with a single
 * attachment.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class DbTableFileInfo extends Relation {

  private DbTableFileInfo(String namespace, String tableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  /**
   * String to stand in for those things in the app's root directory.
   *
   * NOTE: This cannot be null -- GAE doesn't like that!
   */
  public static final String NO_TABLE_ID = "";

  // these are the user-friendly names that are displayed when the user
  // views the contents of this table on the server.
  public static final String UI_ONLY_FILENAME_HEADING = "_FILENAME";
  public static final String UI_ONLY_TABLENAME_HEADING = "_TABLE_NAME";

  // The column names in the table. If you add any to these,
  // be sure to also add them to the columnNames list via the
  // static block.
  // Leading underscores are meant (and necessary) to indicate that these will
  // be displayed to the user on the server. The underscore will be truncated.
  // Jul 17, 2013--kind of just playing nice with the underscore thing for now
  // as I add in proper file sync support.
  public static final DataField ODK_CLIENT_VERSION = new DataField("_ODK_CLIENT_VERSION", DataType.STRING, true, 10L);
  public static final DataField TABLE_ID = new DataField("_TABLE_ID", DataType.STRING, true, 80L);
  public static final DataField PATH_TO_FILE = new DataField("_PATH_TO_FILE", DataType.STRING,
      true, 5120L);
  
  public static final DataField DELETED = new DataField("_DELETED", DataType.BOOLEAN, false);

  // limited to 10 characters
  public static final DataField FILTER_TYPE = new DataField(TableConstants.FILTER_TYPE.toUpperCase(),
      DataType.STRING, true, 10L);
  // limited to 50 characters
  public static final DataField FILTER_VALUE = new DataField(TableConstants.FILTER_VALUE.toUpperCase(),
      DataType.STRING, true, 50L).setIndexable(IndexType.HASH);

  // NOTE: code elsewhere depends upon all these data fields being String
  // fields.
  // NOTE: code elsewhere depends upon all these data fields being String
  // fields.
  // NOTE: code elsewhere depends upon all these data fields being String
  // fields.
  // NOTE: code elsewhere depends upon all these data fields being String
  // fields.
  public static final List<DataField> exposedColumnNames;

  public static final String RELATION_NAME = "TABLE_FILE_INFO4";

  // the list of the datafields/columns
  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    // can be null because we're
    dataFields.add(ODK_CLIENT_VERSION);
    dataFields.add(TABLE_ID);
    dataFields.add(PATH_TO_FILE);
    dataFields.add(DELETED);
    dataFields.add(FILTER_TYPE);
    dataFields.add(FILTER_VALUE);
    // TODO: do the appropriate time stamping and things.
    // populate the list with all the column names
    List<DataField> columns = new ArrayList<DataField>();
    columns.add(ODK_CLIENT_VERSION);
    columns.add(TABLE_ID);
    columns.add(PATH_TO_FILE);
    exposedColumnNames = Collections.unmodifiableList(columns);
  }

  public static class DbTableFileInfoEntity {
    Entity e;

    DbTableFileInfoEntity(Entity e) {
      this.e = e;
    }

    // Primary Key
    public String getId() {
      return e.getId();
    }

    public void put(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      e.put(cc);
    }

    public void delete(CallingContext cc) throws ODKDatastoreException {
      e.delete(cc);
    }

    // Accessors

    public String getTableId() {
      return e.getString(TABLE_ID);
    }

    public void setTableId(String value) {
      e.set(TABLE_ID, value);
    }

    public String getOdkClientVersion() {
      return e.getString(ODK_CLIENT_VERSION);
    }

    public void setOdkClientVersion(String value) {
      e.set(ODK_CLIENT_VERSION, value);
    }

    public String getPathToFile() {
      return e.getString(PATH_TO_FILE);
    }

    public void setPathToFile(String value) {
      e.set(PATH_TO_FILE, value);
    }

    public boolean getDeleted() {
      return e.getBoolean(DELETED);
    }

    public void setDeleted(boolean value) {
      e.set(DELETED, value);
    }

    public String getFilterType() {
      return e.getString(FILTER_TYPE);
    }

    public void setFilterType(String value) {
      e.set(FILTER_TYPE, value);
    }

    public String getFilterValue() {
      return e.getString(FILTER_VALUE);
    }

    public void setFilterValue(String value) {
      e.set(FILTER_VALUE, value);
    }

    public String getStringField(DataField field) {
      return e.getString(field);
    }

    public void setStringField(DataField field, String value) {
      e.set(field, value);
    }

    public Boolean getBooleanField(DataField field) {
      return e.getBoolean(field);
    }

    public void setBooleanField(DataField field, Boolean value) {
      e.set(field, value);
    }
  }

  private static DbTableFileInfo theRelation = null;

  private static synchronized DbTableFileInfo getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (theRelation == null) {
      DbTableFileInfo relation = new DbTableFileInfo(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
      theRelation = relation;
    }
    return theRelation;
  }

  /**
   * Create a new row in this relation. The row is not yet persisted.
   *
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static DbTableFileInfoEntity createNewEntity(CallingContext cc)
      throws ODKDatastoreException {
    return new DbTableFileInfoEntity(getRelation(cc).newEntity(cc));
  }

  /**
   * Returns all the entries.
   */
  public static List<DbTableFileInfoEntity> queryForAppLevelFiles(String odkClientVersion, CallingContext cc)
      throws ODKDatastoreException {

    Query query = getRelation(cc).query("DbTableFileInfo.queryForAppLevelFiles()", cc);
    query.addFilter(TABLE_ID,  FilterOperation.EQUAL, NO_TABLE_ID);
    query.addFilter(ODK_CLIENT_VERSION, FilterOperation.EQUAL, odkClientVersion);

    List<Entity> list = query.execute();
    List<DbTableFileInfoEntity> results = new ArrayList<DbTableFileInfoEntity>();
    for (Entity e : list) {
      results.add(new DbTableFileInfoEntity(e));
    }
    return results;
  }

  /**
   * Returns the entries for the passed in table id.
   */
  public static List<DbTableFileInfoEntity> queryForTableIdFiles(String odkClientVersion, String tableId, CallingContext cc)
      throws ODKDatastoreException {

    Query query = getRelation(cc).query("DbTableFileInfo.queryForTableIdFiles()", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);
    query.addFilter(ODK_CLIENT_VERSION, FilterOperation.EQUAL, odkClientVersion);

    List<Entity> list = query.execute();
    List<DbTableFileInfoEntity> results = new ArrayList<DbTableFileInfoEntity>();
    for (Entity e : list) {
      results.add(new DbTableFileInfoEntity(e));
    }
    return results;
  }

  public static List<String> queryForAllOdkClientVersions(CallingContext cc)
      throws ODKDatastoreException {

    Query query = getRelation(cc).query("DbTableFileInfo.queryForAllOdkClientVersions()", cc);
    query.addSort(ODK_CLIENT_VERSION, Direction.ASCENDING);
    @SuppressWarnings("unchecked")
	List<String> results = (List<String>) query.getDistinct(ODK_CLIENT_VERSION);
    return results;
  }

  public static List<DbTableFileInfoEntity> queryForAllOdkClientVersionsOfAppLevelFiles(CallingContext cc)
      throws ODKDatastoreException {

    Query query = getRelation(cc).query("DbTableFileInfo.queryForAllOdkClientVersionsOfAppLevelFiles()", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, NO_TABLE_ID);

    List<Entity> list = query.execute();
    List<DbTableFileInfoEntity> results = new ArrayList<DbTableFileInfoEntity>();
    for (Entity e : list) {
      results.add(new DbTableFileInfoEntity(e));
    }
    return results;
  }

  /**
   * Returns the entries across all odkClientVersions for the passed in table id.
   */
  public static List<DbTableFileInfoEntity> queryForAllOdkClientVersionsOfTableIdFiles(String tableId, CallingContext cc)
      throws ODKDatastoreException {

    Query query = getRelation(cc).query("DbTableFileInfo.queryForAllOdkClientVersionsOfTableIdFiles()", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);

    List<Entity> list = query.execute();
    List<DbTableFileInfoEntity> results = new ArrayList<DbTableFileInfoEntity>();
    for (Entity e : list) {
      results.add(new DbTableFileInfoEntity(e));
    }
    return results;
  }

  public static List<DbTableFileInfoEntity> queryForEntity(String odkClientVersion, String tableId, String wholePath,
      CallingContext cc) throws ODKDatastoreException {

    Query query = getRelation(cc).query("DbTableFileInfo.queryForEntity()", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);
    query.addFilter(ODK_CLIENT_VERSION, FilterOperation.EQUAL, odkClientVersion);
    query.addFilter(PATH_TO_FILE, FilterOperation.EQUAL, wholePath);

    List<Entity> list = query.execute();
    List<DbTableFileInfoEntity> results = new ArrayList<DbTableFileInfoEntity>();
    for (Entity e : list) {
      results.add(new DbTableFileInfoEntity(e));
    }
    return results;
  }
}
