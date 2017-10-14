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

package org.opendatakit.aggregate.odktables.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.FileNotFoundException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.SchemaETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.exception.TableNotFoundException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.PropertyEntryXmlList;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

public interface TableService {

  public static final String CURSOR_PARAMETER = "cursor";
  public static final String FETCH_LIMIT = "fetchLimit";

  /**
   *
   * Get all tables on the server. Invoked from OdkTables implementation class.
   *
   * @return {@link TableResourceList} of all tables the user has access to.
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   */
  public Response /*TableResourceList*/ getTables(@QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException;

  /**
   * Get a particular tableId (supplied in implementation constructor)
   *
   * @return {@link TableResource} of the requested table.
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*TableResource*/ getTable() throws ODKDatastoreException,
      PermissionDeniedException, ODKTaskLockException, TableNotFoundException;

  /**
   * Create a particular tableId (supplied in implementation constructor)
   *
   * @param definition
   * @return {@link TableResource} of the table. This may already exist (with identical schema) or be newly created.
   * @throws ODKDatastoreException
   * @throws TableAlreadyExistsException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws IOException
   */
  @PUT
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*TableResource*/ createTable(TableDefinition definition )
      throws ODKDatastoreException, TableAlreadyExistsException, PermissionDeniedException, ODKTaskLockException, IOException;

  /**
   * Get the properties.csv for this tableId.
   * 
   * The properties.csv is not versioned but is atomically 
   * updated. It is the metadata for the tableId excluding
   * the data type definitions which are defined in the 
   * TableDefinition's Column array.
   * 
   * @param odkClientVersion
   * @return
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException
   * @throws FileNotFoundException 
   */
  @GET
  @Path("properties/{odkClientVersion}")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*PropertyEntryList*/ getTableProperties(@PathParam("odkClientVersion") String odkClientVersion) throws ODKDatastoreException,
      PermissionDeniedException, ODKTaskLockException, TableNotFoundException, FileNotFoundException;

  /**
   * Replace the properties.csv with the supplied propertiesList.
   * This does not preserve the existing properties in the properties.csv,
   * but does a wholesale, atomic, replacement of those properties.
   * 
   * This is the XML variant of this API. See putJsonTableProperties, below.
   * 
   * @param odkClientVersion
   * @param propertiesList
   * @return
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException
   */
  @PUT
  @Path("properties/{odkClientVersion}")
  @Consumes({ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*void*/ putXmlTableProperties(@PathParam("odkClientVersion") String odkClientVersion, PropertyEntryXmlList propertiesList) throws ODKDatastoreException,
      PermissionDeniedException, ODKTaskLockException, TableNotFoundException;

  /**
   * Replace the properties.csv with the supplied propertiesList.
   * This does not preserve the existing properties in the properties.csv,
   * but does a wholesale, atomic, replacement of those properties.
   * 
   * This is the JSON variant of this API. See putXmlTableProperties, above.
   * 
   * @param odkClientVersion
   * @param propertiesList
   * @return
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException
   */
  @PUT
  @Path("properties/{odkClientVersion}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*void*/ putJsonTableProperties(@PathParam("odkClientVersion") String odkClientVersion, ArrayList<Map<String,Object>> propertiesList) throws ODKDatastoreException,
      PermissionDeniedException, ODKTaskLockException, TableNotFoundException;

  /**
   * Get the realized verison of this table.
   *
   * @param schemaETag
   * @return
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws SchemaETagMismatchException
   * @throws AppNameMismatchException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException
   */
  @Path("ref/{schemaETag}")
  public RealizedTableService getRealizedTable(@PathParam("schemaETag") String schemaETag) throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, TableNotFoundException, ODKTaskLockException;

  /**
   * ACL manager for a particular tableId (supplied in implementation constructor)
   *
   * @return {@link TableAclService} for ACL management on this table.
   * @throws ODKDatastoreException
   * @throws AppNameMismatchException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   */
  @Path("acl")
  public TableAclService getAcl() throws ODKDatastoreException, AppNameMismatchException, PermissionDeniedException, ODKTaskLockException;
}
