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

package org.opendatakit.aggregate.odktables.impl.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.TableManager.WebsafeTables;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.api.RealizedTableService;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.SchemaETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.exception.TableNotFoundException;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo.DbTableFileInfoEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvReader;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvWriter;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.PropertyEntry;
import org.opendatakit.aggregate.odktables.rest.entity.PropertyEntryList;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

import com.google.common.net.MediaType;

public class TableServiceImpl implements TableService {
  private static final Log logger = LogFactory.getLog(TableServiceImpl.class);

  private static final String ERROR_TABLE_NOT_FOUND = "Table not found";
  private static final String ERROR_SCHEMA_DIFFERS = "SchemaETag differs";

  private final ServletContext sc;
  private final HttpServletRequest req;
  private final HttpHeaders headers;
  private final UriInfo info;
  private final String appId;
  private final String tableId;
  private final CallingContext cc;

  public TableServiceImpl(ServletContext sc, HttpServletRequest req, HttpHeaders headers, UriInfo info, String appId, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.sc = sc;
    this.req = req;
    this.headers = headers;
    this.info = info;
    this.appId = appId;
    tableId = null;
    this.cc = cc;
  }

  public TableServiceImpl(ServletContext sc, HttpServletRequest req, HttpHeaders headers, UriInfo info, String appId, String tableId, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.sc = sc;
    this.req = req;
    this.headers = headers;
    this.info = info;
    this.appId = appId;
    this.tableId = tableId;
    this.cc = cc;
  }

  @Override
  public Response getTables(@QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);

