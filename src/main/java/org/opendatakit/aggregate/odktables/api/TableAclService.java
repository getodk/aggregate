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

import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.api.TableAclResource;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

@Produces(MediaType.TEXT_XML)
public interface TableAclService {

  @GET
  public List<TableAclResource> getAcls() throws ODKDatastoreException, PermissionDeniedException;

  @GET
  @Path("user")
  public List<TableAclResource> getUserAcls() throws ODKDatastoreException,
      PermissionDeniedException;

  @GET
  @Path("group")
  public List<TableAclResource> getGroupAcls() throws ODKDatastoreException,
      PermissionDeniedException;

  @GET
  @Path("default")
  public TableAclResource getDefaultAcl() throws ODKDatastoreException, PermissionDeniedException;

  @GET
  @Path("user/{userId}")
  public TableAclResource getUserAcl(@PathParam("userId") String userId)
      throws ODKDatastoreException, PermissionDeniedException;

  @GET
  @Path("group/{groupId}")
  public TableAclResource getGroupAcl(@PathParam("groupId") String groupId)
      throws ODKDatastoreException, PermissionDeniedException;

  @PUT
  @Path("default")
  @Consumes(MediaType.TEXT_XML)
  public TableAclResource setDefaultAcl(TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException;

  @PUT
  @Path("user/{userId}")
  @Consumes(MediaType.TEXT_XML)
  public TableAclResource setUserAcl(@PathParam("userId") String userId, TableAcl acl)
      throws ODKDatastoreException, PermissionDeniedException;

  @PUT
  @Path("group/{groupId}")
  @Consumes(MediaType.TEXT_XML)
  public TableAclResource setGroupAcl(@PathParam("groupId") String groupId, TableAcl acl)
      throws ODKDatastoreException, PermissionDeniedException;

  @DELETE
  @Path("default")
  public void deleteDefaultAcl() throws ODKDatastoreException, PermissionDeniedException;

  @DELETE
  @Path("user/{userId}")
  public void deleteUserAcl(@PathParam("userId") String userId) throws ODKDatastoreException,
      PermissionDeniedException;

  @DELETE
  @Path("group/{groupId}")
  public void deleteGroupAcl(@PathParam("groupId") String groupId) throws ODKDatastoreException,
      PermissionDeniedException;
}
