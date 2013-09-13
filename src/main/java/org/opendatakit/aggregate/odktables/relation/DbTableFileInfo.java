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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.ermodel.BlobEntitySet;
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
 * Each entry is a three member tuple of (appId, tableId, pathToFile). In this
 * way all are guaranteed to be unique.
 * <p>
 * The files themselves are stored in {@link DbTablefiles} by their pathToFile
 * parameter. Each pathToFile points to a {@link BlobEntitySet} with a single
 * attachment. 
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
  // Jul 17, 2013--kind of just playing nice with the underscore thing for now 
  // as I add in proper file sync support.
  public static final String APP_ID = "_APP_ID";
  public static final String TABLE_ID = "_TABLE_ID";
  public static final String PATH_TO_FILE = "_PATH_TO_FILE";

  public static final List<String> columnNames;

  public static final String RELATION_NAME = "TABLE_FILE_INFO";

  // the list of the datafields/columns
  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(new DataField(APP_ID, DataType.STRING, false)
    .setIndexable(IndexType.HASH));
    // can be null because we're 
    dataFields.add(new DataField(TABLE_ID, DataType.STRING, true));
    dataFields.add(new DataField(PATH_TO_FILE, DataType.STRING, true, 
        20480L));
    // and add the things from DbTable
    dataFields.addAll(DbTable.getStaticFields());
    // TODO: do the appropriate time stamping and things.
    // populate the list with all the column names
    List<String> columns = new ArrayList<String>();
    columns.add(APP_ID);
    columns.add(TABLE_ID);
    columns.add(PATH_TO_FILE);
    columnNames = Collections.unmodifiableList(columns);
  }

  private static Relation theRelation = null;

  public static synchronized Relation getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if ( theRelation == null ) {
      Relation relation = new Relation(RUtil.NAMESPACE, RELATION_NAME,
        dataFields, cc);
      theRelation = relation;
    }
    return theRelation;
  }

  /**
   * I'm pretty sure this returns the entries for the passed in table id.
   */
  public static List<Entity> queryForTableId(String tableId, CallingContext cc)
      throws ODKDatastoreException {
    return getRelation(cc).query("DbTableFileInfo.queryForTableId()", cc)
        .equal(TABLE_ID, tableId).execute();
  }
  
  public static List<Entity> queryForAppId(String appId, CallingContext cc) 
      throws ODKDatastoreException {
    return getRelation(cc).query("DbTableFileInfo.queryForAppId()", cc)
        .equal(APP_ID, appId).execute();
  }
  
  public static List<Entity> queryForAppAndTable(String appId, String tableId,
      CallingContext cc) throws ODKDatastoreException {
    return getRelation(cc).query("DbTableFileInfo.queryForAppAndTable()", cc)
        .equal(APP_ID, appId).equal(TABLE_ID, tableId).execute();
  }
  
  public static List<Entity> queryForEntity(String appId, String tableId, 
      String wholePath, CallingContext cc) throws ODKDatastoreException {
    return getRelation(cc).query(
        "DbTableFileInfo.queryForEntity()", cc).equal(APP_ID, appId)
        .equal(TABLE_ID, tableId).equal(PATH_TO_FILE, wholePath).execute();
  }

}
