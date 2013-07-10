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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Produces(MediaType.TEXT_XML)
public interface DataService {

  @GET
  public List<RowResource> getRows() throws ODKDatastoreException, PermissionDeniedException;

  @GET
  @Path("{rowId}")
  public RowResource getRow(@PathParam("rowId") String rowId) throws ODKDatastoreException,
      PermissionDeniedException;

  @PUT
  @Path("{rowId}")
  @Consumes(MediaType.TEXT_XML)
  public RowResource createOrUpdateRow(@PathParam("rowId") String rowId, Row row)
      throws ODKTaskLockException, ODKDatastoreException, EtagMismatchException,
      PermissionDeniedException, BadColumnNameException;

  @DELETE
  @Path("{rowId}")
  public void deleteRow(@PathParam("rowId") String rowId) throws ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException;

}