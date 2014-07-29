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

package org.opendatakit.aggregate.odktables.importexport;

import java.io.BufferedReader;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.client.exception.BadColumnNameExceptionClient;
import org.opendatakit.aggregate.client.exception.ETagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.ImportFromCSVExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvReader;
import org.opendatakit.common.web.CallingContext;

/**
 * This is OBSOLETE and BROKEN!!!!  See the one from the Android codebase.
 *
 * Holds various things for importing and exporting tables through CSVs.
 * <p>
 * Modified from the same class on the phone.
 *
 * TODO: subclass from phone definition and modify to fit server. TODO: THIS IS
 * BROKEN!!!!
 *
 * @author sudar.sam@gmail.com
 *
 */
public class CsvUtil {

  private static final String LAST_MOD_TIME_LABEL = "_ts";
  private static final String SRC_PHONE_LABEL = "_pn";

  // private final DataUtil du;
  // private final DbHelper dbh;

  /**
   * Tables imported through this function are added to the active key value
   * store. Doing it another way would give users a workaround to add tables to
   * the server database.
   *
   * @param file
   * @param tableName
   * @return
   */
  public boolean importNewTable(BufferedReader buffReader, String tableName, CallingContext cc)
      throws ImportFromCSVExceptionClient, ETagMismatchExceptionClient,
      PermissionDeniedExceptionClient, EntityNotFoundExceptionClient, BadColumnNameExceptionClient {
    LogFactory.getLog(getClass()).error("importNewTable out of date and is unimplemented!");
    return false;
    // List<ColumnClient> columns = new ArrayList<ColumnClient>();
    // try {
    // CSVReader reader = new CSVReader(buffReader);
    // String[] row = reader.readNext();
    // if (row.length == 0) {
    // reader.close();
    // return true;
    // }
    // // adding columns
    // // For the server, I think this should never happen, because this is
    // // only going to be a JSON activity...right?
    // //if ((row.length == 1) && (row[0].startsWith("{"))) {
    // // tp.setFromJson(row[0]);
    // // row = reader.readNext();
    // //} else {
    // int startIndex = 0;
    // if (row[startIndex].equals(LAST_MOD_TIME_LABEL)) {
    // startIndex++;
    // }
    // if ((row.length > startIndex) && row[startIndex].equals(SRC_PHONE_LABEL))
    // {
    // startIndex++;
    // }
    // // Here we add the columns.
    // for (int i = startIndex; i < row.length; i++) {
    // //tp.addColumn(row[i]);
    // // TODO on the phone it imports with "none". Make sure that there is a
    // // similar default on the server. Atm I'm using "string", which might
    // // not be the answer.
    // // TODO make the proper safe_name_entry here. Replace spaces with
    // // underscores, precede with underscore.
    // //String backingName = RUtil.convertToDbSafeBackingColumnName(row[i]);
    // ColumnClient newCol = new ColumnClient(row[i],
    // ColumnClient.ColumnType.STRING);
    // // TODO check for name conflicts
    // columns.add(newCol);
    // }
    // // not sure what these booleans are going.
    // boolean includeTs = row[0].equals(LAST_MOD_TIME_LABEL);
    // boolean includePn = (!includeTs || (row.length > 1))
    // && row[includeTs ? 1 : 0].equals(SRC_PHONE_LABEL);
    // return importTable(reader, tableName,
    // columns, includeTs, includePn, cc);
    // } catch (FileNotFoundException e) {
    // return false;
    // } catch (IOException e) {
    // return false;
    // }
  }

  /*
   * public boolean importAddToTable(File file, String tableId) { // TODO is
   * this the correct KVS to get the properties from? TableProperties tp =
   * TableProperties.getTablePropertiesForTable(dbh, tableId,
   * KeyValueStore.Type.ACTIVE); try { CSVReader reader = new CSVReader(new
   * FileReader(file)); String[] row = reader.readNext(); if (row.length == 0) {
   * reader.close(); return true; } if ((row.length == 1) &&
   * row[0].startsWith("{")) { tp.setFromJson(row[0]); row = reader.readNext();
   * } boolean includeTs = row[0].equals(LAST_MOD_TIME_LABEL); boolean includePn
   * = (row.length > (includeTs ? 1 : 0)) && row[includeTs ? 1 :
   * 0].equals(SRC_PHONE_LABEL); int startIndex = (includeTs ? 1 : 0) +
   * (includePn ? 1 : 0); String[] columns = new String[tp.getColumns().length];
   * for (int i = 0; i < columns.length; i++) { String displayName =
   * row[startIndex + i]; String dbName =
   * tp.getColumnByDisplayName(displayName); columns[i] = dbName; } return
   * importTable(reader, tableId, columns, includeTs, includePn); } catch
   * (FileNotFoundException e) { return false; } catch (IOException e) { return
   * false; } }
   */

