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
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResourceList;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

@Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
public interface TableAclService {

  /**
   *
   * @return {@link TableAclResourceList}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @GZIP
  public Response /*TableAclResourceList*/ getAcls() throws ODKDatastoreException, PermissionDeniedException;

  /**
   *
   * @return {@link TableAclResourceList}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @Path("user")
  @GZIP
  public Response /*TableAclResourceList*/ getUserAcls() throws ODKDatastoreException,
      PermissionDeniedException;

  /**
   *
   * @return {@link TableAclResourceList}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @Path("group")
  @GZIP
  public Response /*TableAclResourceList*/ getGroupAcls() throws ODKDatastoreException,
      PermissionDeniedException;

  /**
   *
   * @return {@link TableAclResource}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @Path("default")
  @GZIP
  public Response /*TableAclResource*/ getDefaultAcl() throws ODKDatastoreException, PermissionDeniedException;

  /**
   *
   * @param userId
   * @return {@link TableAclResource}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @Path("user/{userId}")
  @GZIP
  public Response /*TableAclResource*/ getUserAcl(@PathParam("userId") String userId)
      throws ODKDatastoreException, PermissionDeniedException;

  /**
   *
   * @param groupId
   * @return {@link TableAclResource}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @Path("group/{groupId}")
  @GZIP
  public Response /*TableAclResource*/ getGroupAcl(@PathParam("groupId") String groupId)
      throws ODKDatastoreException, PermissionDeniedException;

  /**
   *
   * @param acl
   * @return {@link TableAclResource}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @PUT
  @Path("default")
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  @GZIP
  public Response /*TableAclResource*/ setDefaultAcl(@GZIP TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException;

  /**
   *
   * @param userId
   * @param acl
   * @return {@link TableAclResource}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @PUT
  @Path("user/{userId}")
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  @GZIP
  public Response /*TableAclResource*/ setUserAcl(@PathParam("userId") String userId, @GZIP TableAcl acl)
      throws ODKDatastoreException, PermissionDeniedException;

  /**
   *
   * @param groupId
   * @param acl
   * @return {@link TableAclResource}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @PUT
  @Path("group/{groupId}")
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  @GZIP
  public Response /*TableAclResource*/ setGroupAcl(@PathParam("groupId") String groupId, @GZIP TableAcl acl)
      throws ODKDatastoreException, PermissionDeniedException;

  /**
   *
   * @return HttpStatus.OK
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @DELETE
  @Path("default")
  public Response /*void*/ deleteDefaultAcl() throws ODKDatastoreException, PermissionDeniedException;

  /**
   *
   * @param userId
   * @return HttpStatus.OK
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @DELETE
  @Path("user/{userId}")
  public Response /*void*/ deleteUserAcl(@PathParam("userId") String userId) throws ODKDatastoreException,
      PermissionDeniedException;

  /**
   *
   * @param groupId
   * @return HttpStatus.OK
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @DELETE
  @Path("group/{groupId}")
  public Response /*void*/ deleteGroupAcl(@PathParam("groupId") String groupId) throws ODKDatastoreException,
      PermissionDeniedException;
}