    int limit = (fetchLimit == null || fetchLimit.length() == 0) ? 2000 : Integer.parseInt(fetchLimit);
    WebsafeTables websafeResult = tm.getTables(QueryResumePoint.fromWebsafeCursor(WebUtils.safeDecode(cursor)), limit);
    ArrayList<TableResource> resources = new ArrayList<TableResource>();
    for (TableEntry entry : websafeResult.tables) {
      // database cruft will have a null schemaETag -- ignore those
      if ( entry.getSchemaETag() != null ) {
        TableResource resource = getResource(info, appId, entry);
        resources.add(resource);
      }
    }
    TableResourceList tableResourceList = new TableResourceList(resources,
        WebUtils.safeEncode(websafeResult.websafeRefetchCursor),
        WebUtils.safeEncode(websafeResult.websafeBackwardCursor),
        WebUtils.safeEncode(websafeResult.websafeResumeCursor),
        websafeResult.hasMore, websafeResult.hasPrior);
    return Response.ok(tableResourceList).build();
  }

  @Override
  public Response getTable() throws ODKDatastoreException, TableNotFoundException,
      PermissionDeniedException, ODKTaskLockException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);
    TableEntry entry = tm.getTable(tableId);
    if ( entry == null || entry.getSchemaETag() == null ) {
      // the table doesn't exist yet (or something is there that is database cruft)
      throw new TableNotFoundException(ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    TableResource resource = getResource(info, appId, entry);
    return Response.ok(resource).build();
  }

  @Override
  public Response createTable(TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException, PermissionDeniedException, ODKTaskLockException, IOException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);
    // NOTE: the only access control restriction for
    // creating the table is the Administer Tables role. 
    List<Column> columns = definition.getColumns();

    TableEntry entry = tm.createTable(tableId, columns);
    TableResource resource = getResource(info, appId, entry);
    logger.info(String.format("tableId: %s, definition: %s", tableId, definition));
    return Response.ok(resource).build();
  }

  @Override
  public RealizedTableService getRealizedTable(@PathParam("schemaETag") String schemaETag) throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);
    TableEntry entry = tm.getTable(tableId);
    if ( entry == null || entry.getSchemaETag() == null ) {
      // the table doesn't exist yet (or something is there that is database cruft)
      throw new TableNotFoundException(ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    if ( !entry.getSchemaETag().equals(schemaETag) ) {
      throw new SchemaETagMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + entry.getSchemaETag());
    }
    RealizedTableService service = new RealizedTableServiceImpl(sc, req, headers, info, appId, tableId, schemaETag, userPermissions, tm, cc);
    return service;

  }

  @Override
  public TableAclService getAcl() throws ODKDatastoreException, AppNameMismatchException, PermissionDeniedException, ODKTaskLockException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    // orthogonal to access rights to the table...
    // TableManager tm = new TableManager(appId, userPermissions, cc);
    TableAclService service = new TableAclServiceImpl(appId, tableId, info, userPermissions, cc);
    return service;
  }

  private TableResource getResource(UriInfo info, String appId, TableEntry entry) {
    String tableId = entry.getTableId();
    String schemaETag = entry.getSchemaETag();

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(OdkTables.class, "getTablesService");
    URI self = ub.clone().build(appId, tableId);
    UriBuilder realized = ub.clone().path(TableService.class, "getRealizedTable");
    URI data = realized.clone().path(RealizedTableService.class, "getData").build(appId, tableId, schemaETag);
    URI instanceFiles = realized.clone().path(RealizedTableService.class, "getInstanceFileService").build(appId, tableId, schemaETag);
    URI diff = realized.clone().path(RealizedTableService.class, "getDiff").build(appId, tableId, schemaETag);
    URI acl = ub.clone().path(TableService.class, "getAcl").build(appId, tableId);
    URI definition = realized.clone().build(appId, tableId, schemaETag);

    TableResource resource = new TableResource(entry);
    try {
      resource.setSelfUri(self.toURL().toExternalForm());
      resource.setDefinitionUri(definition.toURL().toExternalForm());
      resource.setDataUri(data.toURL().toExternalForm());
      resource.setInstanceFilesUri(instanceFiles.toURL().toExternalForm());
      resource.setDiffUri(diff.toURL().toExternalForm());
      resource.setAclUri(acl.toURL().toExternalForm());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return resource;
  }

  @Override
  public Response getTableProperties() throws ODKDatastoreException, PermissionDeniedException,
      ODKTaskLockException, TableNotFoundException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    String wholePath = FileServiceImpl.getPropertiesFilePath(tableId);

    byte[] fileBlob;
    String contentType;
    Long contentLength;
    try {
      userPermissions.checkPermission(appId, tableId, TablePermission.READ_PROPERTIES);
      
      // properties.csv is odkClientVersion-agnostic -- and always stored as a version "1" file.
      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity("1", tableId, wholePath, cc);
      if (entities.size() > 1) {
        Log log = LogFactory.getLog(DbTableFileInfo.class);
        log.error("more than one entity for appId: " + appId + ", tableId: " + tableId
            + ", pathToFile: " + wholePath);
      } else if (entities.size() < 1) {
        return Response.status(Status.NOT_FOUND).entity("No manifest entry found for: " + wholePath).build();
      }
      DbTableFileInfoEntity dbTableFileInfoRow = entities.get(0);
      String uri = dbTableFileInfoRow.getId();
      DbTableFiles dbTableFiles = new DbTableFiles(cc);
      BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(uri, cc);
      // We should only ever have one, as wholePath is the primary key.
      if (blobEntitySet.getAttachmentCount(cc) > 1) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("More than one file specified for: " + wholePath).build();
      }
      if (blobEntitySet.getAttachmentCount(cc) < 1) {
        return Response.status(Status.NOT_FOUND).entity("No file found for path: " + wholePath).build();
      }
      fileBlob = blobEntitySet.getBlob(1, cc);
      contentType = blobEntitySet.getContentType(1, cc);
      contentLength = blobEntitySet.getContentLength(1, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve attachment and access attributes for: " + wholePath).build();
    } catch (PermissionDeniedException e) {
      logger.error(("ODKTables file upload permissions error: " + e.getMessage()));
      return Response.status(Status.UNAUTHORIZED).entity("Permission denied").build();
    }
    // And now prepare everything to be returned to the caller.
    if (fileBlob != null && contentType != null && contentLength != null && contentLength != 0L) {
      // read the byte[] array using the CSV reader, and build a 
      // list of PropertyEntry objects. 
      ByteArrayInputStream bas = new ByteArrayInputStream(fileBlob);
      Reader rdr = null;
      RFC4180CsvReader csvReader = null;
      ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
      try {
        rdr = new InputStreamReader(bas, CharEncoding.UTF_8);
        csvReader = new RFC4180CsvReader(rdr);
        
        String[] entries = csvReader.readNext();
        if ( entries.length != 5 ) {
          throw new IllegalStateException("Uploaded properties.csv does not have 5 columns!");
        }
        
        if ( !"_partition".equals(entries[0])) {
          throw new IllegalStateException("Uploaded properties.csv does not have 'partition' as first column heading!");
        }
        
        if ( !"_aspect".equals(entries[1])) {
          throw new IllegalStateException("Uploaded properties.csv does not have 'aspect' as second column heading!");
        }
        
        if ( !"_key".equals(entries[2])) {
          throw new IllegalStateException("Uploaded properties.csv does not have 'key' as third column heading!");
        }
        
        if ( !"_type".equals(entries[3])) {
          throw new IllegalStateException("Uploaded properties.csv does not have 'type' as fourth column heading!");
        }
        
        if ( !"_value".equals(entries[4])) {
          throw new IllegalStateException("Uploaded properties.csv does not have 'value' as fifth column heading!");
        }
        
        entries = csvReader.readNext();
        while ( entries != null ) {
          PropertyEntry e = new PropertyEntry(entries[0], entries[1], entries[2], entries[3], entries[4]);
          properties.add(e);
          entries = csvReader.readNext();
        }
      } catch (UnsupportedEncodingException ex) {
        ex.printStackTrace();
        throw new IllegalStateException("unrecognized UTF-8 charset encoding!");
      } catch (IOException ex) {
        ex.printStackTrace();
        throw new IllegalStateException("unable to parse properties.csv!");
      } finally {
        if ( csvReader != null ) {
          try {
            csvReader.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else if ( rdr != null ) {
          try {
            rdr.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      
      PropertyEntryList pl = new PropertyEntryList(properties);
      
      ResponseBuilder rBuild = Response.ok(pl);
      return rBuild.build();
    } else {
      PropertyEntryList pl = new PropertyEntryList(null);
      
      ResponseBuilder rBuild = Response.ok(pl);
      return rBuild.build();
    }
  }

  @Override
  public Response putTableProperties(PropertyEntryList propertiesList)
      throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException,
      TableNotFoundException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    String filePath = FileServiceImpl.getPropertiesFilePath(tableId);

    String contentType = MediaType.CSV_UTF_8.toString();
    try {
      // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level permissions
      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_PROPERTIES);

      ByteArrayOutputStream bas = new ByteArrayOutputStream();
      Writer wtr = null;
      RFC4180CsvWriter csvWtr = null;
      
      try {
        wtr = new OutputStreamWriter(bas, CharEncoding.UTF_8);
        csvWtr = new RFC4180CsvWriter(wtr);
        String[] entry = new String[5];
        entry[0] = "_partition";
        entry[1] = "_aspect";
        entry[2] = "_key";
        entry[3] = "_type";
        entry[4] = "_value";
        csvWtr.writeNext(entry);
        for ( PropertyEntry e : propertiesList.getProperties()) {
          entry[0] = e.getPartition();
          entry[1] = e.getAspect();
          entry[2] = e.getKey();
          entry[3] = e.getType();
          entry[4] = e.getValue();
          csvWtr.writeNext(entry);
        }
        csvWtr.flush();
      } catch (UnsupportedEncodingException ex) {
        ex.printStackTrace();
        throw new IllegalStateException("Unrecognized UTF-8 charset!");
      } catch (IOException ex) {
        ex.printStackTrace();
        throw new IllegalStateException("Unable to write into a byte array!");
      } finally {
        if ( csvWtr != null ) {
          try {
            csvWtr.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else if ( wtr != null ) {
          try {
            wtr.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      
      byte[] content = bas.toByteArray();
      // properties.csv is odkClientVersion-agnostic -- and always stored as a version "1" file.

      // 0) Delete anything that is already stored

      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity("1", tableId, filePath, cc);
      for ( DbTableFileInfoEntity entity : entities ) {

        String uri = entity.getId();
        DbTableFiles dbTableFiles = new DbTableFiles(cc);
        BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(uri, cc);
        blobEntitySet.remove(cc);
        entity.delete(cc);
      }

      // We are going to store the file in two tables: 1) a user-friendly table
      // that relates an app and table id to the name of a file; 2) a table
      // that holds the actual blob.
      //
      // Table 1 is represented by DbTableFileInfo. Each row of this table
      // contains a uri, appid, tableid, and pathToFile.
      // Table 2 is a BlobEntitySet. The top level URI of this blob entity set
      // is the uri from table 1. Each blob set here has a single attachment
      // count of 1--the blob of the file itself. The pathToFile of this
      // attachment is null.
      //
      // So, now that we have retrieved the file from the request, we have two
      // things to do: 1) create an entry in the user-friendly table so we can
      // bet a uri. 2) add the file to the blob entity set, using the top level
      // uri as the row uri from table 1.
      //
      // 1) Create an entry in the user friendly table.
      EntityCreator ec = new EntityCreator();
      DbTableFileInfoEntity tableFileInfoRow = ec.newTableFileInfoEntity("1", tableId, filePath,
          userPermissions, cc);
      String rowUri = tableFileInfoRow.getId();

      // 2) Put the blob in the datastore.
      DbTableFiles dbTableFiles = new DbTableFiles(cc);
      // Although this is called an entity set, it in fact represents a single
      // file, because we have chosen to use it this way in this case. For more
      // information see the docs in DbTableFiles. We'll use the uri of the
      // corresponding row in the DbTableFileInfo table.
      BlobEntitySet instance = dbTableFiles.newBlobEntitySet(rowUri, cc);
      // TODO: this being set to true is probably where some sort of versioning
      // should happen.
      BlobSubmissionOutcome outcome = instance.addBlob(content, contentType, null, true, cc);
      // 3) persist the user-friendly table entry about the blob
      tableFileInfoRow.put(cc);
      return Response.status(Status.ACCEPTED).build();
    } catch (ODKDatastoreException e) {
      logger.error(("ODKTables file upload persistence error: " + e.getMessage()));
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage()).build();
    } catch (PermissionDeniedException e) {
      logger.error(("ODKTables file upload permissions error: " + e.getMessage()));
      return Response.status(Status.UNAUTHORIZED).entity("Permission denied").build();
    }
  }

}