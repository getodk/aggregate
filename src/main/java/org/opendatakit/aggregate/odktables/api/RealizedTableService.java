/*
 * Copyright (C) 2014 University of Washington
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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.SchemaETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableNotFoundException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

/**
 * Realized tables are identified by the tuple (appId, tableId, schemaETag)
 * Whenever a table is dropped and recreated, the new table has a new,
 * unique, schemaETag.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public interface RealizedTableService {

  /**
   * Delete a realized tableId and all its data (supplied in implementation constructor)
   *
   * @return successful status code if successful.
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   */
  @DELETE
  public Response /*void*/ deleteTable() throws ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException;

  /**
   * Get the definition of a realized tableId (supplied in implementation constructor)
   *
   * @return {@link TableDefinitionResource} for the schema of this table.
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws AppNameMismatchException
   * @throws TableNotFoundException 
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*TableDefinitionResource*/ getDefinition()
      throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException, AppNameMismatchException, TableNotFoundException;
  
  /**
   * Data row subresource for a realized tableId (supplied in implementation constructor)
   *
   * @return {@link DataService} for manipulating row data in this table.
   * @throws ODKDatastoreException
   * @throws SchemaETagMismatchException
   * @throws PermissionDeniedException
   * @throws AppNameMismatchException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException 
   */
  @Path("rows")
  public DataService getData() throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException;

  /**
   * Exposed only to provide the attachments URL in the TableResource
   *
   * @return throws PermissionDeniedException if called.
   * @throws PermissionDeniedException
   */
  @Path("attachments")
  public InstanceFileService getInstanceFileService() throws PermissionDeniedException;


  /**
   * Instance file subresource for a realized tableId and (supplied in implementation constructor)
   *
   * @param rowId
   * @return {@link InstanceFileService} for file attachments to the rows on this table.
   * @throws ODKDatastoreException
   * @throws SchemaETagMismatchException
   * @throws PermissionDeniedException
   * @throws AppNameMismatchException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException 
   */
  @Path("attachments/{rowId}")
  public InstanceFileService getInstanceFiles(@PathParam("rowId") String rowId) throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException;

  /**
   * Differences subresource for a realized tableId (supplied in implementation constructor)
   *
   * @return {@link DiffService} for the row-changes on this table.
   * @throws ODKDatastoreException
   * @throws SchemaETagMismatchException
   * @throws PermissionDeniedException
   * @throws AppNameMismatchException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException 
   */
  @Path("diff")
  public DiffService getDiff() throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException;
  
  /**
   * Differences subresource for a realized tableId (supplied in implementation constructor)
   *
   * @return {@link QueryService} for the row-changes on this table.
   * @throws ODKDatastoreException
   * @throws SchemaETagMismatchException
   * @throws PermissionDeniedException
   * @throws AppNameMismatchException
   * @throws ODKTaskLockException
   * @throws TableNotFoundException 
   */
  @Path("query")
  public QueryService getQuery() throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException;
}
