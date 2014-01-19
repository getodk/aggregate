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

import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Path("/tables")
@Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
public interface TableService {

  @GET
  public TableResourceList getTables() throws ODKDatastoreException;

  @GET
  @Path("{tableId}")
  public TableResource getTable(@PathParam("tableId") String tableId) throws ODKDatastoreException,
      PermissionDeniedException;

  @PUT
  @Path("{tableId}")
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public TableResource createTable(@PathParam("tableId") String tableId, TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException, PermissionDeniedException;

  @DELETE
  @Path("{tableId}")
  public void deleteTable(@PathParam("tableId") String tableId) throws ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException;

  @Path("{tableId}/rows")
  public DataService getData(@PathParam("tableId") String tableId) throws ODKDatastoreException;

  @Path("{tableId}/properties")
  public PropertiesService getProperties(@PathParam("tableId") String tableId)
      throws ODKDatastoreException;

  @GET
  @Path("{tableId}/definition")
  public TableDefinitionResource getDefinition(@PathParam("tableId") String tableId)
      throws ODKDatastoreException, PermissionDeniedException;

  @Path("{tableId}/diff")
  public DiffService getDiff(@PathParam("tableId") String tableId) throws ODKDatastoreException;

  @Path("{tableId}/acl")
  public TableAclService getAcl(@PathParam("tableId") String tableId) throws ODKDatastoreException;
}
