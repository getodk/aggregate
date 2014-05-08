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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Path("{appId}/tables")
@Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
public interface TableService {

  /**
   *
   * @param appId
   *
   * @return {@link TableResourceList} of all tables the user has access to.
   * @throws ODKDatastoreException
   */
  @GET
  @Path("")
  @GZIP
  public Response /*TableResourceList*/ getTables(@PathParam("appId") String appId) throws ODKDatastoreException;

  /**
   *
   * @param appId
   * @param tableId
   * @return {@link TableResource} of the requested table.
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @Path("{tableId}")
  @GZIP
  public Response /*TableResource*/ getTable(@PathParam("appId") String appId, @PathParam("tableId") String tableId) throws ODKDatastoreException,
      PermissionDeniedException;

  /**
   *
   * @param appId
   * @param tableId
   * @param definition
   * @return {@link TableResource} of the table. This may already exist (with identical schema) or be newly created.
   * @throws ODKDatastoreException
   * @throws TableAlreadyExistsException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   */
  @PUT
  @Path("{tableId}")
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  @GZIP
  public Response /*TableResource*/ createTable(@PathParam("appId") String appId, @PathParam("tableId") String tableId, @GZIP TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException, PermissionDeniedException, ODKTaskLockException;

  /**
   *
   * @param appId
   * @param tableId
   * @return successful status code if successful.
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   */
  @DELETE
  @Path("{tableId}")
  public Response /*void*/ deleteTable(@PathParam("appId") String appId, @PathParam("tableId") String tableId) throws ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException;

  /**
   *
   * @param appId
   * @param tableId
   * @return {@link TableDefinitionResource} for the schema of this table.
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   */
  @GET
  @Path("{tableId}/definition")
  @GZIP
  public Response /*TableDefinitionResource*/ getDefinition(@PathParam("appId") String appId, @PathParam("tableId") String tableId)
      throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException;

  /**
   *
   * @param appId
   * @param tableId
   * @return {@link DataService} for manipulating row data in this table.
   * @throws ODKDatastoreException
   */
  @Path("{tableId}/rows")
  public DataService getData(@PathParam("appId") String appId, @PathParam("tableId") String tableId) throws ODKDatastoreException;

  /**
   *
   * @param appId
   * @param tableId
   * @return {@link InstanceFileService} for file attachments to the rows on this table.
   * @throws ODKDatastoreException
   */
  @Path("{tableId}/attachments")
  public InstanceFileService getInstanceFiles(@PathParam("appId") String appId, @PathParam("tableId") String tableId) throws ODKDatastoreException;

  /**
   *
   * @param appId
   * @param tableId
   * @return {@link DiffService} for the row-changes on this table.
   * @throws ODKDatastoreException
   */
  @Path("{tableId}/diff")
  public DiffService getDiff(@PathParam("appId") String appId, @PathParam("tableId") String tableId) throws ODKDatastoreException;

  /**
   *
   * @param appId
   * @param tableId
   * @return {@link TableAclService} for ACL management on this table.
   * @throws ODKDatastoreException
   */
  @Path("{tableId}/acl")
  public TableAclService getAcl(@PathParam("appId") String appId, @PathParam("tableId") String tableId) throws ODKDatastoreException;
}
