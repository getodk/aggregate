package org.opendatakit.aggregate.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.client.exception.BadColumnNameExceptionClient;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.EtagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.exception.TableAlreadyExistsExceptionClient;
import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.TableDefinitionClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

/**
 * The idea is that this will house methods for OdkTables that could exist in
 * the Server.*Impl methods but that also need to be called not via gwt, eg
 * through a servlet. In this case various things like getting CallingContext
 * change, and this will be the home for that level of indirection.
 * @author sudar.sam@gmail.com
 *
 */
public class ServerOdkTablesUtil {

  /**
   * Create a table in the datastore. 
   * @param tableId
   * @param definition
   * @param cc
   * @return
   * @throws DatastoreFailureException
   * @throws TableAlreadyExistsExceptionClient
   */
  public static TableEntryClient createTable(String tableId, 
      TableDefinitionClient definition,
      CallingContext cc) throws DatastoreFailureException, 
      TableAlreadyExistsExceptionClient {
    TableManager tm = new TableManager(cc);
    Log logger = LogFactory.getLog(ServerOdkTablesUtil.class);
    // TODO: add access control stuff
    // Have to be careful of all the transforms going on here.
    // Make sure they actually work as expected!
    // also have to be sure that I am passing in an actual column and not a
    // column resource or something, in which case the transform() method is not
    // altering all of the requisite fields.
    try {
      String tableKey = definition.getTableKey();
      String dbTableName = definition.getDbTableName();
      String type = definition.getType();
      String tableIdAccessControls = definition.getTableIdAccessControls();
      // TODO: find a way to, for creation, generate a minimal list of 
      // kvs entries. for now just putting in blank if you create a table
      // from the server.
      List<OdkTablesKeyValueStoreEntry> kvsEntries = 
          new ArrayList<OdkTablesKeyValueStoreEntry>();
      List<ColumnClient> columns = definition.getColumns();
      List<Column> columnsServer = new ArrayList<Column>();
      for (ColumnClient column : columns) {
        columnsServer.add(UtilTransforms.transform(column));
      }
      TableEntry entry = tm.createTable(tableId, tableKey, dbTableName, type,
          tableIdAccessControls, columnsServer, kvsEntries);
      TableEntryClient entryClient = entry.transform();
      logger.info(String.format("tableId: %s, definition: %s", tableId, definition));
      return entryClient;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (TableAlreadyExistsException e) {
      e.printStackTrace();
      throw new TableAlreadyExistsExceptionClient(e);
    }
  }
  
  /**
   * Create or update a row in the datastore.
   * @param tableId
   * @param rowId
   * @param row
   * @param cc
   * @return
   * @throws AccessDeniedException
   * @throws RequestFailureException
   * @throws DatastoreFailureException
   * @throws EtagMismatchExceptionClient
   * @throws PermissionDeniedExceptionClient
   * @throws BadColumnNameExceptionClient
   * @throws EntityNotFoundExceptionClient
   */
  public static RowClient createOrUpdateRow(String tableId, String rowId, 
      RowClient row, CallingContext cc) throws AccessDeniedException, 
      RequestFailureException, DatastoreFailureException, 
      EtagMismatchExceptionClient, PermissionDeniedExceptionClient, 
      BadColumnNameExceptionClient, EntityNotFoundExceptionClient {
    try {
      // first transform row into a server-side row
      Row serverRow = UtilTransforms.transform(row);
      DataManager dm = new DataManager(tableId, cc);
      AuthFilter af = new AuthFilter(tableId, cc);
      af.checkPermission(TablePermission.WRITE_ROW);
      row.setRowId(rowId);
      Row dbRow = dm.getRow(rowId);
      if (dbRow == null) {
        serverRow = dm.insertRow(serverRow);
      } else {
        af.checkFilter(TablePermission.UNFILTERED_WRITE, dbRow);
        serverRow = dm.updateRow(serverRow);
      }
      return serverRow.transform();
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw new BadColumnNameExceptionClient(e);
    } catch (EtagMismatchException e) {
      e.printStackTrace();
      throw new EtagMismatchExceptionClient(e);
    }
  }
  
  /**
   * Create a FileSummaryClient object from a row that originated from 
   * EntityConverter.
   * @param row
   * @param blobSetRelation
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static FileSummaryClient getFileSummaryClientFromRow(Row row, 
      String tableId, DbTableFiles blobSetRelation, CallingContext cc) throws 
      ODKDatastoreException {
    String filename = blobSetRelation.getBlobEntitySet(
        row.getValues().get(DbTableFileInfo.VALUE), cc)
        .getUnrootedFilename(1, cc);
    Long contentLength = blobSetRelation.getBlobEntitySet(
        row.getValues().get(DbTableFileInfo.VALUE), cc)
        .getContentLength(1, cc);
    String contentType = blobSetRelation.getBlobEntitySet(
        row.getValues().get(DbTableFileInfo.VALUE), cc)
        .getContentType(1, cc);
    String key = row.getValues().get(DbTableFileInfo.KEY);
    String id = row.getRowId();
    FileSummaryClient summary = new FileSummaryClient(
        filename, contentType, contentLength, key, 0, id, tableId);
    return summary;
  }
}
