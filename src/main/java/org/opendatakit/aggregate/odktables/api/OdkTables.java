package org.opendatakit.aggregate.odktables.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.impl.api.FileManifestServiceImpl;
import org.opendatakit.aggregate.odktables.impl.api.FileServiceImpl;
import org.opendatakit.aggregate.odktables.impl.api.TableServiceImpl;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

public interface OdkTables {

  public static final String CURSOR_PARAMETER = "cursor";
  public static final String FETCH_LIMIT = "fetchLimit";
  
  /**
   * Return the JSON serialized list of appNames that this server supports.
   * For ODK Aggregate, this will be a single-element list.
   * 
   * @param sc
   * @param req
   * @param httpHeaders
   * @return
   * @throws AppNameMismatchException
   * @throws PermissionDeniedException
   * @throws ODKDatastoreException
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*AppNameList*/ getAppNames(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException;

  @GET
  @Path("{appId}/clientVersions")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*ClientVersionList*/ getOdkClientVersions(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

  @GET
  @Path("{appId}/privilegesInfo")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*PrivilegesInfo*/ getPrivilegesInfo(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

  @GET
  @Path("{appId}/usersInfo")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*UserInfoList*/ getUsersInfo(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

  @POST
  @Path("{appId}/installationInfo")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response /*OK*/ postInstallationInfo(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId, Object body) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

  @Path("{appId}/manifest")
  public FileManifestServiceImpl getFileManifestService(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

  @Path("{appId}/files")
  public FileServiceImpl getFilesService(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

  @GET
  @Path("{appId}/tables")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*TableResourceList*/ getTables(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId, @QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws AppNameMismatchException,
      PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

  @Path("{appId}/tables/{tableId}")
  public TableServiceImpl getTablesService(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info, @PathParam("appId") String appId, @PathParam("tableId") String tableId) throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

}