  private boolean importTable(RFC4180CsvReader reader, String tableName, List<ColumnClient> columns,
      boolean includeTs, boolean includePn, CallingContext cc) throws BadColumnNameExceptionClient,
      EntityNotFoundExceptionClient, PermissionDeniedExceptionClient, ETagMismatchExceptionClient,
      ImportFromCSVExceptionClient {
    return false; // unimplemented and out of date.
    // NOTE: this reader will return null if the row is empty.
    // Compatible with Excel on Mac and Windows.
    //
    // int tsIndex = includeTs ? 0 : -1;
    // int pnIndex = includePn ? (includeTs ? 1 : 0) : -1;
    // int startIndex = (includeTs ? 1 : 0) + (includePn ? 1 : 0);
    // //DbTable dbt = DbTable.getDbTable(dbh, tableId);
    // try {
    // String newTableId = CommonFieldsBase.newUri();
    // PropertiesMetadata metadataObject = new PropertiesMetadata(newTableId,
    // tableName, columns);
    // String metadataString = metadataObject.getAsJson();
    // TableManager tm = new TableManager(cc);
    // TableDefinitionClient tableDef = new TableDefinitionClient(
    // tableName, columns, metadataString);
    // // Now create the actual table in the db. Null for the id so it auto
    // // generates a UUID.
    // //ServerTableServiceImpl tableService = new ServerTableServiceImpl();
    // // we do this just to create the table. the fact that we don't use it
    // // is ok.
    // TableEntryClient tableEntry = ServerOdkTablesUtil.createTable(
    // newTableId, tableDef, cc);
    //
    // // And now add all the rows for the doowop.
    // //ServerDataServiceImpl dataService = new ServerDataServiceImpl();
    // Map<String, String> values = new HashMap<String, String>();
    // String[] row = reader.readNext();
    // while (row != null) {
    // for (int i = 0; i < columns.size(); i++) {
    // values.put(columns.get(i).getDbName(), row[startIndex + i]);
    // }
    // // we want to generate a UUID for each row.
    // String newRowId = UUID.randomUUID().toString();
    // RowClient newTableRow = RowClient.forInsert(newRowId, values);
    // // So I think atm, that both of these should be -1, as we're not
    // // allowing any ADDING to the table at this point from the server via
    // // CSV.
    // //String lastModTime = tsIndex == -1 ? du.formatNowForDb() :
    // row[tsIndex];
    // //String srcPhone = pnIndex == -1 ? null : row[pnIndex];
    // DateTimeFormatter formatter =
    // DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").withZoneUTC();
    // String lastModTime = formatter.print(new DateTime());
    // String srcPhone = null;
    // //dbt.addRow(values, lastModTime, srcPhone);
    // //dataService.createOrUpdateRow(newTableId, newRowId, newTableRow);
    // // TODO if this fails we should probably delete the table we've created
    // ServerOdkTablesUtil.createOrUpdateRow(newTableId, newRowId,
    // newTableRow, cc);
    // values.clear();
    // row = reader.readNext();
    // }
    // reader.close();
    // return true;
    // } catch (IOException e) {
    // e.printStackTrace();
    // return false;
    // } catch (DatastoreFailureException e) {
    // e.printStackTrace();
    // throw new ImportFromCSVExceptionClient("datastore failure in CsvUtil",
    // e);
    // } catch (RequestFailureException e) {
    // e.printStackTrace();
    // throw new ImportFromCSVExceptionClient("request failure in csvutil", e);
    // } catch (AccessDeniedException e) {
    // e.printStackTrace();
    // throw new ImportFromCSVExceptionClient("access denied while importing" +
    // " from csv", e);
    // }
  }

  /*
   * public boolean export(File file, String tableId, boolean includeTs, boolean
   * includePn) { return export(file, tableId, includeTs, includePn, true); }
   *
   * public boolean exportWithProperties(File file, String tableId, boolean
   * includeTs, boolean includePn) { return export(file, tableId, includeTs,
   * includePn, false); }
   *
   *
   * private boolean export(File file, String tableId, boolean includeTs,
   * boolean includePn, boolean raw) { // TODO test that this is the correct KVS
   * to get the export from. TableProperties tp =
   * TableProperties.getTablePropertiesForTable(dbh, tableId,
   * KeyValueStore.Type.ACTIVE); // building array of columns to select and
   * header row for output file int columnCount = tp.getColumns().length +
   * (includeTs ? 1 : 0) + (includePn ? 1 : 0); String[] columns = new
   * String[columnCount]; String[] headerRow = new String[columnCount]; int
   * index = 0; if (includeTs) { columns[index] = DbTable.DB_LAST_MODIFIED_TIME;
   * headerRow[index] = LAST_MOD_TIME_LABEL; index++; } if (includePn) {
   * columns[index] = DbTable.DB_SRC_PHONE_NUMBER; headerRow[index] =
   * SRC_PHONE_LABEL; index++; } if (raw) { for (ColumnProperties cp :
   * tp.getColumns()) { columns[index] = cp.getColumnDbName(); headerRow[index]
   * = cp.getDisplayName(); index++; } } else { for (ColumnProperties cp :
   * tp.getColumns()) { columns[index] = cp.getColumnDbName(); headerRow[index]
   * = cp.getColumnDbName(); index++; } } // getting data DbTable dbt =
   * DbTable.getDbTable(dbh, tableId); Table table = dbt.getRaw(columns, null,
   * null, null); // writing data try { CSVWriter cw = new CSVWriter(new
   * FileWriter(file)); if (!raw) { cw.writeNext(new String[] { tp.toJson() });
   * } cw.writeNext(headerRow); String[] row = new String[columnCount]; for (int
   * i = 0; i < table.getHeight(); i++) { for (int j = 0; j < table.getWidth();
   * j++) { row[j] = table.getData(i, j); } cw.writeNext(row); } cw.close();
   * return true; } catch (IOException e) { return false; } }
   */
}
